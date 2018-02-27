#include "http.hpp"
#include "runnable.hpp"

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
               function<void(HttpResponse)> response)
{
    if(m_http){
        m_http->get(url, header, make_shared<Http::Request>(std::move(response), m_context));
    }
}


Http::Request::Request(function<void(HttpResponse)> cb,
                       const shared_ptr<ledgerapp_gen::ExecutionContext> &context)
                        : m_context{context},
                          m_cb {std::move(cb)}
{}

void Http::Request::on_network_error() {
    HttpResponse resp;
    resp.error = true;
    _cb_with(std::move(resp));
}

void Http::Request::on_success(int16_t http_code, const string& data) {
    HttpResponse resp;
    resp.error = false;
    resp.http_code = http_code;
    resp.data = data;
    _cb_with(std::move(resp));
}

void Http::Request::_cb_with(HttpResponse resp) {
    auto callback = m_cb;
    auto shared_resp = make_shared<HttpResponse>(std::move(resp));
    auto cb_task = [callback, shared_resp] () {
        callback(std::move(*shared_resp));
    };
    auto cb_runnable = make_shared<ledgerapp::Runnable>(cb_task);
    if(m_context){
        m_context->execute(cb_runnable);
    }
}

}
