#include <Foundation/Foundation.h>
#include "LGHttpObjc.h"
#include "gen/LGHttpCallback.h"
#include "gen/LGHttpHeader.h"
@implementation LGHttpObjc

- (void) get:(NSString *)urlString header:(NSArray *)header callback:(LGHttpCallback *)callback {

    NSURL *URL = [NSURL URLWithString:urlString];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:URL];
    
    NSLog(@"Http request with URL: %@", urlString);
    
    for(LGHttpHeader *currHeader in header){
        [request setValue:[currHeader value] forHTTPHeaderField:[currHeader field]];
    }

    [[[NSURLSession sharedSession] dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        if (error) {
            [callback onNetworkError];
        } else {
            int16_t httpCode = [(NSHTTPURLResponse*) response statusCode];
            NSString * strData = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
            [callback onSuccess:httpCode data: strData];
        }
    }] resume];
}

@end
