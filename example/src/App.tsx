import Unityads from 'asdaily-rn-unityads';

import React, {useState} from 'react';
import {Platform, StyleSheet, Text, View} from 'react-native';
import AppButton from './AppButton';
import 'react-native-gesture-handler';
import {NavigationContainer} from "@react-navigation/native";

var adLoadState = {
  notLoaded: 'NOT_LOADED',
  loading: 'LOADING',
  loaded: 'LOADED',
};

var adsShowState = {
  notStarted: 'NOT_STARTED',
  completed: 'COMPLETED',
  failed: 'FAILED',
  start: 'STARTED',
  click: 'CLICKED',
};

const App = () => {

  // GameID
  const SDK_KEY = Platform.select({
    ios: '3231863',
    android: '3231862',
  });

  const INTERSTITIAL_AD_UNIT_ID = Platform.select({
    ios: 'iOS_Interstitial',
    android: 'Android_Interstitial',
  });

  const REWARDED_AD_UNIT_ID = Platform.select({
    ios: 'iOS_rewarded',
    android: 'android_rewarded',
  });

  const BANNER_AD_UNIT_ID = Platform.select({
    ios: 'iOS_banner',
    android: 'android_banner',
  });

  // Create states
  const [isInitialized, setIsInitialized] = useState(false);
  const [interstitialAdLoadState, setInterstitialAdLoadState] = useState(adLoadState.notLoaded);
  const [unityAdShowCompleteState, setUnityAdShowCompleteState] = useState(adsShowState.notStarted);
  const [interstitialRetryAttempt, setInterstitialRetryAttempt] = useState(0);
  const [rewardedAdLoadState, setRewardedAdLoadState] = useState(adLoadState.notLoaded);
  const [isNativeUIBannerShowing, setIsNativeUIBannerShowing] = useState(false);
  const [statusText, setStatusText] = useState('Initializing SDK...');


  Unityads.initialize(SDK_KEY, 1, (callback) => { //second parameter for test mode, 1 default. 0 for production.
    setIsInitialized(true);
    logStatus('SDK Initialized: '+ callback);

    // Attach ad listeners for rewarded ads, and banner ads
    attachAdListeners();
  });

  function attachAdListeners() {
    Unityads.addEventListener('onUnityAdsAdFailedToLoad', (errorInfo) => {
      // ad failed to load
      // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)
      setInterstitialRetryAttempt(interstitialRetryAttempt + 1);

      var retryDelay = Math.pow(2, Math.min(6, interstitialRetryAttempt));
      logStatus('Interstitial ad failed to load with code ' + errorInfo + ' - retrying in ' + retryDelay + 's');
    });
    Unityads.addEventListener('onUnityAdsAdLoaded', (adInfo) => {
      logStatus('unity AdLoaded, with ID: ' +adInfo.adUnitId);
    });


    Unityads.addEventListener('onUnityAdsShowComplete', (adInfo) => {
      setUnityAdShowCompleteState(adsShowState.completed);
      logStatus('Ads show completed, with ID: ' +adInfo.adUnitId +" state: "+ adInfo.state);
      if (adInfo.adUnitId == REWARDED_AD_UNIT_ID && adInfo.state == 1) {
        console.log('reward the user');
      }
    });
    Unityads.addEventListener('onUnityAdsShowFailed', (adInfo) => {
      setUnityAdShowCompleteState(adsShowState.failed);
      logStatus('Ads show failed, with ID: '+adInfo.adUnitId +"message: "+ adInfo.message +"error: "+adInfo.error);
      
    });
    Unityads.addEventListener('onUnityAdsShowStart', (adInfo) => {
      setUnityAdShowCompleteState(adsShowState.start);
      logStatus('Ads show started , with ID: '+adInfo.adUnitId);
    });
    Unityads.addEventListener('onUnityAdsShowClick', (adInfo) => {
      setUnityAdShowCompleteState(adsShowState.click);
      logStatus('Ads show clicked, with ID: '+adInfo.adUnitId);
    });
   

    // Banner Ad Listeners
    Unityads.addEventListener('bannerViewDidLoad', (adInfo) => {
      logStatus('Banner ad loaded, with ID: ' +adInfo.adUnitId);
      setIsNativeUIBannerShowing(!isNativeUIBannerShowing);
    });
    Unityads.addEventListener('onBannerViewDidError', (errorInfo) => {
      logStatus('Banner ad failed to load with error code ' + errorInfo.code + ' and message: ' + errorInfo.message);
    });
    Unityads.addEventListener('onBannerViewDidClick', (adInfo) => {
      logStatus('Banner ad clicked');
    });
    Unityads.addEventListener('onBannerViewDidLeaveApplication', (adInfo) => {
      logStatus('Banner ad leave application')
      setIsNativeUIBannerShowing(!isNativeUIBannerShowing);
    });
  }

  function getInterstitialButtonTitle() {
    if (interstitialAdLoadState === adLoadState.notLoaded) {
      return 'Load Interstitial';
    } else if (interstitialAdLoadState === adLoadState.loading) {
      return 'Loading...';
    } else {
      return 'Show Interstitial'; // adLoadState.loaded
    }
  }

  function getRewardedButtonTitle() {
    if (rewardedAdLoadState === adLoadState.notLoaded) {
      return 'Load Rewarded Ad';
    } else if (rewardedAdLoadState === adLoadState.loading) {
      return 'Loading...';
    } else {
      return 'Show Rewarded Ad'; // adLoadState.loaded
    }
  }

  function logStatus(status) {
    console.log(status);
    setStatusText(status);
  }

  return (
    <NavigationContainer>
      <View style={styles.container}>
        <Text style={styles.statusText}>
          {statusText}
        </Text>
        <AppButton
          title={getInterstitialButtonTitle()}
          enabled={
            isInitialized && interstitialAdLoadState !== adLoadState.loading
          }
          onPress={() => {
            Unityads.loadInterstitial(INTERSTITIAL_AD_UNIT_ID);
          }}
        />
        <AppButton
          title={getRewardedButtonTitle()}
          enabled={isInitialized && rewardedAdLoadState !== adLoadState.loading}
          onPress={() => {
            Unityads.loadInterstitial(REWARDED_AD_UNIT_ID);
          }}
        />
        <AppButton
          title={isNativeUIBannerShowing ? 'Hide Native UI Banner' : 'Show Native UI Banner'}
          enabled={isInitialized}
          onPress={() => {
            if (isNativeUIBannerShowing) {
              Unityads.unLoadBottomBanner();
            }else{
              Unityads.loadBottomBanner(BANNER_AD_UNIT_ID);
            
            } 
            
          }}
        /> 
        
      </View>
    </NavigationContainer>
  );
};


const styles = StyleSheet.create({
  container: {
    paddingTop: 80,
    flex: 1, // Enables flexbox column layout
  },
  statusText: {
    marginBottom: 10,
    backgroundColor: 'green',
    padding: 10,
    fontSize: 20,
    textAlign: 'center',
  },
  banner: {
    // Set background color for banners to be fully functional
    backgroundColor: '#000000',
    position: 'absolute',
    width: '100%',
    height: 300,
    bottom: Platform.select({
      ios: 36, // For bottom safe area
      android: 0,
    })
  }
});

export default App;
