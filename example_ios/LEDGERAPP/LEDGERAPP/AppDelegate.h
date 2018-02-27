//
//  AppDelegate.h
//  LEDGERAPP
//
//  Created by El Khalil Bellakrid on 16/01/2018.
//  Copyright © 2018 Ledger. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "gen/LGApi.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (strong, nonatomic) LGApi *api;

@end

