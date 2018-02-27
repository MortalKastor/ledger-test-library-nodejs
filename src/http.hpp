#ifndef LEDGERAPP_HTTP_H
#define LEDGERAPP_HTTP_H
#include "stl.hpp"

#include "interface/http.hpp"
#include "interface/http_header.hpp"
#include "interface/http_callback.hpp"
#include "interface/execution_context.hpp"

namespace ledgerapp {

struct HttpResponse {
    bool error;
    uint16_t http_code;
    string data;
};

class Http final : ledgerapp_gen::Http {

  public:

    Http(shared_ptr<ledgerapp_gen::Http> http_impl, const shared_ptr<ledgerapp_gen::ExecutionContext> &context);

    virtual void get(const std::string & url,
                     const std::experimental::optional<std::vector<ledgerapp_gen::HttpHeader>> & header,
                     const std::shared_ptr<ledgerapp_gen::HttpCallback> & callback) override;

    void get(const std::string & url,
             const std::experimental::optional<std::vector<ledgerapp_gen::HttpHeader>> & header,
             function<void(HttpResponse)>);

    shared_ptr<ledgerapp_gen::Http> getHttpRequest(){return m_http;};

  private:

    class Request final : public ledgerapp_gen::HttpCallback {

      public:

        Request(function<void(HttpResponse)> cb, const shared_ptr<ledgerapp_gen::ExecutionContext> &context);

        virtual void on_network_error() override ;
        virtual void on_success(int16_t http_code, const string& data) override;

        void _cb_with(HttpResponse resp);

      private:

        shared_ptr<ledgerapp_gen::ExecutionContext> m_context;
        function<void(HttpResponse)> m_cb;
    };

    shared_ptr<ledgerapp_gen::ExecutionContext> m_context;
    shared_ptr<ledgerapp_gen::Http> m_http;
};

}

#endif
