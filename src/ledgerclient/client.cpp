#include <iostream>
#include <sstream>

#include "client.hpp"
#include "../ledgerapp.hpp"
#include "../runnable.hpp"

#include "../interface/http_header.hpp"


using ledgerclient::Client;

using namespace json11;
using namespace std;

bool ledgerclient::testnetMode = false;

ledgerclient::Tx ledgerclient::parse_transaction(const json11::Json& data){
    ledgerclient::Tx tx;
    tx.hash = data["hash"].string_value();
    tx.received_at = data["received_at"].string_value();
    //tx.data = data.string_value();
    tx.data = data.dump();
    return tx;
}

void ledgerclient::get_token(const shared_ptr<ledgerapp::Http> &http,
                             function<void(const std::string&)> callback){
    if(http){

        //Get sync token
        string url = ledgerclient::testnetMode ? ledgerapp::TESTNET_BASE_URL : ledgerapp::BASE_URL;
        url.append("syncToken");

        std::vector<ledgerapp_gen::HttpHeader> header;

        http->get(url, header, [callback] (ledgerapp::HttpResponse resp) {

            if (resp.error) {
                return;
            }
            vector<ledgerclient::Tx> txs;
            string error;
            auto json_response = Json::parse(resp.data, error);
            if (!error.empty()) {
                // there was an error
                // fail somehow
            } else {
                callback(json_response["token"].string_value());
            }
        });
    }
}

void ledgerclient::get_transactions(const shared_ptr<ledgerapp::Http> &http,
                                    const vector<string> &addresses,
                                    const shared_ptr<ledgerapp_gen::ThreadDispatcher> &thread_dispatcher,
                                    function<void(vector<ledgerclient::Tx>)> callback)
{

    //Get sync token
    string url = ledgerclient::testnetMode ? ledgerapp::TESTNET_BASE_URL : ledgerapp::BASE_URL;

    //Join addresses
    const string separator =",";
    ostringstream ss;
    auto it = addresses.begin();
    if(it != addresses.end()) {
        ss << *it++;
    }
    while(it != addresses.end()) {
        ss << separator;
        ss << *it++;
    }
    url.append("addresses/" + ss.str() + "/transactions");

    std::string received_token;

    auto back_context = thread_dispatcher->getSerialExecutionContext(ledgerapp::BACK_EXECUTION_CONTEXT);
    auto local_http = http;
    get_token(http, [&,local_http,url,callback,back_context](const std::string &token) mutable {

        std::vector<ledgerapp_gen::HttpHeader> header;
        header.emplace_back(ledgerapp_gen::HttpHeader("X-LedgerWallet-SyncToken",token));

        //we capture thread dispatcher because we want to run on main thread injection of txs
        auto tx_task = [&,local_http,url,header,callback](){

            local_http->get(url, header, [&,callback] (ledgerapp::HttpResponse resp) {
                if (resp.error) {
                    return;
                }
                string error;
                auto json_response = Json::parse(resp.data, error);

                if (!error.empty()) {
                    // there was an error
                    // fail somehow
                }else {
                    if (json_response.is_object()) {

                        bool truncated = json_response["truncated"].bool_value();
                        if(truncated)
                            std::cout<<"Is truncated !!!"<<std::endl;

                        vector<ledgerclient::Tx> txs;

                        for (const auto& item : json_response["txs"].array_items()) {
                            txs.emplace_back( ledgerclient::parse_transaction(item) );
                        }

                        callback(txs);
                    }
                }
            });
        };
        auto tx_runnable = make_shared<ledgerapp::Runnable>(tx_task);
        back_context->execute(tx_runnable);
    });
}
Client::Client(const shared_ptr<ledgerapp::Http> &http_client) : m_http(http_client)
{}

void Client::get_transactions(const vector<string> &addresses,
                              const shared_ptr<ledgerapp_gen::ThreadDispatcher> &thread_dispatcher,
                              function<void(vector<ledgerclient::Tx>)>callback)
{
    ledgerclient::get_transactions(m_http, addresses, thread_dispatcher, callback);
}
