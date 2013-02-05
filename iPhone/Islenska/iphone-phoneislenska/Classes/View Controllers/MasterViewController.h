//
//  MasterViewController.h
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MasterViewController : UITableViewController <UITableViewDelegate, UITableViewDataSource, UISearchDisplayDelegate, UISearchBarDelegate>

@property (strong, nonatomic) NSMutableDictionary *names;
@property (strong, nonatomic) NSMutableArray *keys;
@property (strong, nonatomic) NSDictionary *allNames;
@property (strong, nonatomic) IBOutlet UISearchDisplayController *searchDisplayController;

- (id)initWithDefaultNibAndSearch:(BOOL)search;
- (void)resetSearch;
- (void)handleSearchForTerm:(NSString *)searchTerm;

@end
