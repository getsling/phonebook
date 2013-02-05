//
//  Employee+Methods.m
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 7/11/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "Employee+Methods.h"

@implementation Employee (Methods)
+ (Employee *) insertEmployeeWithName:(NSString *)name email:(NSString *)email mobilephone:(NSString *)mobilephone workphone:(NSString *)workphone imageUrl:(NSString *)imageUrl empID:(NSNumber *)empID inContext:(NSManagedObjectContext *)context {
    
    Employee *employee = [Employee createInContext:context];
    employee.name = name;
    employee.empID = empID;
    employee.email = email;
    employee.workphone = workphone;
    employee.mobilephone = mobilephone;
    employee.imageUrl = imageUrl;
    return employee;
}
@end
