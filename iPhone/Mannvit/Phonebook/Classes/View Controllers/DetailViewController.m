//
//  DetailViewController.m
//  Phonebook
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "DetailViewController.h"
#import "Phonebook.h"
#import <QuartzCore/QuartzCore.h>
#import <MessageUI/MFMailComposeViewController.h>

@interface DetailViewController ()
@property (strong, nonatomic) UIPopoverController *masterPopoverController;
- (void)configureView;
@end

@implementation DetailViewController

@synthesize detailItem = _detailItem;
@synthesize masterPopoverController = _masterPopoverController;
@synthesize names = _names;
@synthesize keys = _keys;
@synthesize fieldLabels = _fieldLabels;
@synthesize keyDict = _keyDict;
@synthesize categories = _categories;
@synthesize tvCell = _tvCell;

#pragma mark - Initializing and managing the table

-(void) InitializeMatrix {
    NSArray *tempArray = [[NSArray alloc] initWithObjects:@"info",@"phonenumbers",@"email",@"address", nil];
    self.categories = tempArray;
    
    NSArray *sortedKeysArray = [[NSArray alloc] initWithObjects:[self.categories objectAtIndex:0],[self.categories objectAtIndex:1],[self.categories objectAtIndex:2],[self.categories objectAtIndex:3], nil];
    self.keys = sortedKeysArray;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSString *key = [self.keys objectAtIndex:section];
    NSDictionary *nameSection = [self.names objectForKey:key];
    if([key isEqualToString:[self.categories objectAtIndex:0]]) {
        return 1;
    }
    return nameSection.count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSUInteger section = [indexPath section];
    NSString *key = [self.keys objectAtIndex:section];
    
    if([key isEqualToString: [self.categories objectAtIndex:0]]) {
        return 80;
    } else if([key isEqualToString: [self.categories objectAtIndex:3]]) {
        Phonebook *pbItem = (Phonebook *)_detailItem;
        NSString *cellText = pbItem.workPlace;
        
        UIFont *cellFont = [UIFont fontWithName:@"Helvetica" size:17.0];
        
        // get a constraint size - not sure how it works 
        CGSize constraintSize = CGSizeMake(280.0f, MAXFLOAT);
        
        // calculate a label size - takes parameters including the font, a constraint and a specification for line mode
        CGSize labelSize = [cellText sizeWithFont:cellFont constrainedToSize:constraintSize lineBreakMode:UILineBreakModeWordWrap];
        
        // give it a little extra height
        return labelSize.height + 20;
    } else {
        return 44;
    }
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	
    NSString *key = [self.keys objectAtIndex:section];
    NSDictionary *nameSection = [self.names objectForKey:key];
    NSArray *mainKeys = [self.keyDict objectForKey:key];
	NSString *currentPhoneBook = [nameSection objectForKey:[mainKeys objectAtIndex:row]];
    Phonebook *pbItem = (Phonebook *) _detailItem;
    UITableViewCell *cell = [[UITableViewCell alloc] init];
    
    if([key isEqualToString: [self.categories objectAtIndex:0]]) {
        static NSString *InfoCellIdentifier = @"InfoCellIdentifier";
        
        cell = [tableView dequeueReusableCellWithIdentifier:InfoCellIdentifier];
        if(cell == nil) {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"InfoCell" owner:self options:nil];
            if(nib.count > 0) {
                cell = self.tvCell;
            } else {
                NSLog(@"Failed to load InfoCell nib file");
            }
        }
        // Set up the cell
        NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        NSString *imagePath = [[[documentsPath stringByAppendingPathComponent:@"images"]stringByAppendingPathComponent:pbItem.ID]stringByAppendingString:@".png"];
        UIImage *image = nil;
        if([[NSFileManager defaultManager]fileExistsAtPath:imagePath]) {
            image = [[UIImage alloc]initWithContentsOfFile:imagePath];
        } else {
            image = [UIImage imageNamed:@"Icon.png"];
        }
        
        UILabel *nameLabel = (UILabel *)[cell viewWithTag:kNameValueTag];
        UILabel *titleLabel = (UILabel *)[cell viewWithTag:kTitleValueTag];
        UILabel *divisionLabel = (UILabel *)[cell viewWithTag:kDivisionValueTag];
        UIImageView *imageView = (UIImageView *)[cell viewWithTag:kLargeImageValueTag];
        
        float frameHeight = 80;
        float frameWidth = 80;
        float imgHeight = image.size.height;
        float imgWidth = image.size.width;
        float aspRatio = imgWidth/imgHeight;
        
        bool heightIsBigger = NO;
        bool tooSmall = NO;
        if(imgHeight<frameHeight && imgWidth<frameWidth) {
            tooSmall = YES;
            if(imgHeight<imgWidth)
                heightIsBigger = NO;
            else
                heightIsBigger = YES;
        }
        if(tooSmall && heightIsBigger) {
            imgHeight = frameHeight;
            imgWidth = imgHeight * aspRatio;
        } else if (tooSmall && !heightIsBigger) {
            imgWidth = frameWidth;
            imgHeight = imgWidth / aspRatio;
        }
        
        
        if(imgHeight > frameHeight) {
            imgHeight = frameHeight;
            imgWidth = imgHeight * aspRatio;
        }
        if(imgWidth > frameWidth) {
            imgWidth = frameWidth;
            imgHeight = imgWidth / aspRatio;
        }
        
        imageView.hidden = NO;
        imageView.image = image;
        imageView.layer.masksToBounds = YES;
        imageView.layer.cornerRadius = 5.0;
        imageView.bounds = CGRectMake(0,0,imgWidth,imgHeight);
        imageView.backgroundColor = [UIColor whiteColor];
        
        nameLabel.text = pbItem.name;
        CGFloat nameHeight = [self heightForText:pbItem.name];
        if(nameHeight > 41) {
            nameLabel.frame = CGRectMake(nameLabel.frame.origin.x, nameLabel.frame.origin.y, 233, [self heightForText:pbItem.name]);
            titleLabel.frame = CGRectMake(titleLabel.frame.origin.x, titleLabel.frame.origin.y+nameHeight-21, titleLabel.frame.size.width, titleLabel.frame.size.height);
            divisionLabel.hidden = YES;
        }
        titleLabel.text = pbItem.title;
        divisionLabel.text = pbItem.division;
        cell.backgroundColor = [UIColor clearColor];
        cell.backgroundView = [[UIView alloc] initWithFrame:CGRectZero];
        
    } else {
        static NSString *CellIdentifier = @"Cell";
        
        cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
        if (cell == nil) {
            cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:CellIdentifier];
        }
        
        // Configure the cell.
        cell.detailTextLabel.lineBreakMode = UILineBreakModeWordWrap;
        
        // 0 means any number of lines
        cell.detailTextLabel.numberOfLines = 0;
        
        cell.detailTextLabel.text = currentPhoneBook;
        cell.textLabel.text = [mainKeys objectAtIndex:row];
    }
    return cell;
}

- (CGFloat)heightForText:(NSString *)bodyText
{
    UIFont *cellFont = [UIFont boldSystemFontOfSize:17];
    CGSize constraintSize = CGSizeMake(233, MAXFLOAT);
    CGSize labelSize = [bodyText sizeWithFont:cellFont constrainedToSize:constraintSize lineBreakMode:UILineBreakModeWordWrap];
    CGFloat height = labelSize.height + 5;
    return height;    
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    UIDevice *device = [UIDevice currentDevice];
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    NSString *key = [self.keys objectAtIndex:section];
    NSDictionary *nameSection = [self.names objectForKey:key];
    NSArray *mainKeys = [self.keyDict objectForKey:key];
	
    if([key isEqualToString:[self.categories objectAtIndex:1]]) {
        NSString *currentPhoneNumber = [nameSection objectForKey:[mainKeys objectAtIndex:row]];
        currentPhoneNumber = [currentPhoneNumber stringByReplacingOccurrencesOfString:@" " withString:@"-"];
        NSString *callingNumber = [NSString stringWithFormat:@"tel:%@",currentPhoneNumber];
        if ([[device model] isEqualToString:@"iPhone"] ) {
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:callingNumber]];
        } else {
            UIAlertView *notpermitted =[[UIAlertView alloc] initWithTitle:@"Alert" message:@"Tækið þitt styður ekki þennan fítus!" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [notpermitted show];
        }
    }
    
    if ([key isEqualToString:[self.categories objectAtIndex:2]]) {
        NSString *currentEmail = [nameSection objectForKey:[mainKeys objectAtIndex:row]];
        NSString *emailFormat = [NSString stringWithFormat:@"mailto:%@",currentEmail];
        
        [[UIApplication sharedApplication] openURL: [NSURL URLWithString:emailFormat]];
    }
    
    if ([key isEqualToString:[self.categories objectAtIndex:3]]) {
        NSString *currentWorkplace = [nameSection objectForKey:[mainKeys objectAtIndex:row]];
        NSError *error = NULL;
        NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:@"[0-9]" options:NSRegularExpressionCaseInsensitive error:&error];
        
        currentWorkplace = [regex stringByReplacingMatchesInString:currentWorkplace options:0 range:NSMakeRange(0, [currentWorkplace length]) withTemplate:@""];
        NSString *addString = @"";
        if([currentWorkplace isEqualToString:@"Grensásvegur "]) {
            addString = @"reykjavik,";
        }
        currentWorkplace = [currentWorkplace stringByAddingPercentEscapesUsingEncoding:NSISOLatin1StringEncoding];
        
        NSString *mapsFormat = [NSString stringWithFormat:@"http://maps.google.com/maps?q=%@&near=%@iceland", currentWorkplace,addString];
        [[UIApplication sharedApplication] openURL: [NSURL URLWithString:mapsFormat]];
    }
    
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    return nil;
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    return nil;
}

#pragma mark - Managing the detail item

- (void)setDetailItem:(id)newDetailItem {
    if (_detailItem != newDetailItem) {
        [self InitializeMatrix];
        _detailItem = newDetailItem;
        
        NSArray *array = [[NSArray alloc] initWithObjects:@"nafn", @"starfsheiti",@"póstur", @"vinnusími",@"farsími",@"staður",@"deild", nil];
        self.fieldLabels = array;
        
        NSMutableDictionary *detailedDict = [[NSMutableDictionary alloc] init];    
        Phonebook *pbItem = (Phonebook *) _detailItem;
        
        NSMutableDictionary *infoDict = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *phoneDict = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *emailDict = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *addressDict = [[NSMutableDictionary alloc] init];
        NSMutableArray *infoArray = [[NSMutableArray alloc] init];
        NSMutableArray *phoneArray = [[NSMutableArray alloc] init];
        NSMutableArray *emailArray = [[NSMutableArray alloc] init];
        NSMutableArray *addressArray = [[NSMutableArray alloc] init];
        
        if(pbItem.name.length != 0) {
            [infoDict setObject:pbItem.name forKey:[self.fieldLabels objectAtIndex:0]]; 
            [infoArray addObject:[self.fieldLabels objectAtIndex:0]];
        }
        if(pbItem.title.length != 0) {
            [infoDict setObject:pbItem.title forKey:[self.fieldLabels objectAtIndex:1]]; 
            [infoArray addObject:[self.fieldLabels objectAtIndex:1]];
        }
        if(pbItem.division.length != 0) {        
            [infoDict setObject:pbItem.division forKey:[self.fieldLabels objectAtIndex:6]];
            [infoArray addObject:[self.fieldLabels objectAtIndex:6]];
        }
        if(pbItem.workPhone.length != 0) {        
            [phoneDict setObject:pbItem.workPhone forKey:[self.fieldLabels objectAtIndex:3]];
            [phoneArray addObject:[self.fieldLabels objectAtIndex:3]];
        }
        if(pbItem.mobile.length != 0) {        
            [phoneDict setObject:pbItem.mobile forKey:[self.fieldLabels objectAtIndex:4]];
            [phoneArray addObject:[self.fieldLabels objectAtIndex:4]];
        }
        if(pbItem.email.length != 0) {        
            [emailDict setObject:pbItem.email  forKey:[self.fieldLabels objectAtIndex:2]];
            [emailArray addObject:[self.fieldLabels objectAtIndex:2]];
        }
        if(pbItem.workPlace.length != 0) {        
            [addressDict setObject:pbItem.workPlace forKey:[self.fieldLabels objectAtIndex:5]];
            [addressArray addObject:[self.fieldLabels objectAtIndex:5]];
        }
        
        if(infoDict.count != 0) {
            [detailedDict setObject:infoDict forKey:[self.categories objectAtIndex:0]];
        }
        if(phoneDict.count != 0) {
            [detailedDict setObject:phoneDict forKey:[self.categories objectAtIndex:1]];
        }
        if(emailDict.count != 0) {
            [detailedDict setObject:emailDict forKey:[self.categories objectAtIndex:2]];
        }
        if(addressDict.count != 0) {
            [detailedDict setObject:addressDict forKey:[self.categories objectAtIndex:3]];
        }
        
        NSMutableDictionary *tempDict = [[NSMutableDictionary alloc] init];
        [tempDict setObject:infoArray forKey:[self.categories objectAtIndex:0]];
        [tempDict setObject:phoneArray forKey:[self.categories objectAtIndex:1]];
        [tempDict setObject:emailArray forKey:[self.categories objectAtIndex:2]];
        [tempDict setObject:addressArray forKey:[self.categories objectAtIndex:3]];
        self.keyDict = tempDict;
        
        self.names = detailedDict;
        [self.tableView reloadData];
        
        // Update the view.
        [self configureView];
    }
    
    if (self.masterPopoverController != nil) {
        [self.masterPopoverController dismissPopoverAnimated:YES];
    }        
}

// Customize the number of sections in the table view.
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return self.keys.count;
}

- (void)configureView {
    // Update the user interface for the detail item.
    if (!self.detailItem) {
        _detailItem = [[Phonebook alloc] initWithName:@"Veldu nafn" ID:@"" workPhone:@"" mobile:@"" title:@"" email:@"" imageUrl:@"" workPlace:@"" division:@""];
        [self InitializeMatrix];
    }
}

#pragma mark - View lifecycle

- (void)viewDidLoad {    
    [super viewDidLoad];
    self.title = NSLocalizedString(@"Upplýsingar", @"Upplýsingar");
    [self configureView];
}

- (void)viewDidUnload {
    self.tvCell = nil;
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

@end
