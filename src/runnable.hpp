//
//  runnable.hpp
//  libledgerapp
//
//  Created by El Khalil Bellakrid on 05/02/2018.
//

#ifndef runnable_hpp
#define runnable_hpp

#include "stl.hpp"
#include "interface/runnable.hpp"

using namespace std;

namespace ledgerapp {

    class Runnable : public ledgerapp_gen::Runnable,
                     public enable_shared_from_this<Runnable> {

    public:

        using Task = function<void()>;
        Runnable(Task task)
        {
            m_task = task;
        };

        void run(){
            m_task();
        };

    private:
        Task m_task;
    };
}

#endif /* runnable_hpp */
