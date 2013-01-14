//
//  Employee.h
//  iphone-phoneislenska
//
//  Created by Hörður Hauksson on 7/11/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface Employee : NSManagedObject

@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * empID;
@property (nonatomic, retain) NSString * email;
@property (nonatomic, retain) NSString * mobilephone;
@property (nonatomic, retain) NSString * workphone;
@property (nonatomic, retain) NSString * imageUrl;
@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSString * division;
@property (nonatomic, retain) NSString * workplace;

@end
