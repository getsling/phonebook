//
//  MasterViewController.m
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "MasterViewController.h"
#import "DetailViewController.h"
#import "AppDelegate.h"
#import "NSDictionary-MutableDeepCopy.h"
#import "PhoneBookTableViewCell.h"
#import "UIImage+Resize.h"
#import "Employee+Methods.h"

@interface MasterViewController ()
@property (nonatomic) BOOL searchFromStart;
@property (nonatomic) BOOL spinnerIsOn;
@property (nonatomic,strong) UINib *phoneNib;
@property (nonatomic,strong) NSArray *allEmployees;
@end

@implementation MasterViewController
@synthesize spinnerIsOn = _spinnerIsOn;
@synthesize allEmployees = _allEmployees;
@synthesize searchFromStart = _searchFromStart;
@synthesize names = _names;
@synthesize keys = _keys;
@synthesize searchDisplayController = _mySearchDisplayController;
@synthesize allNames = _allNames;
@synthesize phoneNib = _phoneNib;

#pragma mark - Custom Methods

- (UINib *)phoneNib {
    if (!_phoneNib) _phoneNib = [PhoneBookTableViewCell nib];
    return _phoneNib;
}

-(void)resetSearch {
    NSMutableDictionary *allNamesCopy = [self.allNames mutableDeepCopy];
    self.names = allNamesCopy;
    NSMutableArray *keyArray = [[NSMutableArray alloc] init];
    [keyArray addObject:UITableViewIndexSearch];
    [keyArray addObjectsFromArray:[[self.allNames allKeys] sortedArrayUsingSelector:@selector(localizedCaseInsensitiveCompare:)]];
    self.keys = keyArray;
}

- (void)handleSearchForTerm:(NSString *)searchTerm {
    NSMutableArray *sectionsToRemove = [[NSMutableArray alloc] init];
    [self resetSearch];
	
    for (NSString *key in self.keys) {
        NSMutableArray *array = [self.names valueForKey:key];
        NSMutableArray *toRemove = [[NSMutableArray alloc] init];
        for (Employee *emp in array) {                                                  
            if ([emp.name rangeOfString:searchTerm options:NSCaseInsensitiveSearch].location != 0) {
                NSString *numberSearchTerm = [NSString stringWithFormat:@" %@", searchTerm];
                
                if ([emp.workphone rangeOfString:numberSearchTerm options:NSLiteralSearch].location == NSNotFound) {
                    if ([emp.mobilephone rangeOfString:numberSearchTerm options:NSLiteralSearch].location == NSNotFound) {
                        [toRemove addObject:emp];
                    }
                }
            }
        }
        
        if ([array count] == [toRemove count]) {
            [sectionsToRemove addObject:key];
        }
		
        [array removeObjectsInArray:toRemove];
    }
    
    [self.keys removeObjectsInArray:sectionsToRemove];
    [self.searchDisplayController.searchResultsTableView reloadData];
}

- (id)initWithDefaultNibAndSearch:(BOOL)search {
    self = [super initWithNibName:@"MasterViewController" bundle:nil];
    if(self) {
        self.searchFromStart = search;
    }
    return self;
}

-(void)downloadImagesFromWeb {
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        NSString *imagesPath = [documentsPath stringByAppendingPathComponent:@"images"];
        NSString *thumbPath = [imagesPath stringByAppendingPathComponent:@"thumbs"];
        [[NSFileManager defaultManager]createDirectoryAtPath:thumbPath withIntermediateDirectories:YES attributes:nil error:NULL];
        
        for(Employee *employee in self.allEmployees) {
            NSString *imageLocation = [[imagesPath stringByAppendingPathComponent:[employee.empID stringValue]]stringByAppendingString:@".png"];
            NSString *thumbLocation = [[thumbPath stringByAppendingPathComponent:[employee.empID stringValue]]stringByAppendingString:@".png"];
            if(![[NSFileManager defaultManager]fileExistsAtPath:imageLocation]) {
                NSData *imageData = [[NSData alloc]initWithContentsOfURL:[NSURL URLWithString:employee.imageUrl]];
                UIImage *image = nil;
                if((image = [UIImage imageWithData:imageData])) {
                    UIImage *thumbnail = [image thumbnailImage:43 transparentBorder:0 cornerRadius:5 interpolationQuality:kCGInterpolationHigh];
                    BOOL thumbWorked = [UIImagePNGRepresentation(thumbnail) writeToFile:thumbLocation atomically:YES];
                    BOOL writeWorked = [UIImagePNGRepresentation(image) writeToFile:imageLocation atomically:YES]; 
                    if(!writeWorked || !thumbWorked) {
                        NSLog(@"Failed to write to documents folder");
                    } else {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [self imageDownloadedWithEmployee:employee];  
                        });
                    }
                }  
            } 
        }
    });
}

#pragma mark - View lifecycle
- (void)setupWithCoreData {
    NSArray *result = [Employee MR_findAll];
    self.allEmployees = result;
    NSMutableDictionary *initDict = [[NSMutableDictionary alloc] init];
    
    for (Employee *employee in result) {
        NSString *fl = [employee.name substringToIndex:1];
        
        NSMutableArray *letterArray = [initDict objectForKey:fl];
        
        if (letterArray != nil) {
            [letterArray addObject:employee];
        } else {
            letterArray = [NSMutableArray arrayWithObject:employee];
        }
        
        [initDict setObject:letterArray forKey:fl];
    }
    
    self.allNames = initDict;    
    [self resetSearch];
    [self.tableView reloadData];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.title = NSLocalizedString(@"Símaskrá", @"Símaskrá");
    NSNumber *hasPhonebook = [[NSUserDefaults standardUserDefaults]objectForKey:@"hasPhonebook"];
    if(hasPhonebook == [NSNumber numberWithBool:NO]	) {
        [self toggleWaitingSpinner];
    } else {
        [self setupWithCoreData];
    }
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(databaseArrived) name:@"DatabaseDownloaded" object:nil];
    [self.tableView setContentOffset:CGPointMake(0.0, 44.0) animated:NO];
    if(self.searchFromStart) {
        [self.searchDisplayController.searchBar becomeFirstResponder];
    }
}

- (void)toggleWaitingSpinner {
    int spinnerTag = 99;
    if(!self.spinnerIsOn) {
        UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
        [spinner startAnimating];
        spinner.frame = CGRectMake(320/2-15, 480/2-15, 30, 30);
        [spinner setTag:spinnerTag];
        [self.view addSubview:spinner];

    } else {
        UIView *spinner = [self.view viewWithTag:spinnerTag];
        if(spinner) {
            [spinner removeFromSuperview];
        }
    }
    self.spinnerIsOn = !self.spinnerIsOn;
}

- (void)databaseArrived {
    [self setupWithCoreData];
    //[self toggleWaitingSpinner];
}

- (void)viewDidAppear:(BOOL)animated {
    if(self.searchFromStart) {
        [self.searchDisplayController.searchBar becomeFirstResponder];
        self.searchFromStart = NO;
    }
    [self downloadImagesFromWeb];
}

- (void)viewDidUnload {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"DatabaseDownloaded" object:nil];
    self.phoneNib = nil;
    self.names = nil;
    self.keys = nil;
    self.allEmployees = nil;
    self.allNames = nil;
    self.searchDisplayController = nil;
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

-(void) imageDownloadedWithEmployee:(Employee *)employee {
    NSString *firstLetter = [employee.name substringToIndex:1];
    NSUInteger section = [self.keys indexOfObject:firstLetter];
    NSArray *namesWithKey = [self.names objectForKey:firstLetter];
    NSUInteger row = [namesWithKey indexOfObject:employee];
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:row inSection:section];
    if([self.tableView.indexPathsForVisibleRows containsObject:indexPath]) {
        PhoneBookTableViewCell *cell = (PhoneBookTableViewCell *)[self.tableView cellForRowAtIndexPath:indexPath];
        UIImage *image = [self imageForCellWithID:[employee.empID stringValue]];
        cell.profileImageView.image = image;
    }
}

-(UIImage *)imageForCellWithID:(NSString *)ID {
    NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    NSString *imagePath = [[[[documentsPath stringByAppendingPathComponent:@"images"]stringByAppendingPathComponent:@"thumbs"]stringByAppendingPathComponent:ID]stringByAppendingString:@".png"];
    UIImage *image = nil;
    if(!(image = [[UIImage alloc]initWithContentsOfFile:imagePath])) {
        image = [UIImage imageNamed:@"Icon.png"];
    }
    return image;
}


- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    NSString *titleHeader = [self tableView:tableView titleForHeaderInSection:section];
    if(titleHeader == nil) {
        return nil;
    }
    UIView *sectionView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 320, 22)];
    [sectionView setBackgroundColor:[UIColor colorWithPatternImage:[UIImage imageNamed:@"tableHeader.png"]]];
    UILabel *label = [[UILabel alloc]initWithFrame:CGRectMake(20, 2, 300, 20)];   
    [label setBackgroundColor:[UIColor clearColor]];
    label.text = titleHeader;
    label.font = [UIFont boldSystemFontOfSize:17];
	label.shadowColor = [UIColor colorWithWhite:0.4 alpha:1];
	label.shadowOffset = CGSizeMake(0, 1);
	label.textColor = [UIColor colorWithRed:0.8 green:0.8 blue:0.8 alpha:1.000];
    [sectionView addSubview:label];
    return sectionView;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if([self tableView:tableView titleForHeaderInSection:section] == nil) {
        return 0.0;
    } else {
        return 22.0;
    }
}

// Customize the number of sections in the table view.
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return ([self.keys count] > 0) ? [self.keys count] : 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if ([self.keys count] == 0) {
        return 0;
    }
    
    NSString *key = [self.keys objectAtIndex:section];
    NSArray *nameSection = [self.names objectForKey:key];
    return nameSection.count;
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSUInteger section = indexPath.section;
    NSUInteger row = indexPath.row;
	
    NSString *key = [self.keys objectAtIndex:section];
    NSArray *nameSection = [self.names objectForKey:key];
	Employee *currentEmployee = [nameSection objectAtIndex:row];
    
    PhoneBookTableViewCell *cell = [PhoneBookTableViewCell cellForTableView:tableView fromNib:self.phoneNib];
    UIImage *image = [self imageForCellWithID:[currentEmployee.empID stringValue]];
    cell.profileImageView.image = image;
    cell.textLabel.text = currentEmployee.name;
    
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    if ([self.keys count] == 0) {
        return nil;
    }
    
    NSString *key = [self.keys objectAtIndex:section];
    if (key == UITableViewIndexSearch) {
        return nil;
    }
    
    return key;
}

- (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView {
    return self.keys;
}

#pragma mark - Table View Delegate Methods

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    NSString *key = [self.keys objectAtIndex:section];
    NSArray *nameSection = [self.names objectForKey:key];
	Employee *currentEmployee = [nameSection objectAtIndex:row];
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DetailViewController *dvc = [[DetailViewController alloc] initWithNibName:@"DetailViewController" bundle:nil];
    dvc.detailItem = currentEmployee;
    [self.navigationController pushViewController:dvc animated:YES];
}

#pragma mark - Search Bar Delegate Methods

- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar {
    NSString *searchTerm = [searchBar text];
    [self handleSearchForTerm:searchTerm];
    [searchBar resignFirstResponder];
}

- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchTerm {
    if ([searchTerm length] == 0) {
        [self resetSearch];
        [self.tableView reloadData];
        return;
    }
    
    [self handleSearchForTerm:searchTerm];
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar {
    [self resetSearch];
    [self.tableView reloadData];
    [searchBar resignFirstResponder];
}

- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index {
    NSString *key = [self.keys objectAtIndex:index];
    if (key == UITableViewIndexSearch) {
        [tableView setContentOffset:CGPointZero animated:NO];
        return NSNotFound;
    }
    
    else return index;
}



@end
