//
//  FullArticleViewController.m
//  metrolyrics
//
//  Created by Hörður Hauksson on 6/11/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "WebBrowserViewController.h"

@interface WebBrowserViewController ()
@property (strong, nonatomic) NSURLRequest *request;
- (void)actualizeButtons;
@end


@implementation WebBrowserViewController

@synthesize webView = _webView;
@synthesize request = _request;
@synthesize backButton = _backButton;
@synthesize forwardButton = _forwardButton;

-(id)initWithRequest:(NSURLRequest *)request {
    self = [super initWithNibName:@"WebBrowserViewController" bundle:nil];
    if (self) {
        self.request = request;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.webView loadRequest:self.request];
	[UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    [self actualizeButtons];
}

- (void)viewDidUnload {
    [self setWebView:nil];
    [self setBackButton:nil];
    [self setForwardButton:nil];
    [super viewDidUnload];
}

- (void)viewWillDisappear:(BOOL)animated {
    [self.webView stopLoading];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    [super viewWillDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    [self actualizeButtons];
	[UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
}

- (IBAction)closeButtonPressed:(id)sender {
    [self dismissModalViewControllerAnimated:YES];
}

- (IBAction)actionButtonPressed:(id)sender {
    [[UIApplication sharedApplication] openURL:self.webView.request.URL];
}

- (void)actualizeButtons {
    self.backButton.enabled = [self.webView canGoBack];
    self.forwardButton.enabled = [self.webView canGoForward];
}

@end
