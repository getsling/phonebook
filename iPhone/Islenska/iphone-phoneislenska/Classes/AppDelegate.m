//
//  AppDelegate.m
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "AppDelegate.h"
#import "MainViewController.h"
#import "Phonebook.h"
#import "Constants.h"
#import "MasterViewController.h"
#import "AFJSONRequestOperation.h"
#import "Employee+Methods.h"

@implementation AppDelegate
@synthesize window = _window;
@synthesize navigationController = _navigationController;
@synthesize employees = _employees;

-(void)getEmployeeFromJson {
    NSURL *url = [NSURL URLWithString:@"http://127.0.0.1:8000/companies/1/employee_json/"];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSDate *lastChecked = [defaults objectForKey:@"Last-modified-db"];
    if(lastChecked == nil) {
        lastChecked = [[NSDate alloc]initWithTimeIntervalSince1970:0];   
        [defaults setObject:[NSNumber numberWithBool:NO] forKey:@"hasPhonebook"];
        [defaults synchronize];
    }
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    NSDateFormatter *formatter = [[NSDateFormatter alloc]init];
    [formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    NSString *lastCheckedFormatted = [formatter stringFromDate:lastChecked];
    [request setValue:lastCheckedFormatted forHTTPHeaderField:@"Last-modified"];
    AFJSONRequestOperation *getJsonOperation = [[AFJSONRequestOperation alloc]initWithRequest:request];
    [getJsonOperation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        // TODO: what do we do with the pictures
        [defaults setObject:[NSDate date] forKey:@"Last-modified-db"];
        [defaults synchronize];
        NSManagedObjectContext *context = [NSManagedObjectContext MR_contextForCurrentThread];
        NSArray *allEmps = [Employee findAll];
        if(allEmps) {
            for(Employee *emp in allEmps) {
                [emp deleteInContext:context];
            }
        }
        [context save];
        
        NSArray *employees = (NSArray *)responseObject;
        NSManagedObjectContext *myNewContext = [NSManagedObjectContext MR_context];
        for(NSDictionary *dict in employees) {
            NSDictionary *fields = [dict objectForKey:@"fields"];
            NSString *name = [fields objectForKey:@"name"];
            NSString *email = [fields objectForKey:@"email"];
            NSString *mobilephone = [fields objectForKey:@"mobilephone"];
            NSString *workphone = [fields objectForKey:@"workphone"];
            NSNumber *identification = [dict objectForKey:@"pk"];
            NSString *imageUrl = [fields objectForKey:@"imageurl"];
            [Employee insertEmployeeWithName:name email:email mobilephone:mobilephone workphone:workphone imageUrl:imageUrl empID:identification inContext:myNewContext];
        }
        [myNewContext save];
        [defaults setObject:[NSNumber numberWithBool:YES] forKey:@"hasPhonebook"];
        [defaults synchronize];
        [[NSNotificationCenter defaultCenter]postNotification:[NSNotification notificationWithName:@"DatabaseDownloaded" object:nil]];
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        if([operation.response statusCode] == 304) {
            NSLog(@"No new data");
        } else {
            NSLog(@"%@", error);
        }
    }];
    [getJsonOperation start];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [MagicalRecordHelpers setupAutoMigratingCoreDataStack];
    dispatch_queue_t databaseQueue = dispatch_queue_create("database downloader", NULL);
    dispatch_async(databaseQueue, ^{
        [self getEmployeeFromJson];      
    });
    
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    MainViewController *mvc = [[MainViewController alloc]initWithNibName:@"MainViewController" bundle:nil];
    self.navigationController = [[UINavigationController alloc] initWithRootViewController:mvc];
    self.window.rootViewController = self.navigationController;
    
    UINavigationBar *navBar = [self.navigationController navigationBar];
    if ([navBar respondsToSelector:@selector(setBackgroundImage:forBarMetrics:)]) {
        [navBar setBackgroundImage:[UIImage imageNamed:@"bg_nav"] forBarMetrics:UIBarMetricsDefault];
        [[UIBarButtonItem appearance]setTintColor:[UIColor colorWithWhite:0.1 alpha:1]];
        
    }
    [self.window makeKeyAndVisible];
    return YES;
}

- (void)applicationWillTerminate:(UIApplication *)application {
    [MagicalRecordHelpers cleanUp];
}

@end
