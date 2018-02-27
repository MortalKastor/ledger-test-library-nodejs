//
//  LGExecutionContextObjc.h
//  libledgerapp
//
//  Created by El Khalil Bellakrid on 05/02/2018.
//

#import <Foundation/Foundation.h>
#import "gen/LGExecutionContext.h"

@interface LGExecutionContextObjc : NSObject <LGExecutionContext>
-(BOOL)IsContextRunning;
-(void)waitUntilStopped;
@end
