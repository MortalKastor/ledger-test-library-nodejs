#include <iostream>

#include "ui_interface/transaction_list_vm.hpp"

#include "api.hpp"
#include "ledgerclient/client.hpp"
#include "ledgerclient/types.hpp"
#include "ledgerapp.hpp"

using ledgerapp::Api;
using json11::Json;

std::shared_ptr<ledgerapp_gen::Api> ledgerapp_gen::Api::create_api(const std::shared_ptr<ledgerapp_gen::Http> & http_impl,
                                const std::shared_ptr<ledgerapp_gen::ThreadDispatcher> &thread_dispatcher){
    return make_shared<ledgerapp::Api>(http_impl, thread_dispatcher);
}


Api::Api(const shared_ptr<ledgerapp_gen::Http> & http_impl,
         const shared_ptr<ledgerapp_gen::ThreadDispatcher> &thread_dispatcher):
         m_thread_dispatcher(thread_dispatcher)
{
    m_http = make_shared<ledgerapp::Http>(http_impl, m_thread_dispatcher->getSerialExecutionContext(ledgerapp::MAIN_EXECUTION_CONTEXT));
}

shared_ptr<ledgerapp_gen::TransactionListVmHandle> Api::observer_transaction_list() {
    return make_shared<ledgerapp::TransactionListVmHandle>(m_http,m_thread_dispatcher);
}

vector<string> Api::get_transactions(const string & address){
    ///TODO: instantiate a Handle and call get transactions on it
    std::vector<std::string>testVector(1,address);
    return testVector;
}


