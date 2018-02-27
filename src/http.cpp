#include "http.hpp"

namespace ledgerapp {

Http::Http(shared_ptr<ledgerapp_gen::Http> http_impl,
           const shared_ptr<ledgerapp_gen::ExecutionContext> &context)
            : m_context{std::move(context)},
              m_http {std::move(http_impl)}
{}

void Http::get(const std::string & url,
               const std::experimental::optional<std::vector<ledgerapp_gen::HttpHeader>> & header,
               const std::shared_ptr<ledgerapp_gen::HttpCallback> & callback)
{
    if(m_http){
        m_http->get(url, header, callback);
    }
}

void Http::get(const std::string & url,
                const std::experimental::optional<std::vector<ledgerapp_gen::HttpHeader>> & header,
               function<void(ledgerapp::HttpResponse)> response)
{
    if(m_http){
        m_http->get(url, header, make_shared<ledgerapp::HttpCallback>(std::move(response), m_context));
    }
}


}
