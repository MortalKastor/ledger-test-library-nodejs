#import <UIKit/UIKit.h>
#import "gen/LGApi.h"
#import "gen/LGTransactionListVmObserver.h"

@interface LGSampleDataTableViewController : UITableViewController <LGTransactionListVmObserver>
@property (nonatomic) NSString *address;
- (instancetype) initWithApi:(LGApi *) api;
- (void)onUpdate:(LGTransactionListVm *)newData;
@end
