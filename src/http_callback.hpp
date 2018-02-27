//
// Created by El Khalil Bellakrid on 27/02/2018.
//

#ifndef LEDGER_TEST_LIBRARY_NODE_JS_HTTP_CALLBACK_H
#define LEDGER_TEST_LIBRARY_NODE_JS_HTTP_CALLBACK_H

#include "stl.hpp"

#include "interface/http_callback.hpp"
#include "interface/execution_context.hpp"

namespace ledgerapp {

    struct HttpResponse {
        bool error;
        string error_message;
        uint16_t http_code;
        string data;
    };

    class HttpCallback final : public ledgerapp_gen::HttpCallback {

    public:

        HttpCallback(function<void(HttpResponse)> cb, const shared_ptr<ledgerapp_gen::ExecutionContext> &context);

        virtual void on_network_error(const std::string &error) override ;
        virtual void on_success(int16_t http_code, const string& data) override;

        void _cb_with(HttpResponse resp);

    private:

        shared_ptr<ledgerapp_gen::ExecutionContext> m_context;
        function<void(HttpResponse)> m_cb;
    };

}


#endif //LEDGER_TEST_LIBRARY_NODE_JS_HTTP_CALLBACK_H
