#ifndef LEDGERAPP_API_H
#define LEDGERAPP_API_H

#include "stl.hpp"
#include <json11/json11.hpp>
#include "http.hpp"

#include "interface/api.hpp"

using namespace std;

namespace ledgerapp {

// the "api" of how the UI is allowed to talk to the c++ code
class Api final : public ledgerapp_gen::Api {

public:
    Api(const shared_ptr<ledgerapp_gen::Http> & http_impl,
        const shared_ptr<ledgerapp_gen::ThreadDispatcher> &thread_dispatcher);

    virtual shared_ptr<ledgerapp_gen::TransactionListVmHandle> observer_transaction_list() override;
    virtual vector<string> get_transactions(const string & address) override;

  private:

    shared_ptr<ledgerapp_gen::ThreadDispatcher> m_thread_dispatcher;
    shared_ptr<ledgerapp::Http> m_http;
};

}

#endif
