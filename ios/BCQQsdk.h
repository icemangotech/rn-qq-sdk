#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#if __has_include(<React/RCTImageLoader.h>)
#import <React/RCTImageLoader.h>
#else
#import <React/RCTImageLoaderProtocol.h>
#endif
#import <TencentOpenAPI/QQApiInterface.h>
#import <TencentOpenAPI/TencentOAuth.h>

typedef NS_ENUM(NSInteger, QQShareScene) {
    QQ,
    QQZone,
    Favorite,
};

typedef NS_ENUM(NSInteger, QQShareType) {
    QQShareTypeText        = 0,
    QQShareTypeImage       = 1,
    QQShareTypeMusic       = 2,
    QQShareTypeVideo       = 3,
    QQShareTypeWeb         = 4,
};

typedef void(^ImageDataCallback)(NSData* _Nullable data);

@interface BCQQsdk : RCTEventEmitter <RCTBridgeModule, TencentSessionDelegate, QQApiInterfaceDelegate>

@property (nullable) NSString* appId;
@property (nullable) TencentOAuth* tencentOAuth;

@property (nullable) RCTPromiseResolveBlock shareResolve;
@property (nullable) RCTPromiseRejectBlock shareReject;

@end
