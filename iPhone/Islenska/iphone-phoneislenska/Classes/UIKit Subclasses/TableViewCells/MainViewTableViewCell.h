
//
//  MainViewTableViewCell.h
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 6/13/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "PRPNibBasedTableViewCell.h"

@interface MainViewTableViewCell : PRPNibBasedTableViewCell
@property (strong, nonatomic) IBOutlet UILabel *dateLabel;
@property (strong, nonatomic) IBOutlet UILabel *textLabel;

@end
