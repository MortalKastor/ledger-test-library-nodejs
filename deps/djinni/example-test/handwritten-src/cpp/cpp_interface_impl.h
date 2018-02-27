//
// Created by El Khalil Bellakrid on 23/02/2018.
//

#ifndef LEDGER_TEST_LIBRARY_WITH_SUBMODULES_00_CPP_INTERFACE_IMPL_H
#define LEDGER_TEST_LIBRARY_WITH_SUBMODULES_00_CPP_INTERFACE_IMPL_H


#include <cstdint>
#include <memory>
#include <string>

#include "../../generated-src/cpp/cpp_interface.hpp"
#include "../../generated-src/cpp/node_interface.hpp"

namespace testapp {

    class CppInterfaceImpl : public CppInterface, public std::enable_shared_from_this<CppInterfaceImpl> {
    public:
        CppInterfaceImpl(){};
        ~CppInterfaceImpl() {};

        int32_t getCppVersion(const std::string & version);
        int32_t getVersionFromNode(const std::shared_ptr<NodeInterface> & njs_interface);
    };

}

#endif //LEDGER_TEST_LIBRARY_WITH_SUBMODULES_00_CPP_INTERFACE_IMPL_H
