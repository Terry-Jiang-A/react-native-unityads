# react-native-unityads

unity ads module

## Installation

```sh
npm install RN-unityads
```

## Usage

```js
import Unityads from 'RN-unityads';

  
  Initialize SDK
  Unityads.initialize(SDK_KEY, 1, (callback) => { //second parameter for test mode, 1 default. 0 for production.
    

    // Attach ad listeners for rewarded ads, and banner ads
    attachAdListeners(); //need to call removeEventListener to remove listeners.
  });
  
  Attach ad Listeners for rewarded ads, and banner ads, and so on.
  function attachAdListeners() {
    Unityads.addEventListener('OnunityAdsAdFailedToLoad', (errorInfo) => {
      // ad failed to load
      // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)
      setInterstitialRetryAttempt(interstitialRetryAttempt + 1);

      var retryDelay = Math.pow(2, Math.min(6, interstitialRetryAttempt));
      logStatus('Interstitial ad failed to load with code ' + errorInfo + ' - retrying in ' + retryDelay + 's');
    });
    Unityads.addEventListener('OnunityAdsAdLoaded', (adInfo) => {
      logStatus('unity AdLoaded, with ID: ' +adInfo.adUnitId);
    });


    Unityads.addEventListener('OnunityAdsShowComplete', (adInfo) => {
      setUnityAdShowCompleteState(adsShowState.completed);
      logStatus('Ads show completed, with ID: ' +adInfo.adUnitId +" state: "+ adInfo.state);
      if (adInfo.adUnitId == REWARDED_AD_UNIT_ID && adInfo.state == 1) {
        console.log('reward the user');
      }
    });
    Unityads.addEventListener('OnunityAdsShowFailed', (adInfo) => {
      setUnityAdShowCompleteState(adsShowState.failed);
      logStatus('Ads show failed, with ID: '+adInfo.adUnitId +"message: "+ adInfo.message +"error: "+adInfo.error);
      
    });
    Unityads.addEventListener('OnunityAdsShowStart', (adInfo) => {
      setUnityAdShowCompleteState(adsShowState.start);
      logStatus('Ads show started , with ID: '+adInfo.adUnitId);
    });
    Unityads.addEventListener('OnunityAdsShowClick', (adInfo) => {
      setUnityAdShowCompleteState(adsShowState.click);
      logStatus('Ads show clicked, with ID: '+adInfo.adUnitId);
    });
   

    // Banner Ad Listeners
    Unityads.addEventListener('bannerViewDidLoad', (adInfo) => {
      logStatus('Banner ad loaded, with ID: ' +adInfo.adUnitId);
      setIsNativeUIBannerShowing(!isNativeUIBannerShowing);
    });
    Unityads.addEventListener('OnbannerViewDidError', (errorInfo) => {
      logStatus('Banner ad failed to load with error code ' + errorInfo.code + ' and message: ' + errorInfo.message);
    });
    Unityads.addEventListener('OnbannerViewDidClick', (adInfo) => {
      logStatus('Banner ad clicked');
    });
    Unityads.addEventListener('OnbannerViewDidLeaveApplication', (adInfo) => {
      logStatus('Banner ad leave application')
      setIsNativeUIBannerShowing(!isNativeUIBannerShowing);
    });
  }
  
  ios:
  Modify podfile，add Unity Ads SDK：
  pod 'UnityAds'
  
  For specific usage, please refer to example.
  How To Run example:
  1,$ cd example && npm install
  2,$ cd ios && pod install
  3,$ cd .. && npm run ios or npm run android
  // ...

```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
