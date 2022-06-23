package com.reactnativeunityads;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

//unity ads
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;


import java.util.concurrent.TimeUnit;
import android.util.Log;
import android.text.TextUtils;
import android.app.Activity;
import android.content.Context;
import androidx.annotation.Nullable;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.content.pm.ActivityInfo;



import static com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

@ReactModule(name = UnityadsModule.NAME)
public class UnityadsModule extends ReactContextBaseJavaModule implements IUnityAdsInitializationListener{
    public static final String NAME = "Unityads";

    private static final String SDK_TAG = "Unity Ads Sdk";
    private static final String TAG     = "Unity Ads Module";


    public static  UnityadsModule instance;
    private static Activity          sCurrentActivity;
    private BannerView bottomBanner;
    private RelativeLayout bottomBannerView;
    public static final int BANNER_WIDTH = 320;
    public static final int BANNER_HEIGHT = 50;

    private Callback mInitCallback;

    // Parent Fields
    private boolean                  isPluginInitialized;
    private boolean                  isSdkInitialized;


    private IUnityAdsLoadListener loadListener = new IUnityAdsLoadListener() {
      @Override
      public void onUnityAdsAdLoaded(String placementId) {
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", placementId );
        sendReactNativeEvent( "OnunityAdsAdLoaded", params );

        UnityAds.show(sCurrentActivity, placementId, new UnityAdsShowOptions(), showListener);
      }

      @Override
      public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
        Log.e("UnityAdsExample", "Unity Ads failed to load ad for " + placementId + " with error: [" + error + "] " + message);
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", placementId );
        params.putString( "error", error.toString() );
        params.putString( "message", message );

        sendReactNativeEvent( "OnunityAdsAdFailedToLoad", params );
      }
    };

    private IUnityAdsShowListener showListener = new IUnityAdsShowListener() {
      @Override
      public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
        Log.e("UnityAdsExample", "Unity Ads failed to show ad for " + placementId + " with error: [" + error + "] " + message);
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", placementId );
        params.putString( "error", error.toString() );
        params.putString( "message", message );

        sendReactNativeEvent( "OnunityAdsShowFailed", params );
      }

      @Override
      public void onUnityAdsShowStart(String placementId) {
        Log.v("UnityAdsExample", "onUnityAdsShowStart: " + placementId);
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", placementId );
        sendReactNativeEvent( "OnunityAdsShowStart", params );
      }

      @Override
      public void onUnityAdsShowClick(String placementId) {
        Log.v("UnityAdsExample", "onUnityAdsShowClick: " + placementId);
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", placementId );
        sendReactNativeEvent( "OnunityAdsShowClick", params );
      }

      @Override
      public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
        Log.v("UnityAdsExample", "onUnityAdsShowComplete: " + placementId);
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", placementId );
        params.putInt( "state", state.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)?1:0 );

        sendReactNativeEvent( "OnunityAdsShowComplete", params );
      }
    };

    // Listener for banner events:
    private BannerView.IListener bannerListener = new BannerView.IListener() {
      @Override
      public void onBannerLoaded(BannerView bannerAdView) {
        // Called when the banner is loaded.
        Log.v("UnityAdsExample", "onBannerLoaded: " + bannerAdView.getPlacementId());
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", bannerAdView.getPlacementId() );

        sendReactNativeEvent( "bannerViewDidLoad", params );
        loadBannerView();
      }

      @Override
      public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
        Log.e("UnityAdsExample", "Unity Ads failed to load banner for " + bannerAdView.getPlacementId() + " with error: [" + errorInfo.errorCode + "] " + errorInfo.errorMessage);
        // Note that the BannerErrorInfo object can indicate a no fill (see API documentation).
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", bannerAdView.getPlacementId() );
        params.putString( "code", errorInfo.errorCode.toString() );
        params.putString( "message", errorInfo.errorMessage );

        sendReactNativeEvent( "OnbannerViewDidError", params );
      }

      @Override
      public void onBannerClick(BannerView bannerAdView) {
        // Called when a banner is clicked.
        Log.v("UnityAdsExample", "onBannerClick: " + bannerAdView.getPlacementId());
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", bannerAdView.getPlacementId() );

        sendReactNativeEvent( "OnbannerViewDidClick", params );
      }

      @Override
      public void onBannerLeftApplication(BannerView bannerAdView) {
        // Called when the banner links out of the application.
        Log.v("UnityAdsExample", "onBannerLeftApplication: " + bannerAdView.getPlacementId());
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", bannerAdView.getPlacementId() );

        sendReactNativeEvent( "OnbannerViewDidLeaveApplication", params );
      }
    };


    public static UnityadsModule getInstance()
    {
      return instance;
    }

    public UnityadsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        instance = this;
        sCurrentActivity = reactContext.getCurrentActivity();
    }

    @Override
    public void onInitializationComplete() {
      Log.d(TAG, "SDK initialized" );
      isSdkInitialized = true;
      mInitCallback.invoke( "success" );

    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
      mInitCallback.invoke( "Unity Sdk has Initiallized" +message + " error: " +error);

    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @Nullable
    private Activity maybeGetCurrentActivity()
    {
      // React Native has a bug where `getCurrentActivity()` returns null: https://github.com/facebook/react-native/issues/18345
      // To alleviate the issue - we will store as a static reference (WeakReference unfortunately did not suffice)
      if ( getReactApplicationContext().hasCurrentActivity() )
      {
        sCurrentActivity = getReactApplicationContext().getCurrentActivity();
      }

      return sCurrentActivity;
    }


    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod(isBlockingSynchronousMethod = true)
    public boolean isInitialized()
    {
      return isPluginInitialized && isSdkInitialized;
    }

    @ReactMethod
    public void initialize(final String sdkKey, int testMode, final Callback callback)
    {
      // Check if Activity is available
      Activity currentActivity = maybeGetCurrentActivity();
      if ( currentActivity != null )
      {
        performInitialization( sdkKey, testMode==1?true:false, currentActivity, callback );
      }
      else
      {
        Log.d( TAG, "No current Activity found! Delaying initialization..." );

        new Handler().postDelayed(new Runnable()
        {
          @Override
          public void run()
          {
            Context contextToUse = maybeGetCurrentActivity();
            if ( contextToUse == null )
            {
              Log.d( TAG,"Still unable to find current Activity - initializing SDK with application context" );
              contextToUse = getReactApplicationContext();
            }

            performInitialization( sdkKey, testMode==1?true:false, contextToUse, callback );
          }
        }, TimeUnit.SECONDS.toMillis( 3 ) );
      }
    }

    @ReactMethod()
    public void loadInterstitial(String adUnitId)
    {
      UnityAds.load(adUnitId, loadListener);
    }

    @ReactMethod()
    public void loadBottomBanner(String adUnitId)
    {
      bottomBanner = new BannerView(sCurrentActivity, adUnitId, new UnityBannerSize(320, 50));
      // Set the listener for banner lifecycle events:
      bottomBanner.setListener(bannerListener);
      bottomBanner.load();

    }

    @ReactMethod()
    public void unLoadBottomBanner()
    {
      if (bottomBannerView != null && sCurrentActivity != null){
        sCurrentActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            bottomBannerView.setVisibility(View.INVISIBLE);
            bottomBannerView.removeAllViews();
            bottomBannerView = null;
            bottomBanner = null;

          }
        });
        WritableMap params = Arguments.createMap();
        params.putString( "adUnitId", "unload banner view" );

        sendReactNativeEvent( "OnbannerViewDidLeaveApplication", params );

      }



    }

    private static  int toPixelUnits(int dipUnit) {
      float density = sCurrentActivity.getResources().getDisplayMetrics().density;
      return Math.round(dipUnit * density);
    }

    private void loadBannerView(){
      sCurrentActivity.runOnUiThread(new Runnable(){

        @Override
        public void run(){
          sCurrentActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
          //sCurrentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏：根据传感器横向切换

          bottomBannerView = new RelativeLayout(sCurrentActivity.getApplicationContext());

          LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
          sCurrentActivity.addContentView(bottomBannerView,lp);

          //RelativeLayout.LayoutParams bannerLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
          //bannerLayoutParams.setMargins(5, 5, 5, 5);

          int width = toPixelUnits(BANNER_WIDTH);
          int height = toPixelUnits(BANNER_HEIGHT);
          RelativeLayout.LayoutParams bannerLayoutParams = new RelativeLayout.LayoutParams(width, height);
          bannerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
          bannerLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

          //bottomBannerView.addView( applovin_adView, new android.widget.FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER ) );
          bottomBannerView.addView( bottomBanner,bannerLayoutParams);


        }
      });
    }

    private void performInitialization(final String sdkKey, boolean testMode, final Context context, final Callback callback)
    {
      // Guard against running init logic multiple times
      if ( isPluginInitialized ) return;

      isPluginInitialized = true;


      // If SDK key passed in is empty
      if ( TextUtils.isEmpty( sdkKey ) )
      {
        throw new IllegalStateException( "Unable to initialize Unity Ads SDK - no SDK key provided!" );
      }

      // Initialize SDK
      mInitCallback = callback;
      UnityAds.initialize(sCurrentActivity, sdkKey, testMode, instance);

    }

    // React Native Bridge
    private void sendReactNativeEvent(final String name, @Nullable final WritableMap params)
    {
      getReactApplicationContext()
        .getJSModule( RCTDeviceEventEmitter.class )
        .emit( name, params );
    }

}
