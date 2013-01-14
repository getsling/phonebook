//
//  Phonebook.m
//  Phonebook
//
//  Created by Hörður Hauksson on 1/18/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "Phonebook.h"

@implementation Phonebook

@synthesize name = _name;
@synthesize ID = _ID;
@synthesize workPhone = _workPhone;
@synthesize mobile = _mobile;
@synthesize title = _title;
@synthesize email = _email;
@synthesize imageUrl = _imageUrl;
@synthesize workPlace = _workPlace;
@synthesize division = _division;

- (id)initWithName:(NSString *)name
                ID:(NSString *)ID 
         workPhone:(NSString *)workPhone 
            mobile:(NSString *)mobile 
             title:(NSString *)title 
             email:(NSString *)email 
          imageUrl:(NSString *)imageURL
         workPlace:(NSString *)workPlace
          division:(NSString *)division {

	self = [super init];
    
    if (self) {
        self.ID = ID;
        self.name = name;
        self.workPhone = workPhone;
        self.mobile = mobile;
        self.email = email;
        self.imageUrl = imageURL;
        self.division = division;
        self.workPlace = workPlace;
        self.title = title;
    }

	return self;
}

@end
