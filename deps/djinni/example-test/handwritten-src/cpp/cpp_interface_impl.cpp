//
// Created by El Khalil Bellakrid on 23/02/2018.
//

#include "cpp_interface_impl.h"

namespace testapp {

    std::shared_ptr<CppInterface> createInstance(const std::string & version){
        return std::make_shared<CppInterfaceImpl>();
    }

    int32_t CppInterfaceImpl::getVersionFromNode(const std::shared_ptr<testapp::NodeInterface> & njs_interface){
        return njs_interface->getNodeVersion("1");
    }

    int32_t CppInterfaceImpl::getCppVersion(const std::string & version){
        return std::stoi(version);
    }
}


