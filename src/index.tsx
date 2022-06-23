import { NativeModules, Platform , NativeEventEmitter } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-unityads' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const Unityads = NativeModules.Unityads
  ? NativeModules.Unityads
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const emitter = new NativeEventEmitter(Unityads);
const subscriptions = {};

const addEventListener = (event:string, handler:any) => {
  let subscription = emitter.addListener(event, handler);
  let currentSubscription = subscriptions[event];
  if (currentSubscription) {
    currentSubscription.remove();
  }
  subscriptions[event] = subscription;
};

const removeEventListener = (event:any) => {
  let currentSubscription = subscriptions[event];
  if (currentSubscription) {
    currentSubscription.remove();
    delete subscriptions[event];
  }
};

export default {
  ...Unityads,
  addEventListener,
  removeEventListener,
  // Use callback to avoid need for attaching listeners at top level on each re-render
  initialize(sdkKey:any, bool:any, callback:any) {
    Unityads.initialize(sdkKey, bool, callback); 
  },

};
