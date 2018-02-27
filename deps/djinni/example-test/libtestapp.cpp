//
// Created by El Khalil Bellakrid on 25/02/2018.
//
#include <iostream>
#include <node.h>
#include <nan.h>

#include "generated-src/nodejs/NJSCppInterfaceCpp.hpp"
#include "generated-src/nodejs/NJSNodeInterface.hpp"

using namespace std;
using namespace v8;
using namespace node;

static void init(Local<Object> target){

    Nan::HandleScope scope;

    cout<<"======================Registering testapp_nodejs"<<endl;
    NJSCppInterface::Initialize(target);
    NJSNodeInterface::Initialize(target);
}

NODE_MODULE(testapp_nodejs, init);
