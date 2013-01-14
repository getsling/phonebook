//
//  MasterViewController.m
//  Phonebook
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "MasterViewController.h"
#import "DetailViewController.h"
#import "Phonebook.h"
#import "AppDelegate.h"
#import "NSDictionary-MutableDeepCopy.h"

@implementation MasterViewController
@synthesize tvCell = _tvCell;
@synthesize names = _names;
@synthesize keys = _keys;
@synthesize searchDisplayController = _mySearchDisplayController;
@synthesize allNames = _allNames;

#pragma mark - Custom Methods

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
        for (Phonebook *pbItem in array) {                                                  
            if ([pbItem.name rangeOfString:searchTerm options:NSCaseInsensitiveSearch].location != 0) {
                NSString *numberSearchTerm = [NSString stringWithFormat:@" %@", searchTerm];
                
                if ([pbItem.workPhone rangeOfString:numberSearchTerm options:NSLiteralSearch].location == NSNotFound) {
                    if ([pbItem.mobile rangeOfString:numberSearchTerm options:NSLiteralSearch].location == NSNotFound) {
                        [toRemove addObject:pbItem];
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

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        self.title = NSLocalizedString(@"Símaskrá", @"Símaskrá");
    }
    
    return self;
}

#pragma mark - View lifecycle

- (void)initFromDatabase {
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
	NSMutableArray *phonebook = appDelegate.employees;
    NSMutableDictionary *initDict = [[NSMutableDictionary alloc] init];
    
    for (Phonebook *phonebookItem in phonebook) {
        NSString *fl = [phonebookItem.name substringToIndex:1];
        
        NSMutableArray *letterArray = [initDict objectForKey:fl];
        
        if (letterArray != nil) {
            [letterArray addObject:phonebookItem];
        } else {
            letterArray = [NSMutableArray arrayWithObject:phonebookItem];
        }
        
        [initDict setObject:letterArray forKey:fl];
    }
    
    self.allNames = initDict;    
    [self resetSearch];
    [self.tableView reloadData];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self initFromDatabase];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(initFromDatabase) name:@"UpdateDatabaseNotification" object:nil];
    [self.tableView setContentOffset:CGPointMake(0.0, 44.0) animated:NO];
}

- (void)viewDidUnload {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"UpdateDatabaseNotification" object:nil];
    self.tvCell = nil;
    self.names = nil;
    self.keys = nil;
    self.allNames = nil;
    [self setSearchDisplayController:nil];
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
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
	label.shadowColor = [UIColor colorWithWhite:1.0 alpha:1];
	label.shadowOffset = CGSizeMake(0, 1);
	label.textColor = [UIColor colorWithRed:0.2 green:0.2 blue:0.2 alpha:1.000];
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
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	
    NSString *key = [self.keys objectAtIndex:section];
    NSArray *nameSection = [self.names objectForKey:key];
	Phonebook *currentPhoneBook = [nameSection objectAtIndex:row];
    
    static NSString *CellIdentifier = @"MainCellIdentifier";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if(cell == nil) {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"MainCell" owner:self options:nil];
        if (nib.count > 0) {
            cell = self.tvCell;
        } else {
            NSLog(@"Failed to load MainCell nib file");
        }
    }
    
    // Set up the cell
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    UIImageView *imageView = (UIImageView *)[cell viewWithTag:kImageValueTag];
    NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    NSString *imagePath = [[[[documentsPath stringByAppendingPathComponent:@"images"]stringByAppendingPathComponent:@"thumbs"]stringByAppendingPathComponent:currentPhoneBook.ID]stringByAppendingString:@".png"];
    UIImage *image = nil;
    if(!(image = [[UIImage alloc]initWithContentsOfFile:imagePath])) {
        image = [UIImage imageNamed:@"Icon.png"];
    }
    imageView.image = image;
    
    UILabel *nameLabel = (UILabel *)[cell viewWithTag:kNameValueTag];
    nameLabel.text = currentPhoneBook.name; 
    
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
	Phonebook *currentPhoneBook = [nameSection objectAtIndex:row];
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DetailViewController *dvc = [[DetailViewController alloc] initWithNibName:@"DetailViewController_iPhone" bundle:nil];
    dvc.detailItem = currentPhoneBook;
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
