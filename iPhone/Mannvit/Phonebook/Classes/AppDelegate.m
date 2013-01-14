//
//  AppDelegate.m
//  Phonebook
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "AppDelegate.h"
#import "MainViewController.h"
#import "Phonebook.h"
#import "Constants.h"
#import "UIImage+Resize.h"

@interface AppDelegate()
@property (strong, nonatomic) NSString *databasePath;
@end

@implementation AppDelegate
@synthesize databasePath = _databasePath;
@synthesize window = _window;
@synthesize navigationController = _navigationController;
@synthesize employees = _employees;

- (void)checkForNewDatabase:(NSString *)dataFile {
    NSFileManager *filemgr = [NSFileManager defaultManager];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSDate *lastChecked = [defaults objectForKey:@"lastChecked"];
    NSTimeInterval timeSinceLastCheck = -[lastChecked timeIntervalSinceNow];
    int checkInterval = 7*60*60*24; // 60*60*24*7
    BOOL downloadFromServer = NO; 
    
    if (lastChecked == nil || (int)timeSinceLastCheck > checkInterval) {
        if ([filemgr fileExistsAtPath:dataFile]) {
            [defaults setObject:[NSDate date] forKey:@"lastChecked"];
            [defaults synchronize];
            NSDictionary *fileInfo = [filemgr attributesOfItemAtPath:dataFile error:nil];
            NSDate *currentDBDate = [fileInfo objectForKey:NSFileModificationDate];
            
            NSString *urlString = @"http://phonebook.gangverk.is/db/mannvit.sqlite";
            NSLog(@"Downloading HTTP header from: %@", urlString);
            NSURL *url = [NSURL URLWithString:urlString];
            
            //NSString *cachedPath = [dataFile stringByReplacingOccurrencesOfString:@"mannvit_staff.sqlite" withString:@"index.html"];
            NSString *cachedPath = dataFile;
            
            NSString *lastModifiedString = nil;
            NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
            [request setHTTPMethod:@"HEAD"];
            NSHTTPURLResponse *response;
            
            [NSURLConnection sendSynchronousRequest:request returningResponse:&response error: NULL];
            if ([response respondsToSelector:@selector(allHeaderFields)]) {
                lastModifiedString = [[response allHeaderFields] objectForKey:@"Last-Modified"];
            }
            
            NSDate *lastModifiedServer = nil;
            @try {
                NSDateFormatter *df = [[NSDateFormatter alloc] init];
                df.dateFormat = @"EEE',' dd MMM yyyy HH':'mm':'ss 'GMT'";
                df.locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
                df.timeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
                lastModifiedServer = [df dateFromString:lastModifiedString];
            }
            @catch (NSException * e) {
                NSLog(@"Error parsing last modified date: %@ - %@", lastModifiedString, [e description]);
            }
            
            NSLog(@"lastModifiedServer: %@", lastModifiedServer);
            
            // Download file from server if the server modified timestamp is later than the local modified timestamp
            if ([currentDBDate laterDate:lastModifiedServer] == lastModifiedServer) {
                downloadFromServer = YES;
            }
            
            if (downloadFromServer) {
                NSLog(@"Downloading new file from server");
                NSData *data = [NSData dataWithContentsOfURL:url];
                if (data) {
                    // Save the data
                    if ([data writeToFile:cachedPath atomically:YES]) {
                        NSLog(@"Downloaded file saved to: %@", cachedPath);
                    }
                    
                    // Set the file modification date to the timestamp from the server
                    if (lastModifiedServer) {
                        NSDictionary *fileAttributes = [NSDictionary dictionaryWithObject:lastModifiedServer forKey:NSFileModificationDate];
                        NSError *error = nil;
                        if ([filemgr setAttributes:fileAttributes ofItemAtPath:cachedPath error:&error]) {
                            NSLog(@"File modification date updated");
                        }
                        if (error) {
                            NSLog(@"Error setting file attributes for: %@ - %@", cachedPath, [error localizedDescription]);
                        }
                    }
                }
            }
        }
    }
    
    if (downloadFromServer) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self readEmployeesFromDatabase];
            [[NSNotificationCenter defaultCenter] postNotificationName:@"UpdateDatabaseNotification" object:nil];
        });
    }
}

-(void) InitializeDatabase {   
	// Get the path to the documents directory and append the databaseName
	NSArray *documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDir = [documentPaths objectAtIndex:0];
	self.databasePath = [documentsDir stringByAppendingPathComponent:DatabaseName];
    
    dispatch_async(dispatch_get_global_queue(0,0), ^{
        [self checkForNewDatabase:self.databasePath]; 
    });
    
	[self checkAndCreateDatabase];
	[self readEmployeesFromDatabase];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [self InitializeDatabase];
    [self downloadImagesFromWeb];
    
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    MainViewController *mvc = [[MainViewController alloc]initWithNibName:@"MainViewController" bundle:nil];
    self.navigationController = [[UINavigationController alloc] initWithRootViewController:mvc];
    self.window.rootViewController = self.navigationController;
    
    UINavigationBar *navBar = [self.navigationController navigationBar];
    if ([navBar respondsToSelector:@selector(setBackgroundImage:forBarMetrics:)]) {
        [navBar setBackgroundImage:[UIImage imageNamed:@"bg_nav"] forBarMetrics:UIBarMetricsDefault];
    }
    [self.window makeKeyAndVisible];
    return YES;
}

-(void) checkAndCreateDatabase{
	BOOL success;  
	NSFileManager *fileManager = [NSFileManager defaultManager];
    
	// Check if the database has already been created in the users filesystem
	success = [fileManager fileExistsAtPath:self.databasePath];
	if(success) return;
    
	NSString *databasePathFromApp = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:DatabaseName];
    
	[fileManager copyItemAtPath:databasePathFromApp toPath:self.databasePath error:nil];
}

-(void) readEmployeesFromDatabase {
	// Setup the database object
	sqlite3 *database;
    
	self.employees = [[NSMutableArray alloc] init];
    
	// Open the database from the users filessytem
	if(sqlite3_open([self.databasePath UTF8String], &database) == SQLITE_OK) {
		// Setup the SQL Statement and compile it for faster access
		const char *sqlStatement = "SELECT * FROM employeeInfo ORDER BY employee ASC";
		sqlite3_stmt *compiledStatement;
		if(sqlite3_prepare_v2(database, sqlStatement, -1, &compiledStatement, NULL) == SQLITE_OK) {
			// Loop through the results and add them to the feeds array
			while(sqlite3_step(compiledStatement) == SQLITE_ROW) {
				// Read the data from the result row
				char *charID = (char *)sqlite3_column_text(compiledStatement,0);
                char *charName = (char *)sqlite3_column_text(compiledStatement,1);
                char *charTitle = (char *)sqlite3_column_text(compiledStatement,2);
                char *charWorkPhone = (char *)sqlite3_column_text(compiledStatement,3);
                char *charMobile = (char *)sqlite3_column_text(compiledStatement,4);
                char *charEmail = (char *)sqlite3_column_text(compiledStatement,5);
                char *charImageUrl = (char *)sqlite3_column_text(compiledStatement,6);
                char *charWorkplace = (char *)sqlite3_column_text(compiledStatement,7);
                char *charDivision = (char *)sqlite3_column_text(compiledStatement,8);
                
                NSString *eID = (charID != NULL) ? [NSString stringWithUTF8String:charID] : @"";
                NSString *eName = (charName != NULL) ? [NSString stringWithUTF8String:charName] : @"";
                NSString *eTitle = (charTitle != NULL) ? [NSString stringWithUTF8String:charTitle] : @"";
                NSString *eWorkPhone = (charWorkPhone != NULL) ? [NSString stringWithUTF8String:charWorkPhone] : @"";
                NSString *eMobile = (charMobile != NULL) ? [NSString stringWithUTF8String:charMobile] : @"";
                NSString *eEmail = (charEmail != NULL) ? [NSString stringWithUTF8String:charEmail] : @"";
                NSString *eImageUrl = (charImageUrl != NULL) ? [NSString stringWithUTF8String:charImageUrl] : @"";
                NSString *eWorkplace = (charWorkplace != NULL) ? [NSString stringWithUTF8String:charWorkplace] : @"";
                NSString *eDivision = (charDivision != NULL) ? [NSString stringWithUTF8String:charDivision] : @"";
                
				// Create a new phonebook object with the data from the database
				Phonebook *phonebook = [[Phonebook alloc] initWithName:eName 
                                                                    ID:eID 
                                                             workPhone:eWorkPhone 
                                                                mobile:eMobile 
                                                                 title:eTitle
                                                                 email:eEmail 
                                                              imageUrl:eImageUrl
                                                             workPlace:eWorkplace
                                                              division:eDivision];
                
				// Add the phonebook object to the phonebooks Array
				[self.employees addObject:phonebook];
			}
		}
        
		// Release the compiled statement from memory
		sqlite3_finalize(compiledStatement);
	}
    
	sqlite3_close(database);
}

-(void)downloadImagesFromWeb {
    
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        NSString *imagesPath = [documentsPath stringByAppendingPathComponent:@"images"];
        NSString *thumbPath = [imagesPath stringByAppendingPathComponent:@"thumbs"];
        [[NSFileManager defaultManager]createDirectoryAtPath:thumbPath withIntermediateDirectories:YES attributes:nil error:NULL];
        for(Phonebook *phonebook in self.employees) {
            NSString *imageLocation = [[imagesPath stringByAppendingPathComponent:phonebook.ID]stringByAppendingString:@".png"];
            NSString *thumbLocation = [[thumbPath stringByAppendingPathComponent:phonebook.ID]stringByAppendingString:@".png"];
            if(![[NSFileManager defaultManager]fileExistsAtPath:imageLocation]) {
                NSData *imageData = [[NSData alloc]initWithContentsOfURL:[NSURL URLWithString:phonebook.imageUrl]];
                UIImage *image = nil;
                if((image = [UIImage imageWithData:imageData])) {
                    
                    UIImage *thumbnail = [image thumbnailImage:43 transparentBorder:0 cornerRadius:5 interpolationQuality:kCGInterpolationHigh];
                    BOOL thumbWorked = [UIImagePNGRepresentation(thumbnail) writeToFile:thumbLocation atomically:YES];
                    BOOL writeWorked = [UIImagePNGRepresentation(image) writeToFile:imageLocation atomically:YES]; 
                    if(!writeWorked || !thumbWorked) {
                        NSLog(@"Failed to write to documents folder");
                    }
                }  
            } 
        }
    });
    
}

@end
