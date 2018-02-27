//
// Created by El Khalil Bellakrid on 16/01/2018.
//

#include <iostream>
#include <thread>

#include "../ledgerclient/client.hpp"
#include "../runnable.hpp"
#include "../ledgerapp.hpp"

#include "transaction_list_vm.hpp"

using namespace ledgerapp_gen;
using namespace std;

namespace ledgerapp {

    void TransactionListVmHandle::start(const shared_ptr<TransactionListVmObserver>& observer,
                                   const vector<string> &addresses, bool testnetMode) {
        m_observer = observer;
        
        auto main_context = m_thread_dispatcher->getSerialExecutionContext(ledgerapp::MAIN_EXECUTION_CONTEXT);
        
        auto self = shared_from_this();
        
        //Set testnetMode
        ledgerclient::testnetMode = testnetMode;
        
        ledgerclient::get_transactions(m_http, addresses, m_thread_dispatcher, [self, main_context](const vector<ledgerclient::Tx> &txs) mutable {

            vector <ledgerapp_gen::TransactionListVmCell> vCells;
            for (auto tx : txs) {
                vCells.emplace_back(ledgerapp_gen::TransactionListVmCell(tx.hash, tx.received_at, tx.data));
            }

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
