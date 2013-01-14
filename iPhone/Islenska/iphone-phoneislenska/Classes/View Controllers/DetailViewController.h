//
//  DetailViewController.h
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DetailViewController : UITableViewController <UITableViewDelegate, UITableViewDataSource> 

@property (strong, nonatomic) id detailItem;
@property (strong, nonatomic) NSDictionary *names;
@property (strong, nonatomic) NSArray *keys;
@property (strong, nonatomic) NSArray *fieldLabels;
@property (strong, nonatomic) NSDictionary *keyDict;
@property (strong, nonatomic) NSArray *categories;

@end
