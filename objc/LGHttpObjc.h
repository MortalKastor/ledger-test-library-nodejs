#pragma once
#include "gen/LGHttp.h"

@interface LGHttpObjc : NSObject <LGHttp>

- (void)get:(NSString *)url header:(NSArray *)header callback:(LGHttpCallback *)callback;

@end
