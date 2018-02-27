package djinni

import  djinni.ast._
import djinni.generatorTools._
import djinni.meta._
import djinni.writer.IndentWriter

class NodeJsMarshal(spec: Spec) extends Marshal(spec) {

  protected val cppMarshal = new CppMarshal(spec)

  override def typename(tm: MExpr): String = toNodeType(tm, None, Seq())
  def typename(tm: MExpr, scopeSymbols: Seq[String]): String = toNodeType(tm, None, scopeSymbols)
  def typename(ty: TypeRef, scopeSymbols: Seq[String]): String = typename(ty.resolved, scopeSymbols)
  def typename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => idNode.enumType(name)
    case i: Interface => idNode.ty(name)
    case r: Record => idNode.ty(name)
  }

  override def fqTypename(tm: MExpr): String = toNodeType(tm, Some(spec.cppNamespace), Seq())
  def fqTypename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => withNs(Some(spec.cppNamespace), idNode.enumType(name))
    case i: Interface => withNs(Some(spec.cppNamespace), idNode.ty(name))
    case r: Record => withNs(Some(spec.cppNamespace), idNode.ty(name))
  }

  override def paramType(tm: MExpr): String = toNodeParamType(tm)
  override def fqParamType(tm: MExpr): String = toNodeParamType(tm, Some(spec.cppNamespace))

  def returnType(ret: Option[TypeRef], scopeSymbols: Seq[String]): String = {
    ret.fold("void")(toNodeType(_, None, scopeSymbols))
  }
  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(toNodeType(_, None))
  override def fqReturnType(ret: Option[TypeRef]): String = {
    ret.fold("void")(toNodeType(_, Some(spec.cppNamespace)))
  }

  def fieldType(tm: MExpr, scopeSymbols: Seq[String]): String = typename(tm, scopeSymbols)
  def fieldType(ty: TypeRef, scopeSymbols: Seq[String]): String = fieldType(ty.resolved, scopeSymbols)
  override def fieldType(tm: MExpr): String = typename(tm)
  override def fqFieldType(tm: MExpr): String = fqTypename(tm)

  private def toSupportedCppNativeTypes(inputType : String): String = {
    inputType match{
      case "int8_t" | "int16_t" => "int32_t"
      case "float" => "double"
      case _ => inputType
    }
  }

  def toCppArgument(tm: MExpr, index: Int, converting: String, wr: IndentWriter, namespace: Option[String] = None, scopeSymbols: Seq[String] = Seq()): IndentWriter = {

    def toCppContainer(container: String): IndentWriter ={

      if(!tm.args.isEmpty){
        val cppTemplType = cppMarshal.paramType(tm.args(0), true)
        val nodeTemplType = paramType(tm.args(0))

        if(container == "Map" && tm.args.length > 1){

          val cppTemplValueType = cppMarshal.paramType(tm.args(1), true)
          val nodeTemplValueType = paramType(tm.args(1))

          wr.wl(s"map<$cppTemplType, $cppTemplValueType> arg_$index;")
          //wr.wl(s"auto objectMap = Nan::To<Object>($converting).ToLocalChecked();")
          wr.wl(s"Local<$container> container = Local<$container>::Cast($converting);")
          wr.wl(s"auto prop_names = objectMap->GetPropertyNames();")
          wr.wl(s"for(uint32_t i = 0; i < prop_names->Length(); i++)").braced{
            wr.wl(s"auto key = prop_names->Get(i);")
            wr.wl(s"if(key->Is$nodeTemplType() && objectMap->Get(key)->Is$nodeTemplValueType())").braced{
              toCppArgument(tm.args(0), index + 1, s"key->To$nodeTemplType()", wr , namespace, scopeSymbols)
              toCppArgument(tm.args(1), index + 2, s"objectMap->Get(key)->To$nodeTemplValueType()", wr , namespace, scopeSymbols)
              wr.wl(s"arg_$index.emplace(arg_${index + 1},arg_${index + 2});")
            }
          }
          wr.wl
        }else{
          wr.wl(s"vector<$cppTemplType> arg_$index;")
          //wr.wl(s"auto container = Nan::To<$container>($converting).ToLocalChecked();")
          wr.wl(s"Local<$container> container = Local<$container>::Cast($converting);")
          wr.wl(s"for(uint32_t i = 0; i < container->Length(); i++)").braced{
            wr.wl(s"if(container->Get(i)->Is$nodeTemplType())").braced{
              toCppArgument(tm.args(0), index + 1, s"container->Get(i)->To$nodeTemplType()", wr , namespace, scopeSymbols)
              wr.wl(s"arg_$index.emplace_back(arg_${index + 1});")
            }
          }
          wr.wl
        }
      }else{
        wr.wl("//Type name not found !")
      }

    }
    val cppType = cppMarshal.paramType(tm, true)

    def base(m: Meta): IndentWriter = m match {
      case p: MPrimitive => wr.wl(s"auto arg_$index = Nan::To<${toSupportedCppNativeTypes(p.cName)}>($converting).FromJust();")
      case MString =>{
        wr.wl(s"String::Utf8Value string_$index($converting->ToString());")
        wr.wl(s"auto arg_$index = std::string(*string_$index);")
      }
      case MDate => wr.wl(s"auto arg_$index = Nan::To<$cppType>($converting).FromJust();")
      //case MBinary => "std::vector<uint8_t>"
      //case MBinary => "std::vector<Number>"
      case MBinary => wr.wl("Object")
      case MOptional => wr.wl(spec.cppOptionalTemplate)
      case MList => toCppContainer("Array")
      case MSet => toCppContainer("Set")
      case MMap => toCppContainer("Map")
      case d: MDef =>
        d.defType match {
          case DEnum => wr.wl(withNamespace(idNode.enumType(d.name), namespace, scopeSymbols))
          case DRecord => wr.wl(withNamespace(idNode.ty(d.name), namespace, scopeSymbols))
          case DInterface => {
            val nodeType = paramType(tm)
            val cppType = cppMarshal.paramType(tm, true)
            //TODO: convert from Object to C++ object (Unwrap)
            wr.wl(s"Local<Object> njs_arg_$index = $converting->ToObject(context).ToLocalChecked();")
            wr.wl
            wr.wl(s"$nodeType *njs_obj_$index = static_cast<$nodeType *>(Nan::GetInternalFieldPointer(njs_arg_$index,0));")
            //If nodeType is implemented in NodeJS it inherits from ${spec.cppNamespace}::$factoCppType
            wr.wl
            wr.wl(s"std::shared_ptr<$nodeType> arg_$index(njs_obj_$index);")
            wr.wl
          }
        }
      case e: MExtern => e.defType match {
        case DInterface => wr.wl(s"std::shared_ptr<${e.cpp.typename}>")
        case _ => wr.wl(e.cpp.typename)
      }
      case p: MParam => wr.wl(idNode.typeParam(p.name))
    }
    base(tm.base)
  }

  /*private def toSupportedNodeNativeTypes(inputType : String): String = {
    inputType match{
      case "int8_t" | "int16_t" => "int32_t"
      case "float" => "double"
      case _ => inputType
    }
  }*/

  def fromCppArgument(tm: MExpr, index: Int, converting: String, wr: IndentWriter, namespace: Option[String] = None, scopeSymbols: Seq[String] = Seq()): IndentWriter = {

    def fromCppContainer(container: String): IndentWriter ={

      if(!tm.args.isEmpty){

        if(container == "Map" && tm.args.length > 1){

          wr.wl(s"Local<$container> arg_$index = Nan::New<$container>();")
          wr.wl(s"for(auto const& elem : $converting)").braced{
            fromCppArgument(tm.args(0), index + 1, "elem.first", wr , namespace, scopeSymbols)
            fromCppArgument(tm.args(1), index + 2, "elem.second", wr , namespace, scopeSymbols)
            wr.wl(s"arg_$index->Set(context, arg_${index + 1}, arg_${index + 2});")
          }
          wr.wl

        }else{
          wr.wl(s"Local<$container> arg_$index = Nan::New<$container>();")
          wr.wl(s"for(size_t i = 0; i < ${converting}.size(); i++)").braced{
            fromCppArgument(tm.args(0), index + 1, s"$converting[i]", wr , namespace, scopeSymbols)
            wr.wl(s"arg_$index->Set((int)i,arg_${index + 1});")
          }
          wr.wl
        }
      }else{
        wr.wl("//Type name not found !")
      }

    }
    val cppType = cppMarshal.paramType(tm, true)

    def base(m: Meta): IndentWriter = m match {
      case p: MPrimitive => wr.wl(s"auto arg_$index = Nan::New<${p.nodeJSName}>($converting);")
      case MString =>{
        wr.wl(s"auto arg_$index = Nan::New<String>($converting).ToLocalChecked();")
      }
      case MDate => wr.wl(s"auto arg_$index = Nan::New<Date>($converting).ToLocalChecked();")
      //case MBinary => "std::vector<uint8_t>"
      //case MBinary => "std::vector<Number>"
      case MBinary => wr.wl(s"auto arg_$index = Nan::New<Object>($converting).ToLocalChecked();")
      //case MOptional => wr.wl(spec.cppOptionalTemplate)
      case MOptional => {
        fromCppArgument(tm.args(0), index, s"(*$converting)", wr, namespace, scopeSymbols)
      }
      case MList => fromCppContainer("Array")
      case MSet => fromCppContainer("Set")
      case MMap => fromCppContainer("Map")
      case d: MDef =>
        d.body match {
          case e: Enum => wr.wl(s"auto arg_$index = Nan::To<Object>($converting).ToLocalChecked();")
          case r: Record => {
            // Field definitions.
            wr.wl(s"auto arg_$index = Nan::New<Object>();")
            var count = 1
            for (f <- r.fields) {
              val fieldName = idCpp.field(f.ident)
              fromCppArgument(f.ty.resolved, index + count, s"$converting.$fieldName", wr, namespace, scopeSymbols)
              //wr.wl(s"auto prop_${index + count} = arg_$index->CreateDataProperty(context,uint32_t(${count -1}),arg_${index + count});")
              val quotedFieldName = s""""$fieldName""""
              wr.wl(s"Nan::DefineOwnProperty(arg_$index, Nan::New<String>($quotedFieldName).ToLocalChecked(), arg_${index + count});")
              count = count + 1
            }
            wr.wl
          }
          case i: Interface => {
            val nodeType = paramType(tm)
            val cppType = cppMarshal.paramType(tm, true)
            //Use wrap methods
            wr.wl(s"auto arg_$index = ${idNode.ty(d.name)}::wrap($converting);")
            wr.wl
          }
        }
      case e: MExtern => e.defType match {
        case DInterface =>  wr.wl(s"auto arg_$index = ${idNode.ty(e.name)}::wrap($converting);")
        case _ => wr.wl(e.cpp.typename)
      }
      case p: MParam => wr.wl(s"auto arg_$index = Nan::To<Object>($converting).ToLocalChecked();") //wr.wl(idNode.typeParam(p.name))
    }
    base(tm.base)
  }

  override def toCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")
  override def fromCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")

  def hppReferences(m: Meta, exclude: String, forwardDeclareOnly: Boolean, nodeMode: Boolean): Seq[SymbolReference] = m match {
    case p: MPrimitive => p.idlName match {
      case "i8" | "i16" | "i32" | "i64" => List(ImportRef("<cstdint>"))
      case _ => List()
    }
    case MString => List(ImportRef("<string>"))
    case MDate => List(ImportRef("<chrono>"))
    case MBinary => List(ImportRef("<vector>"), ImportRef("<cstdint>"))
    case MOptional => List(ImportRef(spec.cppOptionalHeader))
    case MList => List(ImportRef("<vector>"))
    case MSet => List(ImportRef("<unordered_set>"))
    case MMap => List(ImportRef("<unordered_map>"))
    case d: MDef => d.body match {
      case r: Record =>
        if (d.name != exclude) {
          if (forwardDeclareOnly) {
            List(DeclRef(s"struct ${typename(d.name, d.body)};", Some(spec.cppNamespace)))
          } else {
            List(ImportRef(include(d.name, r.ext.cpp)))
          }
        } else {
          List()
        }
      case e: Enum =>
        if (d.name != exclude) {
          if (forwardDeclareOnly) {
            List(DeclRef(s"enum class ${typename(d.name, d.body)};", Some(spec.cppNamespace)))
          } else {
            List(ImportRef(include(d.name)))
          }
        } else {
          List()
        }
      case i: Interface =>
        val base = if (d.name != exclude) {

          var cppInterfaceImport = s""""${idNode.ty(d.name)}"""
          if(i.ext.cpp){
            cppInterfaceImport = s"${cppInterfaceImport}Cpp"
          }

          cppInterfaceImport = s"""${cppInterfaceImport}.${spec.cppHeaderExt}""""
          val nodeInterfaceImport = s""""${spec.nodeIncludeCpp}/${d.name}.${spec.cppHeaderExt}""""

          if(nodeMode){
            List(ImportRef("<memory>"), ImportRef(cppInterfaceImport), ImportRef(nodeInterfaceImport))
          }else{
            List(ImportRef("<memory>"), ImportRef(cppInterfaceImport))
          }

        } else
          List(ImportRef("<memory>"))

        spec.cppNnHeader match {
          case Some(nnHdr) => ImportRef(nnHdr) :: base
          case _ => base
        }
    }

    case e: MExtern => e.defType match {
      case DInterface => List(ImportRef(e.cpp.header))
      case _ => List(ImportRef(e.cpp.header))
    }
    case p: MParam => List()
  }

  def cppReferences(m: Meta, exclude: String, forwardDeclareOnly: Boolean): Seq[SymbolReference] = {

    if (!forwardDeclareOnly) {
      List()
    } else {
      m match {
        case d: MDef => {
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
        }
        case _ => List()
      }
    }
  }


  def include(ident: String, isExtendedRecord: Boolean = false): String = {
    val prefix = if (isExtendedRecord) spec.cppExtendedRecordIncludePrefix else spec.cppIncludePrefix
    q(prefix + spec.cppFileIdentStyle (ident) + "." + spec.cppHeaderExt)
  }

  private def toNodeType(ty: TypeRef, namespace: Option[String] = None, scopeSymbols: Seq[String] = Seq()): String =
    toNodeType(ty.resolved, namespace, scopeSymbols)

  private def withNamespace(name: String, namespace: Option[String], scopeSymbols: Seq[String]): String = {

    val ns = namespace match {
      case Some(ns) => Some(ns)
      case None => if (scopeSymbols.contains(name)) Some(spec.cppNamespace) else None
    }
    withNs(ns, name)
  }

  private def toNodeType(tm: MExpr, namespace: Option[String], scopeSymbols: Seq[String]): String = {

    def base(m: Meta): String = m match {
      case p: MPrimitive => p.nodeJSName
      case MString => if (spec.cppUseWideStrings) "std::wstring" else "String"
      //case MDate => "Object"
      case MDate => "Date"
      //case MBinary => "std::vector<uint8_t>"
      //case MBinary => "std::vector<Number>"
      case MBinary => "Object"
      case MOptional => spec.cppOptionalTemplate
      case MList => "Array"
      case MSet => "Set"
      case MMap => "Map"
      case d: MDef =>
        d.defType match {
          case DEnum => withNamespace(idNode.enumType(d.name), namespace, scopeSymbols)
          case DRecord => withNamespace(idNode.ty(d.name), namespace, scopeSymbols)
          case DInterface => {
            withNamespace(idNode.ty(d.name), namespace, scopeSymbols)
          }
        }
      case e: MExtern => e.defType match {
        case DInterface => s"std::shared_ptr<${e.cpp.typename}>"
        case _ => e.cpp.typename
      }
      case p: MParam => idNode.typeParam(p.name)
    }
    def expr(tm: MExpr): String = {
      spec.cppNnType match {
        case Some(nnType) => {
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
        }
        case None =>
          if (isOptionalInterface(tm)) {
            // otherwise, interfaces are always plain old shared_ptr
            expr(tm.args.head)
          } else {
            val args = if (tm.args.isEmpty) "" else tm.args.map(expr).mkString("<", ", ", ">")
            base(tm.base)
          }
      }
    }
    expr(tm)
  }

  def byValue(tm: MExpr): Boolean = tm.base match {
    case p: MPrimitive => true
    case d: MDef => d.defType match {
      case DEnum => true
      case _  => false
    }
    case e: MExtern => e.defType match {
      case DInterface => false
      case DEnum => true
      case DRecord => e.cpp.byValue
    }
    case MOptional => byValue(tm.args.head)
    case _ => false
  }

  def byValue(td: TypeDecl): Boolean = td.body match {
    case i: Interface => false
    case r: Record => false
    case e: Enum => true
  }

  private def toNodeParamType(tm: MExpr, namespace: Option[String] = None, scopeSymbols: Seq[String] = Seq()): String = {
    toNodeType(tm, namespace, scopeSymbols)
  }
}

