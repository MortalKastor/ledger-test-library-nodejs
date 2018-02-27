// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from view_model.djinni

#pragma once

#include <string>
#include <utility>

namespace ledgerapp_gen {

struct Response final {
    std::string error;
    std::string result;

    Response(std::string error_,
             std::string result_)
    : error(std::move(error_))
    , result(std::move(result_))
    {}
};

}  // namespace ledgerapp_gen