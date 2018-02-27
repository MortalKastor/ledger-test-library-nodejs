//lazy loading
var binding = null;
function loadBinding(){
    if(!binding){
        binding = require('bindings')('testapp_nodejs');
    }
}

loadBinding();

function NJSCppInterface() {
    loadBinding();
    var thisPlusArgs = new Array(arguments.length + 1);
    thisPlusArgs[0] = null;
    for(var i = 0; i < arguments.length; i++)
        thisPlusArgs[i + 1] = arguments[i];
    this._raw = new (Function.prototype.bind.apply(binding.NJSCppInterface,
        thisPlusArgs))();
    //inherit from this._raw
    for(var i in binding.NJSCppInterface.prototype)
        this[i] = binding.NJSCppInterface.prototype[i].bind(this._raw);
}

const NJSCppInterfaceObj = new NJSCppInterface("5");

console.log(NJSCppInterfaceObj._raw.getCppVersion("9"))

exports.NJSCppInterface = NJSCppInterface;
