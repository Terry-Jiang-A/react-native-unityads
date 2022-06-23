#import "Unityads.h"

#define ROOT_VIEW_CONTROLLER (UIApplication.sharedApplication.keyWindow.rootViewController)


@interface Unityads()<UADSBannerViewDelegate>

// Parent Fields
@property (nonatomic, assign, getter=isPluginInitialized) BOOL pluginInitialized;
@property (nonatomic, assign, getter=isSDKInitialized) BOOL sdkInitialized;


// Banner Fields
// This is the Ad Unit or Placement that will display banner ads:
@property (strong) NSString* placementId;
// This banner view object will be placed at the bottom of the screen:
@property (strong, nonatomic) UADSBannerView *bottomBannerView;

@property (nonatomic, strong) UIView *safeAreaBackground;

// React Native's proposed optimizations to not emit events if no listeners
@property (nonatomic, assign) BOOL hasListeners;

@end
@implementation Unityads

static NSString *const SDK_TAG = @"UnitySdk";
static NSString *const TAG = @"Unity Ads";

RCTResponseSenderBlock _onInitialized = nil;

static Unityads *UnityShared; // Shared instance of this bridge module.

RCT_EXPORT_MODULE()

// `init` requires main queue b/c of UI code
+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

// Invoke all exported methods from main queue
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (Unityads *)shared
{
    return UnityShared;
}

- (instancetype)init
{
    self = [super init];
    if ( self )
    {
        UnityShared = self;
    }
    return self;
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(isInitialized)
{
    return @([self isPluginInitialized] && [self isSDKInitialized]);
}

RCT_EXPORT_METHOD(initialize :(NSString *)sdkKey :(nonnull NSNumber *)testMode :(RCTResponseSenderBlock)callback)
{
    // Guard against running init logic multiple times
    if ( [self isPluginInitialized] )
    {
        callback(@[@" Unity Sdk has Initiallized"]);
        return;
    }
    
    self.pluginInitialized = YES;
    _onInitialized = callback;
    
    // Initialize
    [UnityAds
             initialize:sdkKey
             testMode:[RCTConvert BOOL:testMode]
             initializationDelegate:self];
    
    
}

#pragma mark - Interstitials

RCT_EXPORT_METHOD(loadInterstitial:(NSString *)adUnitIdentifier)
{
    [UnityAds load:adUnitIdentifier loadDelegate:self];
}

RCT_EXPORT_METHOD(loadBottomBanner:(NSString *)adUnitIdentifier)
{
    self.bottomBannerView = [[UADSBannerView alloc] initWithPlacementId:adUnitIdentifier size:CGSizeMake(320, 50)];
    self.bottomBannerView.delegate = self;
    [self addBannerViewToBottomView:self.bottomBannerView];
        [_bottomBannerView load];
}

RCT_EXPORT_METHOD(unLoadBottomBanner)
{
    [self.bottomBannerView removeFromSuperview];
    _bottomBannerView = nil;
    [self sendReactNativeEventWithName: @"OnbannerViewDidLeaveApplication" body: @{@"adUnitId" : @"unload bannerview"}];
    
}

- (void)addBannerViewToBottomView: (UIView *)bannerView {
    bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    [ROOT_VIEW_CONTROLLER.view addSubview:bannerView];
    [ROOT_VIEW_CONTROLLER.view addConstraints:@[
                               [NSLayoutConstraint constraintWithItem:bannerView
                                                            attribute:NSLayoutAttributeBottom
                                                            relatedBy:NSLayoutRelationEqual
                                                               toItem:ROOT_VIEW_CONTROLLER.bottomLayoutGuide
                                                            attribute:NSLayoutAttributeTop
                                                           multiplier:1
                                                             constant:0],
                               [NSLayoutConstraint constraintWithItem:bannerView
                                                            attribute:NSLayoutAttributeCenterX
                                                            relatedBy:NSLayoutRelationEqual
                                                               toItem:ROOT_VIEW_CONTROLLER.view
                                                            attribute:NSLayoutAttributeCenterX
                                                           multiplier:1
                                                             constant:0]
                               ]];
}

#pragma mark: UnityAdsShowDelegate
- (void)unityAdsShowComplete:(NSString *)placementId withFinishState:(UnityAdsShowCompletionState)state {
    NSLog(@" - UnityAdsShowDelegate unityAdsShowComplete %@ %ld", placementId, state);
    [self sendReactNativeEventWithName: @"OnunityAdsShowComplete" body: @{@"adUnitId" : placementId,
                                                     @"state" : @(state)}];
    
}

- (void)unityAdsShowFailed:(NSString *)placementId withError:(UnityAdsShowError)error withMessage:(NSString *)message {
    NSLog(@" - UnityAdsShowDelegate unityAdsShowFailed %@ %ld", message, error);
    [self sendReactNativeEventWithName: @"OnunityAdsShowFailed" body: @{@"adUnitId" : placementId,
                                                                        @"message" : message,              @"error" : @(error)}];
    // Optionally execute additional code, such as attempting to load another ad.
}
 
- (void)unityAdsShowStart:(NSString *)placementId {
    NSLog(@" - UnityAdsShowDelegate unityAdsShowStart %@", placementId);
    [self sendReactNativeEventWithName: @"OnunityAdsShowStart" body: @{@"adUnitId" : placementId}];
    
}
 
- (void)unityAdsShowClick:(NSString *)placementId {
    NSLog(@" - UnityAdsShowDelegate unityAdsShowClick %@", placementId);
    [self sendReactNativeEventWithName: @"OnunityAdsShowClick" body: @{@"adUnitId" : placementId}];
    
}

// Implement the delegate methods:
#pragma mark : UADSBannerViewDelegate
 
- (void)bannerViewDidLoad:(UADSBannerView *)bannerView {
    // Called when the banner view object finishes loading an ad.
    NSLog(@"Banner loaded for Ad Unit or Placement: %@", bannerView.placementId);
    [self sendReactNativeEventWithName: @"bannerViewDidLoad" body: @{@"adUnitId" : bannerView.placementId}];
    
}
 
- (void)bannerViewDidClick:(UADSBannerView *)bannerView {
    // Called when the banner is clicked.
    NSLog(@"Banner was clicked for Ad Unit or Placement: %@", bannerView.placementId);
    [self sendReactNativeEventWithName: @"OnbannerViewDidClick" body: @{@"adUnitId" : bannerView.placementId}];
    
}
 
- (void)bannerViewDidLeaveApplication:(UADSBannerView *)bannerView {
    // Called when the banner links out of the application.
    [self sendReactNativeEventWithName: @"OnbannerViewDidLeaveApplication" body: @{@"adUnitId" : bannerView.placementId}];
    
}
 
 
- (void)bannerViewDidError:(UADSBannerView *)bannerView error:(UADSBannerError *)error{
    // Called when an error occurs showing the banner view object.
    NSLog(@"Banner encountered an error for Ad Unit or Placement: %@ with error message %@", bannerView.placementId, [error localizedDescription]);
    // Note that the UADSBannerError can indicate no fill (see API documentation).
    [self sendReactNativeEventWithName: @"OnbannerViewDidError" body: @{@"adUnitId" : bannerView.placementId,
                                                                        @"error" : [error localizedDescription]}];
    
}

// Implement initialization callbacks to handle success or failure:
#pragma mark : UnityAdsInitializationDelegate
- (void)initializationComplete {
    //NSLog(@" - UnityAdsInitializationDelegate initializationComplete" );
    self.sdkInitialized = YES;
    _onInitialized(@[@"success"]);
    // Pre-load an ad when initialization succeeds, so it is ready to show:
    //[UnityAds load:@"iOS_Interstitial" loadDelegate:self];
}
 
- (void)initializationFailed:(UnityAdsInitializationError)error withMessage:(NSString *)message {
    NSLog(@" - UnityAdsInitializationDelegate initializationFailed with message: %@", message );
    self.pluginInitialized = NO;
    _onInitialized(@[@" - UnityAdsInitializationDelegate initializationFailed with message: %@", message ]);
   
}

// Implement load callbacks to handle success or failure after initialization:
#pragma mark: UnityAdsLoadDelegate
- (void)unityAdsAdLoaded:(NSString *)placementId {
    NSLog(@" - UnityAdsLoadDelegate unityAdsAdLoaded %@", placementId);
    [self sendReactNativeEventWithName: @"OnunityAdsAdLoaded" body: @{@"adUnitId" : placementId}];
    [UnityAds show:ROOT_VIEW_CONTROLLER placementId:placementId showDelegate:self];
}
 
- (void)unityAdsAdFailedToLoad:(NSString *)placementId
                     withError:(UnityAdsLoadError)error
                   withMessage:(NSString *)message {
    NSLog(@" - UnityAdsLoadDelegate unityAdsAdFailedToLoad %@", placementId);
    [self sendReactNativeEventWithName: @"OnunityAdsAdFailedToLoad" body: @{@"adUnitId" : placementId,
                                                     @"error" : @(error),
                                                     @"message" : message}];
    
}

#pragma mark - React Native Event Bridge

- (void)sendReactNativeEventWithName:(NSString *)name body:(NSDictionary<NSString *, id> *)body
{
    [self sendEventWithName: name body: body];
}

// From RCTBridgeModule protocol
- (NSArray<NSString *> *)supportedEvents
{
    return @[@"OnunityAdsAdLoaded",
             @"OnunityAdsAdFailedToLoad",
             
             @"OnbannerViewDidError",
             @"OnbannerViewDidLeaveApplication",
             @"OnbannerViewDidClick",
             @"bannerViewDidLoad",
             
             @"OnunityAdsShowClick",
             @"OnunityAdsShowStart",
             @"OnunityAdsShowFailed",
             @"OnunityAdsShowComplete"];
}

- (void)startObserving
{
    self.hasListeners = YES;
}

- (void)stopObserving
{
    self.hasListeners = NO;
}

@end
