//
//  ViewController.h
//  LEDGERAPP
//
//  Created by El Khalil Bellakrid on 16/01/2018.
//  Copyright © 2018 Ledger. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "gen/LGApi.h"

@interface ViewController : UIViewController

- (instancetype) initWithApi:(LGApi *) api;

@property (weak, nonatomic) IBOutlet UITextField *addressField;
- (IBAction)browseHistory:(id)sender;

@end

