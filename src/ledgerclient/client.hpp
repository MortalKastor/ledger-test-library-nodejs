#ifndef LEDGERCLIENT_CLIENT_H
#define LEDGERCLIENT_CLIENT_H

#include "stl.hpp"
#include "types.hpp"
#include "../http.hpp"
#include "../interface/thread_dispatcher.hpp"

using namespace std;

namespace ledgerclient {

    extern bool testnetMode;
    ledgerclient::Tx parse_transaction(const json11::Json& json);

    void get_token(const shared_ptr<ledgerapp::Http> &http,
                   function<void(const std::string&)> callback);

    void get_transactions(const shared_ptr<ledgerapp::Http> &http,
                          const vector<string> &addresses,
                          const shared_ptr<ledgerapp_gen::ThreadDispatcher> &thread_dispatcher,
                          function<void(vector<ledgerclient::Tx>)>);

class Client final {

  public:

    Client(const shared_ptr<ledgerapp::Http> &http_client);

    void get_transactions(const vector<string> &addresses,
                          const shared_ptr<ledgerapp_gen::ThreadDispatcher> &thread_dispatcher,
                          function<void(vector<ledgerclient::Tx>)>);
  private:

    shared_ptr<ledgerapp::Http> m_http;
};

}
#endif
