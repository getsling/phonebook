//
//  MainViewController.m
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 7/2/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "MainViewController.h"
#import "MasterViewController.h"
#import "WebBrowserViewController.h"
#import "RSSParser.h"
#import "RSSItem.h"
#import "UIImageView+WebCache.h"
#import "AFJSONRequestOperation.h"
#import "MainViewTableViewCell.h"
#import "Config.h"

#define NEWS_WIDTH 300
#define NEWS_IMAGE_WIDTH 290
#define NEWS_TEXT_MARGIN 5

@interface MainViewController ()
@property (strong,nonatomic) NSArray *newsItems;
@property (strong, nonatomic) NSArray *twitterItems;
@property (strong, nonatomic) UINib *twitterNib;
@property (strong, nonatomic) NSDateFormatter *formatterFromDate;
@property (strong, nonatomic) NSDateFormatter *formatterToDate;
@end

@implementation MainViewController
@synthesize formatterFromDate = _formatterFromDate;
@synthesize formatterToDate = _formatterToDate;
@synthesize twitterItems = _twitterItems;
@synthesize newsScrollView = _newsScrollView;
@synthesize twitterTableView = _twitterTableView;
@synthesize searchButton = _searchButton;
@synthesize topHeaderView = _topHeaderView;
@synthesize newsItems = _newsItems;
@synthesize twitterNib = _twitterNib;

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = @"Íslenska Auglýsingastofan";
    self.searchButton.titleLabel.alpha = 0.7;
    self.formatterFromDate = [[NSDateFormatter alloc]init];
    [self.formatterFromDate setDateFormat:@"MMM d"];
    self.formatterToDate = [[NSDateFormatter alloc]init];
    [self.formatterToDate setDateFormat:@"EE MMM dd HH:mm:ss Z yyyy"];

    AFJSONRequestOperation *twitterRequest = [[AFJSONRequestOperation alloc]initWithRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:[Config config].twitterFeed]]];
    [twitterRequest setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        [self setupTwitterItems:responseObject];
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Failed to load twitter feed with error %@", error);
    }];
    [twitterRequest start];
    
    NSURLRequest *newsRequest = [NSURLRequest requestWithURL:[NSURL URLWithString:[Config config].tumblrFeed]];
    [RSSParser parseRSSFeedForRequest:newsRequest success:^(NSArray *feedItems) {
        self.newsItems = feedItems;
        //do this on the main thread
        [self setupNewsItems];
    } failure:^(NSError *error) {
        NSLog(@"News Items failed %@",error);
    } ];
}

- (void)viewDidUnload
{
    [self setNewsScrollView:nil];
    [self setTwitterTableView:nil];
    [self setTopHeaderView:nil];
    [self setFormatterFromDate:nil];
    [self setFormatterToDate:nil];
    [self setTwitterItems:nil];
    [self setNewsItems:nil];
    [self setTwitterNib:nil];
    [self setSearchButton:nil];
    [super viewDidUnload];
}

- (UINib *)twitterNib {
    if (!_twitterNib) _twitterNib = [MainViewTableViewCell nib];
    return _twitterNib;
}

- (void)setupTwitterItems:(id)response {
    self.twitterItems = response;
    [self.twitterTableView reloadData];
}

- (void)setupNewsItems {
    CGRect newsRect = self.newsScrollView.bounds;
    NSInteger textHeight = newsRect.size.height*0.3;
    
    [self.newsScrollView setContentSize:CGSizeMake(NEWS_WIDTH*[self.newsItems count], newsRect.size.height)];
    
    for (int i = 0; i < [self.newsItems count]; i++) {
        RSSItem *item = [self.newsItems objectAtIndex:i];
        UIImageView *imageView =[[UIImageView alloc] initWithFrame:CGRectMake(i*NEWS_WIDTH, 0, NEWS_IMAGE_WIDTH,newsRect.size.height)];
        imageView.contentMode = UIViewContentModeScaleAspectFill;
        imageView.clipsToBounds = YES;
        imageView.userInteractionEnabled = YES;
        imageView.tag = i;

        NSString *description = item.itemDescription;
        NSRange endRange = [description rangeOfString:@"\"/>"];
        NSString *imgUrl;
        if(endRange.length != 0) {
            imgUrl = [description substringWithRange:NSMakeRange(10, endRange.location-10)];
        }
        [imageView setImageWithURL:[NSURL URLWithString:imgUrl] placeholderImage:[UIImage imageNamed:@"Icon"]];

        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(newsItemPressed:)];
        [imageView addGestureRecognizer:tap];
        
        [self.newsScrollView addSubview:imageView];
        
        UIView *view = [[UIView alloc] initWithFrame:CGRectMake(i*NEWS_WIDTH, newsRect.size.height-textHeight, NEWS_IMAGE_WIDTH, textHeight)];
        view.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.8];
        
        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(NEWS_TEXT_MARGIN, NEWS_TEXT_MARGIN, NEWS_IMAGE_WIDTH-(2*NEWS_TEXT_MARGIN), textHeight - (2*NEWS_TEXT_MARGIN))];
        label.text = item.title;
        label.numberOfLines = 2;
        label.backgroundColor = [UIColor clearColor];
        label.textColor = [UIColor whiteColor];
        
        [view addSubview:label];
        
        [self.newsScrollView addSubview:view];
    }
}

# pragma mark - Table view
- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    if(section == 0) {
        return self.topHeaderView;
    } else {
        return nil;
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 0) {
        return 0;
    }
    return [self.twitterItems count];
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    MainViewTableViewCell *cell = [MainViewTableViewCell cellForTableView:self.twitterTableView fromNib:self.twitterNib];
    
    NSDictionary *item = [self.twitterItems objectAtIndex:indexPath.row];
    NSDate *date = [self.formatterToDate dateFromString:[item objectForKey:@"created_at"]]; 
    NSString *dateString = [self.formatterFromDate stringFromDate:date];
    NSString *tweetText = [item objectForKey:@"text"];
    [cell.textLabel setFrame:CGRectMake(cell.textLabel.frame.origin.x,cell.textLabel.frame.origin.y, cell.textLabel.frame.size.width, [self calculateLabelHeightWithWidth:280.0f text:tweetText font:cell.textLabel.font])];

    cell.dateLabel.text = dateString;
    cell.textLabel.text = tweetText;
    return cell;
}

- (float)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSDictionary *item = [self.twitterItems objectAtIndex:indexPath.row];
    
    NSString *cellText = [item objectForKey:@"text"];
    UIFont *cellFont = [UIFont systemFontOfSize:16.0];
    return [self calculateLabelHeightWithWidth:280.0f text:cellText font:cellFont] +20;
}

-(float)calculateLabelHeightWithWidth:(float)width text:(NSString *)text font:(UIFont *)font {
    CGSize constraintSize = CGSizeMake(width, MAXFLOAT);
    CGSize labelSize = [text sizeWithFont:font constrainedToSize:constraintSize lineBreakMode:UILineBreakModeWordWrap];
    return labelSize.height;
}

- (float)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        return self.topHeaderView.bounds.size.height;
    } else {
        return 0;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    NSDictionary *item = [self.twitterItems objectAtIndex:indexPath.row];
    WebBrowserViewController *wvc = [[WebBrowserViewController alloc] initWithRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:[NSString stringWithFormat:@"https://twitter.com/metrolyrics/status/%@",[item objectForKey:@"id_str"]]]]];
    [self.navigationController presentModalViewController:wvc animated:YES];
}

# pragma mark - Buttons pressed

- (void)newsItemPressed:(id)sender{
    NSInteger tag = [(UIGestureRecognizer *)sender view].tag;
    if(tag < [self.newsItems count]){
        RSSItem *item = [self.newsItems objectAtIndex:tag];
        WebBrowserViewController *wvc = [[WebBrowserViewController alloc] initWithRequest:[NSURLRequest requestWithURL:item.link]];
        [self.navigationController presentModalViewController:wvc animated:YES];
    } else {
        NSLog(@"Stray item index %d",tag);
    }
}
- (IBAction)searchFieldSelected:(id)sender {
    MasterViewController *mvc = [[MasterViewController alloc]initWithDefaultNibAndSearch:YES];
    [self.navigationController pushViewController:mvc animated:YES];
}

- (IBAction)phonebookButtonPressed {
    MasterViewController *mvc = [[MasterViewController alloc]initWithDefaultNibAndSearch:NO];
    [self.navigationController pushViewController:mvc animated:YES];
}

@end
