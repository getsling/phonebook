//
//  Employee+Methods.h
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 7/11/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "Employee.h"

@interface Employee (Methods)
+ (Employee *) insertEmployeeWithName:(NSString *)name email:(NSString *)email mobilephone:(NSString *)mobilephone workphone:(NSString *)workphone imageUrl:(NSString *)imageUrl empID:(NSNumber *)empID inContext:(NSManagedObjectContext *)context;
@end
