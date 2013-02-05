//
//  Config.h
//  metrolyrics
//
//  Created by Kevin Renskers on 23-05-12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "MCJSONRemoteConfig.h"

@interface Config : MCJSONRemoteConfig

+ (Config *)config;
@property (strong, nonatomic) NSString *twitterFeed;
@property (strong, nonatomic) NSString *rssFeed;
@property (strong, nonatomic) NSString *tumblrFeed;

@end
