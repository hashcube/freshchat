#import "FreshchatPlugin.h"

@interface FreshchatPlugin()
@end

@implementation FreshchatPlugin

// The plugin must call super dealloc.
- (void) dealloc {
  [super dealloc];
}

// The plugin must call super init.
- (id) init {
  self = [super init];
  if (!self) {
    return nil;
  }

  return self;
}

- (void) initializeWithManifest:(NSDictionary *)manifest appDelegate:(TeaLeafAppDelegate *)appDelegate {
  @try {
    NSDictionary *ios = [manifest valueForKey:@"ios"];
    NSString *appID = [ios valueForKey:@"freshchatAppID"];
    NSString *appKey = [ios valueForKey:@"freshchatAppKey"];
    self.app_tag = [ios valueForKey:@"freshchatTag"];

    self.viewController = appDelegate.tealeafViewController;

    FreshchatConfig *config = [[FreshchatConfig alloc]initWithAppID:appID andAppKey:appKey];
    config.cameraCaptureEnabled = YES;
    config.teamMemberInfoVisible = YES;
    config.showNotificationBanner = YES;
    [[Freshchat sharedInstance] initWithConfig:config];
  }
  @catch (NSException *exception) {
    NSLog(@"{freshchat} Failure to get: %@", exception);
  }
}

- (void) setName: (NSDictionary *)jsonObject {
  FreshchatUser *user = [FreshchatUser sharedInstance];

  NSLog(@"User %@", user);
  user.firstName = [jsonObject objectForKey:@"name"];
  [[Freshchat sharedInstance] setUser:user];
}

- (void) setEmail: (NSDictionary *)jsonObject {
  FreshchatUser *user = [FreshchatUser sharedInstance];

  user.email = [jsonObject objectForKey:@"email"];
  [[Freshchat sharedInstance] setUser:user];
}

- (void) addMetaData: (NSDictionary *)jsonObject {
  [[Freshchat sharedInstance]
    updateUserPropertyforKey:[jsonObject objectForKey:@"field_name"]
    withValue:[jsonObject objectForKey:@"value"]];
}

- (void) clearUserData: (NSDictionary *)jsonObject {
  [[Freshchat sharedInstance] clearUserDataWithCompletion:nil];
}

- (void) showConversations: (NSDictionary *)jsonObject {
  ConversationOptions *options = [ConversationOptions new];
  [options filterByTags:@[ self.app_tag ] withTitle: @"Messages"];
  [[Freshchat sharedInstance] showConversations:self.viewController withOptions: options];
}

- (void) showFAQs: (NSDictionary *)jsonObject {
  FAQOptions *options = [FAQOptions new];

  options.showContactUsOnFaqScreens = YES;
  options.showContactUsOnAppBar = YES;

  [options filterByTags:@[ self.app_tag ] withTitle: @"Message Us" andType: CATEGORY];
  [options filterContactUsByTags:@[ self.app_tag ] withTitle: @"Contact Us"];
  [[Freshchat sharedInstance] showFAQs:self.viewController withOptions:options];
}

- (void) getUnreadCountAsync: (NSDictionary *)jsonObject {
  [[Freshchat sharedInstance]unreadCountWithCompletion:^(NSInteger count) {
      NSLog(@"Unread count (Async) : %d", (int)count);
      [[PluginManager get] dispatchJSEvent:[NSDictionary dictionaryWithObjectsAndKeys:
                            @"freshchatUnreadCount", @"name",
                            [NSString stringWithFormat: @"%ld", count], @"count",
                            nil]];
  }];
}
@end
