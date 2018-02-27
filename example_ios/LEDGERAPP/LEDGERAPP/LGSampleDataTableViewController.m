#import "LGSampleDataTableViewController.h"
#import "gen/LGTransactionListVmCell.h"
//#import "gen/LGListChange.h"
#import "gen/LGTransactionListVm.h"
#import "gen/LGTransactionListVmHandle.h"

NSString *const CellIdentifier = @"LGCell";

@interface LGSampleDataTableViewController ()

@property (nonatomic) LGTransactionListVmHandle *handle;
@property (nonatomic) LGTransactionListVm *viewModel;
@end

@implementation LGSampleDataTableViewController

- (instancetype) initWithApi:(LGApi *)api {
    self = [super initWithStyle:UITableViewStylePlain];
    if (self) {
        self.handle = [api observerTransactionList];
        self.viewModel = nil;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.tableView registerClass:UITableViewCell.class forCellReuseIdentifier:CellIdentifier];
}

- (void)viewWillAppear:(BOOL)animated {
    NSArray *addresses = [[NSArray alloc] initWithObjects:self.address,nil];
    [self.handle start:self addresses:addresses];
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [self.handle stop];
    [super viewWillDisappear:animated];
}

- (void)onUpdate:(LGTransactionListVm *)newData {
    self.viewModel = newData;
    NSLog(@"Found %d transactions",[newData count]);
    [self.tableView reloadData];
}

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.viewModel != nil ? [self.viewModel count] : 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier
                                                            forIndexPath:indexPath];
    LGTransactionListVmCell * cellData = [self.viewModel getTransaction:(int32_t)indexPath.row];
    cell.textLabel.text = [cellData txHash];
    cell.detailTextLabel.text = [cellData receivedAt];
    
    return cell;
}

@end
