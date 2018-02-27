//
//  LGThreadDispatcherObjc.m
//  libledgerapp
//
//  Created by El Khalil Bellakrid on 05/02/2018.
//

#import "LGThreadDispatcherObjc.h"
#import "LGExecutionContextObjc.h"
#import "LGLockObjc.h"

@interface LGThreadDispatcherObjc()
@property(strong,nonatomic) NSMutableDictionary *contexts;
@end
@implementation LGThreadDispatcherObjc

-(instancetype)init{
    self = [super init];
    self.contexts = [[NSMutableDictionary alloc] init];
    return self;
}
- (nullable id<LGExecutionContext>)getMainExecutionContext {
    NSArray *values = [self.contexts allValues];
    if([values count] > 0){
        return [values objectAtIndex:0];
    }
    return nil;
}

- (nullable id<LGExecutionContext>)getSerialExecutionContext:(nonnull NSString *)name {
    //For the moment same as main
    id<LGExecutionContext> context = [self.contexts objectForKey:name];
    if(context){
        return context;
    }
    id<LGExecutionContext> newContext =  [[LGExecutionContextObjc alloc] init];
    [self.contexts setObject:newContext forKey:name];
    return newContext;
}

- (nullable id<LGExecutionContext>)getThreadPoolExecutionContext:(nonnull NSString *)name {
    //For the moment same as main
    return [self getSerialExecutionContext:name];
}

- (nullable id<LGLock>)newLock {
    return [[LGLockObjc alloc] init];
}

@end

