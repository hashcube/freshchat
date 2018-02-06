#import "PluginManager.h"
#import "Freshchat.h"

@interface FreshchatPlugin : GCPlugin
@property (nonatomic, retain) NSString *app_tag;
@property(retain, nonatomic) UIViewController *viewController;
@end