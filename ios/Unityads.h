#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTConvert.h>
#import <UnityAds/UnityAds.h>

#define KEY_WINDOW [UIApplication sharedApplication].keyWindow
#define DEVICE_SPECIFIC_ADVIEW_AD_FORMAT ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) ? MAAdFormat.leader : MAAdFormat.banner

NS_ASSUME_NONNULL_BEGIN

/**
 * The primary bridge between JS <-> native code for the Unity ads React Native module.
 */
@interface Unityads : RCTEventEmitter<RCTBridgeModule, UnityAdsInitializationDelegate,
UnityAdsLoadDelegate,
UnityAdsShowDelegate>

/**
 * Shared instance of this bridge module.
 */
@property (nonatomic, strong, readonly, class) Unityads *shared;

@end

NS_ASSUME_NONNULL_END

/*@interface Unityads : NSObject <RCTBridgeModule>

@end*/
