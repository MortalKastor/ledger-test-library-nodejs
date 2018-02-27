#ifndef LEDGERAPP_HTTP_H
#define LEDGERAPP_HTTP_H
#include "stl.hpp"

#include "http_callback.hpp"

#include "interface/http.hpp"
#include "interface/http_header.hpp"
#include "interface/execution_context.hpp"

namespace ledgerapp {

class Http final : ledgerapp_gen::Http {

  public:

    Http(shared_ptr<ledgerapp_gen::Http> http_impl, const shared_ptr<ledgerapp_gen::ExecutionContext> &context);

    virtual void get(const std::string & url,
                     const std::experimental::optional<std::vector<ledgerapp_gen::HttpHeader>> & header,
                     const std::shared_ptr<ledgerapp_gen::HttpCallback> & callback) override;

    void get(const std::string & url,
             const std::experimental::optional<std::vector<ledgerapp_gen::HttpHeader>> & header,
             function<void(ledgerapp::HttpResponse)>);

    shared_ptr<ledgerapp_gen::Http> getHttpRequest(){return m_http;};

  private:

    shared_ptr<ledgerapp_gen::ExecutionContext> m_context;
    shared_ptr<ledgerapp_gen::Http> m_http;
};

}

#endif
