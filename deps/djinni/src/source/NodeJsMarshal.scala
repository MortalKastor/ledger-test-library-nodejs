package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._
import djinni.writer.IndentWriter

import scala.collection.mutable.ListBuffer

class NodeJsMarshal(spec: Spec) extends CppMarshal(spec) {

  override def typename(tm: MExpr): String = toNodeType(tm, None, Seq())
  override def fqTypename(tm: MExpr): String = toNodeType(tm, Some(spec.cppNamespace), Seq())
  override def typename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => idNode.enumType(name)
    case i: Interface => idNode.ty(name)
    case r: Record => idNode.ty(name)
  }

  override def paramType(tm: MExpr): String = toNodeParamType(tm)
  override def fqParamType(tm: MExpr): String = toNodeParamType(tm, Some(spec.cppNamespace))
  private def toNodeParamType(tm: MExpr, namespace: Option[String] = None, scopeSymbols: Seq[String] = Seq()): String = {
    toNodeType(tm, namespace, scopeSymbols)
  }

  override def returnType(ret: Option[TypeRef], scopeSymbols: Seq[String]): String = { ret.fold("void")(toNodeType(_, None, scopeSymbols)) }
  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(toNodeType(_, None))
  override def fqReturnType(ret: Option[TypeRef]): String = {
    ret.fold("void")(toNodeType(_, Some(spec.cppNamespace)))
  }

  def hppReferences(m: Meta, exclude: String, forwardDeclareOnly: Boolean, nodeMode: Boolean): Seq[SymbolReference] = m match {
    case d: MDef => d.body match {
      case i: Interface =>
        val base = if (d.name != exclude) {

          var cppInterfaceImport = s""""${idNode.ty(d.name)}"""
          if (i.ext.cpp) {
            cppInterfaceImport = s"${cppInterfaceImport}Cpp"
          }

          cppInterfaceImport = s"""$cppInterfaceImport.${spec.cppHeaderExt}""""
          val nodeInterfaceImport = s""""${spec.nodeIncludeCpp}/${d.name}.${spec.cppHeaderExt}""""

          if (nodeMode) {
            List(ImportRef("<memory>"), ImportRef(cppInterfaceImport), ImportRef(nodeInterfaceImport))
          } else {
            List(ImportRef("<memory>"), ImportRef(cppInterfaceImport))
          }

        } else
          List(ImportRef("<memory>"))

        spec.cppNnHeader match {
          case Some(nnHdr) => ImportRef(nnHdr) :: base
          case _ => base
        }
      case _ => super.hppReferences(m, exclude, forwardDeclareOnly)
    }
    case _ => super.hppReferences(m, exclude, forwardDeclareOnly)
  }

  override def cppReferences(m: Meta, exclude: String, forwardDeclareOnly: Boolean): Seq[SymbolReference] = {

    if (!forwardDeclareOnly) {
      List()
    } else {
      m match {
        case d: MDef =>
          val nodeRecordImport = s"${spec.nodeIncludeCpp}/${d.name}"
          d.body match {
            case r: Record =>
              if (d.name != exclude) {
                List(ImportRef(include(nodeRecordImport, r.ext.cpp)))
              } else {
                List()
              }
            case e: Enum =>
              if (d.name != exclude) {
                List(ImportRef(include(nodeRecordImport)))
              } else {
                List()
              }
            case _ => List()
          }
        case _ => List()
      }
    }
  }

  override def include(ident: String, isExtendedRecord: Boolean = false): String = {
    val prefix = if (isExtendedRecord) spec.cppExtendedRecordIncludePrefix else spec.cppIncludePrefix
    q(prefix + spec.cppFileIdentStyle(ident) + "." + spec.cppHeaderExt)
  }

  private def toNodeType(ty: TypeRef, namespace: Option[String] = None, scopeSymbols: Seq[String] = Seq()): String =
    toNodeType(ty.resolved, namespace, scopeSymbols)

  private def toNodeType(tm: MExpr, namespace: Option[String], scopeSymbols: Seq[String]): String = {

    def base(m: Meta): String = m match {
      case p: MPrimitive => p.nodeJSName
      case MString => "String"
      case MDate => "Date"
      case MBinary => "Object"
      case MOptional => "MaybeLocal"
      case MList => "Array"
      case MSet => "Set"
      case MMap => "Map"
      case d: MDef =>
        d.defType match {
          case DInterface => withNamespace(idNode.ty(d.name), namespace, scopeSymbols)
          case _ => super.toCppType(tm, namespace, scopeSymbols)
        }
      case p: MParam => idNode.typeParam(p.name)
      case _ => super.toCppType(tm, namespace, scopeSymbols)
    }

    def expr(tm: MExpr): String = {
      spec.cppNnType match {
        case Some(nnType) =>
          // if we're using non-nullable pointers for interfaces, then special-case
          // both optional and non-optional interface types
          val args = if (tm.args.isEmpty) "" else tm.args.map(expr).mkString("<", ", ", ">")
          tm.base match {
            case d: MDef =>
              d.defType match {
                case DInterface => s"${nnType}<${withNamespace(idNode.ty(d.name), namespace, scopeSymbols)}>"
                case _ => base(tm.base) + args
              }
            case MOptional =>
              tm.args.head.base match {
                case d: MDef =>
                  d.defType match {
                    case DInterface => s"std::shared_ptr<${withNamespace(idCpp.ty(d.name), namespace, scopeSymbols)}>"
                    case _ => base(tm.base) + args
                  }
                case _ => base(tm.base) + args
              }
            case _ => base(tm.base) + args
          }
        case None =>
          if (isOptionalInterface(tm)) {
            // otherwise, interfaces are always plain old shared_ptr
            expr(tm.args.head)
          } else {
            base(tm.base)
          }
      }
    }

    expr(tm)
  }

  private def withNamespace(name: String, namespace: Option[String], scopeSymbols: Seq[String]): String = {

    val ns = namespace match {
      case Some(ns) => Some(ns)
      case None => if (scopeSymbols.contains(name)) Some(spec.cppNamespace) else None
    }
    withNs(ns, name)
  }

  override def toCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")
  override def fromCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")

  def toCppArgument(tm: MExpr, converted: String, converting: String, wr: IndentWriter, namespace: Option[String] = None, scopeSymbols: Seq[String] = Seq()): IndentWriter = {

    def toCppContainer(container: String): IndentWriter = {

      if (!tm.args.isEmpty) {
        val cppTemplType = super.paramType(tm.args(0), true)
        val nodeTemplType = paramType(tm.args(0))

        if (container == "Map" && tm.args.length > 1) {

          val cppTemplValueType = super.paramType(tm.args(1), true)
          val nodeTemplValueType = paramType(tm.args(1))

          wr.wl(s"map<$cppTemplType, $cppTemplValueType> $converted;")
          wr.wl(s"Local<$container> container = Local<$container>::Cast($converting);")
          wr.wl(s"auto prop_names = objectMap->GetPropertyNames();")
          wr.wl(s"for(uint32_t i = 0; i < prop_names->Length(); i++)").braced {
            wr.wl(s"auto key = prop_names->Get(i);")
            wr.wl(s"if(key->Is$nodeTemplType() && objectMap->Get(key)->Is$nodeTemplValueType())").braced {
              toCppArgument(tm.args(0), s"${converted}_1", s"key->To$nodeTemplType()", wr, namespace, scopeSymbols)
              toCppArgument(tm.args(1), s"${converted}_2", s"objectMap->Get(key)->To$nodeTemplValueType()", wr, namespace, scopeSymbols)
              wr.wl(s"$converted.emplace(${converted}_1,${converted}_2);")
            }
          }
          wr.wl
        } else {
          wr.wl(s"vector<$cppTemplType> $converted;")
          wr.wl(s"Local<$container> container = Local<$container>::Cast($converting);")
          wr.wl(s"for(uint32_t i = 0; i < container->Length(); i++)").braced {
            wr.wl(s"if(container->Get(i)->Is$nodeTemplType())").braced {
              toCppArgument(tm.args(0), s"${converted}_1", s"container->Get(i)->To$nodeTemplType()", wr, namespace, scopeSymbols)
              wr.wl(s"$converted.emplace_back(${converted}_1);")
            }
          }
          wr.wl
        }
      } else {
        wr.wl("//Type name not found !")
      }

    }

    val cppType = super.paramType(tm, needRef = true)

    def base(m: Meta): IndentWriter = m match {
      case p: MPrimitive => wr.wl(s"auto $converted = Nan::To<${toSupportedCppNativeTypes(p.cName)}>($converting).FromJust();")
      case MString =>
        wr.wl(s"String::Utf8Value string_$converted($converting->ToString());")
        wr.wl(s"auto $converted = std::string(*string_$converted);")
      case MDate => wr.wl(s"auto $converted = Nan::To<$cppType>($converting).FromJust();")
      case MBinary => wr.wl("Object")
      case MOptional => wr.wl(spec.cppOptionalTemplate)
      case MList => toCppContainer("Array")
      case MSet => toCppContainer("Set")
      case MMap => toCppContainer("Map")
      case d: MDef =>
        d.body match {
          case e: Enum => wr.wl(withNamespace(idNode.enumType(d.name), namespace, scopeSymbols))
          case r: Record =>
            // Field definitions.
            var listOfRecordArgs = new ListBuffer[String]()
            var count = 1
            for (f <- r.fields) {
              wr.wl
              val fieldName = idCpp.field(f.ident)
              val quotedFieldName = s""""$fieldName""""
              wr.wl(s"auto field_${converted}_$count = Nan::Get($converting->ToObject(), Nan::New<String>($quotedFieldName).ToLocalChecked()).ToLocalChecked();")
              toCppArgument(f.ty.resolved, s"${converted}_$count", s"field_${converted}_$count", wr, namespace, scopeSymbols)
              listOfRecordArgs += s"${converted}_$count"
              count = count + 1
            }

            wr.wl(s"${idCpp.ty(d.name)} $converted${listOfRecordArgs.toList.mkString("(", ", ", ")")};")
            wr.wl
          case i: Interface =>

            val nodeType = paramType(tm)
            val cppType = super.paramType(tm, needRef = true)
            wr.wl(s"Local<Object> njs_arg_$converted = $converting->ToObject(context).ToLocalChecked();")
            wr.wl
            wr.wl(s"$nodeType *njs_obj_$converted = static_cast<$nodeType *>(Nan::GetInternalFieldPointer(njs_arg_$converted,0));")
            //If nodeType is implemented in NodeJS it inherits from ${spec.cppNamespace}::$factoCppType
            wr.wl
            wr.wl(s"std::shared_ptr<$nodeType> $converted(njs_obj_$converted);")
            wr.wl
        }
      case e: MExtern => e.defType match {
        case DInterface => wr.wl(s"std::shared_ptr<${e.cpp.typename}>")
        case _ => wr.wl(e.cpp.typename)
      }
      case p: MParam => wr.wl(idNode.typeParam(p.name))
    }

    base(tm.base)
  }

  def fromCppArgument(tm: MExpr, converted: String, converting: String, wr: IndentWriter, namespace: Option[String] = None, scopeSymbols: Seq[String] = Seq()): IndentWriter = {

    def fromCppContainer(container: String): IndentWriter = {

      if (!tm.args.isEmpty) {

        if (container == "Map" && tm.args.length > 1) {

          wr.wl(s"Local<$container> $converted = Nan::New<$container>();")
          wr.wl(s"for(auto const& elem : $converting)").braced {
            fromCppArgument(tm.args(0), s"${converted}_1", "elem.first", wr, namespace, scopeSymbols)
            fromCppArgument(tm.args(1), s"${converted}_2", "elem.second", wr, namespace, scopeSymbols)
            wr.wl(s"$converted->Set(context, ${converted}_1, ${converted}_2});")
          }
          wr.wl

        } else {
          wr.wl(s"Local<$container> $converted = Nan::New<$container>();")
          wr.wl(s"for(size_t i = 0; i < $converting.size(); i++)").braced {
            fromCppArgument(tm.args(0), s"${converted}_1", s"$converting[i]", wr, namespace, scopeSymbols)
            wr.wl(s"$converted->Set((int)i,${converted}_1);")
          }
          wr.wl
        }
      } else {
        wr.wl("//Type name not found !")
      }

    }

    def base(m: Meta): IndentWriter = m match {
      case p: MPrimitive => wr.wl(s"auto $converted = Nan::New<${p.nodeJSName}>($converting);")
      case MString => wr.wl(s"auto $converted = Nan::New<String>($converting).ToLocalChecked();")
      case MDate => wr.wl(s"auto $converted = Nan::New<Date>($converting).ToLocalChecked();")
      //case MBinary => "std::vector<uint8_t>"
      //case MBinary => "std::vector<Number>"
      case MBinary => wr.wl(s"auto $converted = Nan::New<Object>($converting).ToLocalChecked();")
      case MOptional => fromCppArgument(tm.args(0), converted, s"(*$converting)", wr, namespace, scopeSymbols)
      case MList => fromCppContainer("Array")
      case MSet => fromCppContainer("Set")
      case MMap => fromCppContainer("Map")
      case d: MDef =>
        d.body match {
          case e: Enum => wr.wl(s"auto $converted = Nan::To<Object>($converting).ToLocalChecked();")
          case r: Record =>
            // Field definitions.
            wr.wl(s"auto $converted = Nan::New<Object>();")
            var count = 1
            for (f <- r.fields) {
              val fieldName = idCpp.field(f.ident)
              fromCppArgument(f.ty.resolved, s"${converted}_$count", s"$converting.$fieldName", wr, namespace, scopeSymbols)
              val quotedFieldName = s""""$fieldName""""
              wr.wl(s"Nan::DefineOwnProperty($converted, Nan::New<String>($quotedFieldName).ToLocalChecked(), ${converted}_$count);")
              count = count + 1
            }
            wr.wl
          case i: Interface =>
            val nodeType = paramType(tm)
            val cppType = super.paramType(tm, needRef = true)
            //Use wrap methods
            wr.wl(s"auto $converted = ${idNode.ty(d.name)}::wrap($converting);")
            wr.wl
        }
      case e: MExtern => e.defType match {
        case DInterface => wr.wl(s"auto $converted = ${idNode.ty(e.name)}::wrap($converting);")
        case _ => wr.wl(e.cpp.typename)
      }
      case p: MParam => wr.wl(s"auto $converted = Nan::To<Object>($converting).ToLocalChecked();") //wr.wl(idNode.typeParam(p.name))
    }

    base(tm.base)
  }

  private def toSupportedCppNativeTypes(inputType: String): String = {
    inputType match {
      case "int8_t" | "int16_t" => "int32_t"
      case "float" => "double"
      case _ => inputType
    }
  }
}

