//
// Created by El Khalil Bellakrid on 16/01/2018.
//

#ifndef LEDGERAPP_TRANSACTION_VM_H
#define LEDGERAPP_TRANSACTION_VM_H

#include "stl.hpp"

#include "../interface/transaction_list_vm.hpp"
#include "../interface/transaction_list_vm_cell.hpp"
#include "../interface/transaction_list_vm_handle.hpp"
#include "../interface/transaction_list_vm_observer.hpp"
#include "../interface/execution_context.hpp"
#include "../interface/thread_dispatcher.hpp"

#include "../http.hpp"



using namespace std;
namespace ledgerapp {

    class TransactionListVmHandle;
    
    class TransactionListVm final : public ledgerapp_gen::TransactionListVm {

    public:

        virtual int32_t count() override {return static_cast<int32_t> (m_transactions.size());};
        virtual experimental::optional<ledgerapp_gen::TransactionListVmCell> getTransaction(int32_t index) override { return m_transactions[index];};

        TransactionListVm(const vector<ledgerapp_gen::TransactionListVmCell>& txs,
                          const weak_ptr<TransactionListVmHandle> & handle):
                m_transactions(txs),
                m_handle(handle)
                {};

    private:

        vector<ledgerapp_gen::TransactionListVmCell> m_transactions;
        weak_ptr<TransactionListVmHandle> m_handle;
    };

    class TransactionListVmHandle final : public ledgerapp_gen::TransactionListVmHandle,
                                          public enable_shared_from_this<TransactionListVmHandle> {
    public:

        TransactionListVmHandle(
                const shared_ptr<ledgerapp::Http>& http,
                const shared_ptr<ledgerapp_gen::ThreadDispatcher> &thread_dispatcher):
                m_http(http),
                m_observer(nullptr),
                m_thread_dispatcher(thread_dispatcher)
        {
            this->_notify_new_data();
        };

        virtual void start(const shared_ptr<ledgerapp_gen::TransactionListVmObserver>& observer,
                           const vector<string> &addresses, bool testnetMode) override;
        virtual void stop() override;

        shared_ptr<ledgerapp_gen::TransactionListVmObserver> getObserver(){ return m_observer;};
        shared_ptr<ledgerapp_gen::ThreadDispatcher> getThreadDispatcher(){return m_thread_dispatcher;};

    private:

        void _notify_new_data();

        shared_ptr<ledgerapp::Http> m_http;
        shared_ptr<ledgerapp_gen::TransactionListVmObserver> m_observer;
        shared_ptr<ledgerapp_gen::ThreadDispatcher> m_thread_dispatcher;
    };
}
#endif //LEDGERAPP_TRANSACTION_VM_H
