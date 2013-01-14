//
//  DetailViewController.m
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "DetailViewController.h"
#import "Employee.h"
#import <QuartzCore/QuartzCore.h>
#import <MessageUI/MFMailComposeViewController.h>
#import "InfoTableViewCell.h"

@interface DetailViewController ()
@property (nonatomic,strong) UINib *employeeNib;
@end

@implementation DetailViewController
@synthesize detailItem = _detailItem;
@synthesize names = _names;
@synthesize keys = _keys;
@synthesize fieldLabels = _fieldLabels;
@synthesize keyDict = _keyDict;
@synthesize categories = _categories;
@synthesize employeeNib = _employeeNib;

#pragma mark - View lifecycle

- (void)viewDidLoad {    
    [super viewDidLoad];
    self.title = NSLocalizedString(@"Upplýsingar", @"Upplýsingar");
    [self initializeMatrix];
    [self.tableView setBackgroundColor:[UIColor colorWithWhite:0.1 alpha:1]];
}

- (void)viewDidUnload {
    self.detailItem = nil;
    self.names = nil;
    self.keys = nil;
    self.fieldLabels = nil;
    self.keyDict = nil;
    self.categories = nil;
    self.employeeNib = nil;
    [super viewDidUnload];
}

- (UINib *)employeeNib {
    if (!_employeeNib) _employeeNib = [InfoTableViewCell nib];
    return _employeeNib;
}


#pragma mark - Initializing and managing the table

-(void) initializeMatrix {
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
        Employee *employee = (Employee *)_detailItem;
        NSString *cellText = employee.workplace;
        
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
    Employee *employee = (Employee *) _detailItem;
    
    if([key isEqualToString: [self.categories objectAtIndex:0]]) {
        InfoTableViewCell *cell = [InfoTableViewCell cellForTableView:tableView fromNib:self.employeeNib];        

        NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        NSString *imagePath = [[[documentsPath stringByAppendingPathComponent:@"images"]stringByAppendingPathComponent:[employee.empID stringValue]]stringByAppendingString:@".png"];
        UIImage *image = nil;
        if([[NSFileManager defaultManager]fileExistsAtPath:imagePath]) {
            image = [[UIImage alloc]initWithContentsOfFile:imagePath];
        } else {
            image = [UIImage imageNamed:@"Icon.png"];
        }
        
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
        
        cell.profileImageView.image = image;
        cell.profileImageView.layer.masksToBounds = YES;
        cell.profileImageView.layer.cornerRadius = 5.0;
        cell.profileImageView.bounds = CGRectMake(0,0,imgWidth,imgHeight);
        cell.profileImageView.backgroundColor = [UIColor whiteColor];
        
        cell.nameLabel.text = employee.name;
        CGFloat nameHeight = [self heightForText:employee.name];
        if(nameHeight > 41) {
            cell.nameLabel.frame = CGRectMake(cell.nameLabel.frame.origin.x, cell.nameLabel.frame.origin.y, 233, [self heightForText:employee.name]);
            cell.titleLabel.frame = CGRectMake(cell.titleLabel.frame.origin.x, cell.titleLabel.frame.origin.y+nameHeight-21, cell.titleLabel.frame.size.width, cell.titleLabel.frame.size.height);
            cell.divisionLabel.hidden = YES;
        }
        cell.titleLabel.text = employee.title;
        cell.divisionLabel.text = employee.division;
        cell.backgroundColor = [UIColor clearColor];
        cell.backgroundView = [[UIView alloc] initWithFrame:CGRectZero];
        return cell;
    } else {
        UITableViewCell *cell = [[UITableViewCell alloc] init];
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
        return cell;
    }
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
        [self initializeMatrix];
        _detailItem = newDetailItem;
        
        NSArray *array = [[NSArray alloc] initWithObjects:@"nafn", @"starfsheiti",@"póstur", @"vinnusími",@"farsími",@"staður",@"deild", nil];
        self.fieldLabels = array;
        
        NSMutableDictionary *detailedDict = [[NSMutableDictionary alloc] init];    
        Employee *employee = (Employee *) _detailItem;
        
        NSMutableDictionary *infoDict = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *phoneDict = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *emailDict = [[NSMutableDictionary alloc] init];
        NSMutableDictionary *addressDict = [[NSMutableDictionary alloc] init];
        NSMutableArray *infoArray = [[NSMutableArray alloc] init];
        NSMutableArray *phoneArray = [[NSMutableArray alloc] init];
        NSMutableArray *emailArray = [[NSMutableArray alloc] init];
        NSMutableArray *addressArray = [[NSMutableArray alloc] init];
        
        if(employee.name.length != 0) {
            [infoDict setObject:employee.name forKey:[self.fieldLabels objectAtIndex:0]]; 
            [infoArray addObject:[self.fieldLabels objectAtIndex:0]];
        }
        if(employee.title.length != 0) {
            [infoDict setObject:employee.title forKey:[self.fieldLabels objectAtIndex:1]]; 
            [infoArray addObject:[self.fieldLabels objectAtIndex:1]];
        }
        if(employee.division.length != 0) {        
            [infoDict setObject:employee.division forKey:[self.fieldLabels objectAtIndex:6]];
            [infoArray addObject:[self.fieldLabels objectAtIndex:6]];
        }
        if(employee.workphone.length != 0) {        
            [phoneDict setObject:employee.workphone forKey:[self.fieldLabels objectAtIndex:3]];
            [phoneArray addObject:[self.fieldLabels objectAtIndex:3]];
        }
        if(employee.mobilephone.length != 0) {        
            [phoneDict setObject:employee.mobilephone forKey:[self.fieldLabels objectAtIndex:4]];
            [phoneArray addObject:[self.fieldLabels objectAtIndex:4]];
        }
        if(employee.email.length != 0) {        
            [emailDict setObject:employee.email  forKey:[self.fieldLabels objectAtIndex:2]];
            [emailArray addObject:[self.fieldLabels objectAtIndex:2]];
        }
        if(employee.workplace.length != 0) {        
            [addressDict setObject:employee.workplace forKey:[self.fieldLabels objectAtIndex:5]];
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
    }     
}

// Customize the number of sections in the table view.
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return self.keys.count;
}

@end
