//
//  AppDelegate.h
//  Phonebook
//
//  Created by Hörður Hauksson on 1/17/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <sqlite3.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (strong, nonatomic) UINavigationController *navigationController;
@property (nonatomic, strong) NSMutableArray *employees;

-(void) checkAndCreateDatabase;
-(void) readEmployeesFromDatabase;
-(void) checkForNewDatabase:databasePath;

@end
