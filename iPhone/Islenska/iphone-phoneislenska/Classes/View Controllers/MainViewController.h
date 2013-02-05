//
//  MainViewController.h
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 7/2/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MainViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>
@property (strong, nonatomic) IBOutlet UIScrollView *newsScrollView;
@property (strong, nonatomic) IBOutlet UITableView *twitterTableView;
@property (strong, nonatomic) IBOutlet UIButton *searchButton;
@property (strong, nonatomic) IBOutlet UIView *topHeaderView;
- (IBAction)phonebookButtonPressed;
@end
