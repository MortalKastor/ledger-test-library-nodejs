//
//  LGLockObjc.m
//  libledgerapp
//
//  Created by El Khalil Bellakrid on 05/02/2018.
//

#import "LGLockObjc.h"

@interface LGLockObjc ()
@property (strong,nonatomic) NSLock *m_lock;
@end
@implementation LGLockObjc

-(instancetype)init {
    self = [super init];
    self.m_lock = [[NSLock alloc] init];
    return self;
}
- (void)lock {
    [self.m_lock lock];
}

- (BOOL)tryLock {
    return [self.m_lock tryLock];
}

- (void)unlock {
    [self.m_lock unlock];
}

@end
