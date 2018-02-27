//
// Created by El Khalil Bellakrid on 16/01/2018.
//

#include <iostream>
#include <thread>

#include "../ledgerclient/client.hpp"
#include "../runnable.hpp"
#include "../ledgerapp.hpp"

#include "../interface/response.hpp"

#include "transaction_list_vm.hpp"

using namespace ledgerapp_gen;
using namespace std;

namespace ledgerapp {

    void TransactionListVmHandle::start(const shared_ptr<TransactionListVmObserver>& observer,
                                        const vector<string> &addresses,
                                        const ledgerapp_gen::ApiOptions & options,
                                        const std::shared_ptr<ledgerapp_gen::HandleResponse> & response) {
        m_observer = observer;
        
        auto main_context = m_thread_dispatcher->getSerialExecutionContext(ledgerapp::MAIN_EXECUTION_CONTEXT);
        
        auto self = shared_from_this();
        
        //Set testnetMode
        ledgerclient::testnetMode = (options.coin_type == 1);
        
        ledgerclient::get_transactions(m_http, addresses, m_thread_dispatcher, response, [self, main_context, response](const vector<ledgerclient::Tx> &txs) mutable {

            //TODO: refactor HandleResponse and Observer
            //Test Integration for HandleResponse
            //Here handle response and observer are having same purpose we can refactor to have one of them doing all the job
            //for test purpose, we leave both of them here
            vector<ledgerapp_gen::TransactionListVmCell> vCells;
            string txs_data;
            for (auto tx : txs) {
                txs_data.append(tx.data);
                vCells.emplace_back(ledgerapp_gen::TransactionListVmCell(tx.hash, tx.received_at, tx.data));
            }
            ledgerapp_gen::Response res("", txs_data);
            response->respond(res);
            //In new context
            const weak_ptr <TransactionListVmHandle> weak_self = self;
        
            auto local_observer = self->getObserver();

            auto task = [weak_self, vCells, local_observer]() mutable {
                auto data = make_shared<TransactionListVm>(vCells, weak_self);
                local_observer->on_update(data);
            };
            
            auto runnable = make_shared<ledgerapp::Runnable>(task);
            if(main_context){
                main_context->execute(runnable);
            }

        });
    }

    void TransactionListVmHandle::stop() {
        // this isn't implemented yet
    }


    void TransactionListVmHandle::_notify_new_data() {
        // this isn't implemented yet
    }
}
