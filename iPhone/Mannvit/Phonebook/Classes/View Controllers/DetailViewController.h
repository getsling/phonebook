//
//  DetailViewController.h
//  Phonebook
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import <UIKit/UIKit.h>
#define kNameValueTag           1
#define kTitleValueTag          2
#define kDivisionValueTag       3
#define kLargeImageValueTag     4


@interface DetailViewController : UITableViewController <UITableViewDelegate, UITableViewDataSource> 

@property (strong, nonatomic) IBOutlet UITableViewCell *tvCell;
@property (strong, nonatomic) id detailItem;
@property (strong, nonatomic) NSDictionary *names;
@property (strong, nonatomic) NSArray *keys;
@property (strong, nonatomic) NSArray *fieldLabels;
@property (strong, nonatomic) NSDictionary *keyDict;
@property (strong, nonatomic) NSArray *categories;

@end
