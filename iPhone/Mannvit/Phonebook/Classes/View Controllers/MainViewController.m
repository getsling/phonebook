//
//  MainViewController.m
//  Mannvit
//
//  Created by Hörður Hauksson on 7/2/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "MainViewController.h"
#import "MasterViewController.h"
#import "WebBrowserViewController.h"

#define NEWS_WIDTH 300
#define NEWS_IMAGE_WIDTH 290
#define NEWS_TEXT_MARGIN 5

@interface MainViewController ()
@property (strong,nonatomic) NSArray *newsItems;
@end

@implementation MainViewController
@synthesize newsScrollView = _newsScrollView;
@synthesize newsItems = _newsItems;

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = @"Mannvit";
    // TODO: Temporary items here
    NSMutableArray *tempArray = [[NSMutableArray alloc]init];
    for(int i = 0; i<3; i++) {
        NSDictionary *temp = [NSDictionary dictionaryWithObjectsAndKeys:@"http://polk.uwex.edu/files/2011/08/AGbreaking-news1.jpg",@"picurl",@"Nýjar fréttir daglega",@"title",@"http://www.mannvit.is",@"newsurl", nil];
        [tempArray addObject:temp];
    }
    self.newsItems = [NSArray arrayWithArray:tempArray];
    [self setupNewsItems];
}

- (void)viewDidUnload
{
    [self setNewsScrollView:nil];
    [super viewDidUnload];
}

- (void)setupNewsItems {
    CGRect newsRect = self.newsScrollView.bounds;
    NSInteger textHeight = newsRect.size.height*0.3;

    [self.newsScrollView setContentSize:CGSizeMake(NEWS_WIDTH*[self.newsItems count], newsRect.size.height)];
    
    for (int i = 0; i < [self.newsItems count]; i++) {
        NSDictionary *item = [self.newsItems objectAtIndex:i];
        UIImageView *imageView =[[UIImageView alloc] initWithFrame:CGRectMake(i*NEWS_WIDTH, 0, NEWS_IMAGE_WIDTH,newsRect.size.height)];
        imageView.contentMode = UIViewContentModeScaleAspectFill;
        imageView.clipsToBounds = YES;
        imageView.userInteractionEnabled = YES;
        imageView.tag = i;
        NSData *image = [NSData dataWithContentsOfURL:[NSURL URLWithString:(NSString *)[item objectForKey:@"picurl"]]];
        [imageView setImage:[UIImage imageWithData:image]];
        
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(newsItemPressed:)];
        [imageView addGestureRecognizer:tap];
        
        [self.newsScrollView addSubview:imageView];
        
        UIView *view = [[UIView alloc] initWithFrame:CGRectMake(i*NEWS_WIDTH, newsRect.size.height-textHeight, NEWS_IMAGE_WIDTH, textHeight)];
        view.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.8];
        
        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(NEWS_TEXT_MARGIN, NEWS_TEXT_MARGIN, NEWS_IMAGE_WIDTH-(2*NEWS_TEXT_MARGIN), textHeight - (2*NEWS_TEXT_MARGIN))];
        label.text = [item objectForKey:@"title"];
        label.numberOfLines = 2;
        label.backgroundColor = [UIColor clearColor];
        label.textColor = [UIColor whiteColor];
        
        [view addSubview:label];
        
        [self.newsScrollView addSubview:view];
    }
}

- (void)newsItemPressed:(id)sender{
    NSInteger tag = [(UIGestureRecognizer *)sender view].tag;
    if(tag < [self.newsItems count]){
        NSDictionary *item = [self.newsItems objectAtIndex:tag];
        WebBrowserViewController *wvc = [[WebBrowserViewController alloc] initWithRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:[item objectForKey:@"newsurl"]]]];
        [self.navigationController presentModalViewController:wvc animated:YES];
    } else {
        NSLog(@"Stray item index %d",tag);
    }
}

- (IBAction)phonebookButtonPressed {
    MasterViewController *mvc = [[MasterViewController alloc]initWithNibName:@"MasterViewController_iPhone" bundle:nil];
    [self.navigationController pushViewController:mvc animated:YES];
}

@end
