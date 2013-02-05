//
//  FullArticleViewController.h
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 6/11/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface WebBrowserViewController : UIViewController <UIWebViewDelegate>

@property (strong, nonatomic) IBOutlet UIWebView *webView;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *backButton;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *forwardButton;

- (id)initWithRequest:(NSURLRequest *)request;
- (IBAction)closeButtonPressed:(id)sender;
- (IBAction)actionButtonPressed:(id)sender;

@end
