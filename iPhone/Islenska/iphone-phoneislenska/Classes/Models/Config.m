//
//  Config.m
//  metrolyrics
//
//  Created by Kevin Renskers on 23-05-12.
//  Copyright (c) 2012 Gangverk. All rights reserved.
//

#import "Config.h"

@implementation Config

@synthesize twitterFeed = _twitterFeed;
@synthesize rssFeed = _rssFeed;
@synthesize tumblrFeed = _tumblrFeed;

+ (Config *)config {
    static dispatch_once_t pred;
    static Config *sharedInstance = nil;
    dispatch_once(&pred, ^{ sharedInstance = [[self alloc] init]; });
    return sharedInstance;
}

- (NSURL *)remoteFileLocation {
    return [NSURL URLWithString:@"http://kompany.gangverk.is:8000/companies/1/config/"];
}

- (void)setupMapping {
    [self mapRemoteKeyPath:@"TW" toLocalAttribute:@"twitterFeed" defaultValue:@"https://api.twitter.com/1/statuses/user_timeline.json?screen_name=islenska&trim_user=1"];
    [self mapRemoteKeyPath:@"TU" toLocalAttribute:@"tumblrFeed" defaultValue:@"http://islenska.tumblr.com/rss"];
    [self mapRemoteKeyPath:@"RS" toLocalAttribute:@"rssFeed" defaultValue:nil];
}

-(NSTimeInterval)redownloadRate {
    return 3600; // One hour, 60*60
}

@end
