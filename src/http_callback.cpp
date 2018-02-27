//
// Created by El Khalil Bellakrid on 27/02/2018.
//

#include "http_callback.hpp"
#include "runnable.hpp"
namespace ledgerapp {

    HttpCallback::HttpCallback(function<void(HttpResponse)> cb,
                           const shared_ptr<ledgerapp_gen::ExecutionContext> &context)
            : m_context{context},
              m_cb {std::move(cb)}
    {}

    void HttpCallback::on_network_error(const std::string &error) {
        HttpResponse resp;
        resp.error = true;
        _cb_with(std::move(resp));
    }

    void HttpCallback::on_success(int16_t http_code, const string& data) {
        HttpResponse resp;
        resp.error = false;
        resp.http_code = http_code;
        resp.data = data;
        _cb_with(std::move(resp));
    }

    void HttpCallback::_cb_with(HttpResponse resp) {
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

