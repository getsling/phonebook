//
//  MainViewController.h
//  Mannvit
//
//  Created by Hörður Hauksson on 7/2/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MainViewController : UIViewController
@property (strong, nonatomic) IBOutlet UIScrollView *newsScrollView;
- (IBAction)phonebookButtonPressed;
@end
