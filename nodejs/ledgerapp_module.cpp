#include <node.h>
#include <nan.h>

#include "gen/NJSItfHttp.hpp"
#include "gen/NJSItfApiCpp.hpp"
#include "gen/NJSItfHttpCallbackCpp.hpp"
#include "gen/NJSItfLock.hpp"
#include "gen/NJSItfExecutionContext.hpp"
#include "gen/NJSItfThreadDispatcher.hpp"
#include "gen/NJSItfTransactionListVmCpp.hpp"
#include "gen/NJSItfTransactionListVmHandleCpp.hpp"
#include "gen/NJSItfTransactionListVmObserver.hpp"
#include "gen/NJSItfRunnableCpp.hpp"

using namespace std;
using namespace v8;
using namespace node;

static void init(Local<Object> target){

    Nan::HandleScope scope;

    NJSItfHttp::Initialize(target);
    NJSItfApi::Initialize(target);
    NJSItfHttpCallback::Initialize(target);
    NJSItfLock::Initialize(target);
    NJSItfExecutionContext::Initialize(target);
    NJSItfThreadDispatcher::Initialize(target);
    NJSItfTransactionListVm::Initialize(target);
    NJSItfTransactionListVmHandle::Initialize(target);
    NJSItfTransactionListVmObserver::Initialize(target);
    NJSItfRunnable::Initialize(target);


}

NODE_MODULE(ledgerapp_nodejs, init);
