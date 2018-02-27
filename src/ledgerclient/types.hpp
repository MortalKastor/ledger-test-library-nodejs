#pragma once
#include "stl.hpp"

namespace ledgerclient {

    struct Tx {
        string hash;
        int64_t id;
        string received_at;
        string data;
    };

}

