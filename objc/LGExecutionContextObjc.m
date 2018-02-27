//
//  LGExecutionContextObjc.m
//  libledgerapp
//
//  Created by El Khalil Bellakrid on 05/02/2018.
//
#import "LGExecutionContextObjc.h"
#include "LGLockObjc.h"
#import "gen/LGRunnable.h"

#define MSEC_PER_SEC 1000

@interface LGExecutionContextObjc()
@property(atomic) NSMutableDictionary *queue;
@property(atomic) LGLockObjc *lock;
@property(atomic) BOOL isRunning;
@end

/*
 For the moment we don't use the queue of contexts but should be done
 */
@implementation LGExecutionContextObjc

-(instancetype)init{
    self = [super init];
    if(self){
        self.queue = [[NSMutableDictionary alloc] init];
        self.lock = [[LGLockObjc alloc] init];
        self.isRunning = false;
    }
    return self;
}

-(void)execute:(LGRunnable *)runnable{
    dispatch_async(dispatch_get_main_queue(), ^{
        [runnable run];
    });
}

-(void)delay:(LGRunnable *)runnable millis:(int64_t)millis{
    [self.lock lock];
    [self appendRunnable:runnable withKey:[[NSNumber numberWithLong:millis] stringValue]];
    [self.lock unlock];
}

-(void)appendRunnable:(LGRunnable *)runnable withKey:(NSString *)key{
    NSMutableArray *runnables = [self.queue objectForKey:key];
    if([runnables count]){
        [runnables addObject:runnable];
    }else{
        NSMutableArray *newRunnables = [[NSMutableArray alloc] init];
        [newRunnables addObject:runnable];
        [self.queue setObject:newRunnables forKey:key];
    }
}

-(BOOL)IsContextRunning{
    [self.lock lock];
    BOOL localRunning = self.isRunning;
    [self.lock unlock];
    return localRunning;
}

-(void)waitUntilStopped{
    
    [self.lock lock];
    self.isRunning = YES;
    [self.lock unlock];
    
    while([self IsContextRunning]){
        if([self.queue count]){
            [self.lock lock];
            NSArray *keys = [self.queue allKeys];
            //Get delay and runnable
            for(NSString *key in keys){
                long millis = [key longLongValue];
                NSMutableArray *runnables = [self.queue objectForKey:key];
                for(LGRunnable *runnable in runnables){
                    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(millis/MSEC_PER_SEC));
                    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                        [runnable run];
                        //rodo ; remove element at 0
                        [self.lock unlock];
                    });
                }
                [self.queue removeObjectForKey:key];
            }
            [self.lock unlock];
        }else{
            [self.lock lock];
            self.isRunning = NO;
            [self.lock unlock];
        }
    }
}

@end
