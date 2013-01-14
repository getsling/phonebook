//
//  Phonebook.h
//  Phonebook
//
//  Created by Hörður Hauksson on 1/18/12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface Phonebook : NSObject

@property (nonatomic, strong) NSString *ID;
@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) NSString *title;
@property (nonatomic, strong) NSString *email;
@property (nonatomic, strong) NSString *imageUrl;
@property (nonatomic, strong) NSString *workPhone;
@property (nonatomic, strong) NSString *mobile;
@property (nonatomic, strong) NSString *workPlace;
@property (nonatomic, strong) NSString *division;

- (id)initWithName:(NSString *)name
               ID:(NSString *)ID 
        workPhone:(NSString *)workPhone 
           mobile:(NSString *)mobile 
            title:(NSString *)title 
            email:(NSString *)email 
            imageUrl:(NSString *)imageURL
        workPlace:(NSString *)workPlace
         division:(NSString *)division;

@end