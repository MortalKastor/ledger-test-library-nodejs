//
//  ViewController.m
//  LEDGERAPP
//
//  Created by El Khalil Bellakrid on 16/01/2018.
//  Copyright © 2018 Ledger. All rights reserved.
//

#import "ViewController.h"
#import "LGSampleDataTableViewController.h"
#import "AppDelegate.h"

@interface ViewController ()
@property (nonatomic) LGApi *api;
@end

@implementation ViewController

- (instancetype) initWithApi:(LGApi *)api {
    self = [super init];
    if (self) {
        self.api = api;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Do any additional setup after loading the view, typically from a nib.
    if(!self.api){
        AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
        self.api = appDelegate.api;
    }
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (IBAction)browseHistory:(id)sender {
    LGSampleDataTableViewController *sampleViewController = [[LGSampleDataTableViewController alloc] initWithApi:self.api];
    if(sampleViewController){
        sampleViewController.address = _addressField.text;
        [self presentViewController:sampleViewController animated:YES completion:nil];
    }
    
}
@end
