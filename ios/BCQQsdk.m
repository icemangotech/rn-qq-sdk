#import "BCQQsdk.h"


@implementation BCQQsdk

- (instancetype)init
{
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleOpenURL:) name:@"RCTOpenURLNotification"
                                                   object:nil];
    }
#if DEBUG
    [QQApiInterface startLogWithBlock:^(NSString *logStr) {
        NSLog(@"%@", logStr);
    }];
#endif
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
#if DEBUG
    [QQApiInterface stopLog];
#endif
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[];
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

- (BOOL)handleOpenURL:(NSNotification *)aNotification
{
    NSString * aURLString =  [aNotification userInfo][@"url"];
    NSURL * aURL = [NSURL URLWithString:aURLString];

    return [QQApiInterface handleOpenURL:aURL delegate:self] || [QQApiInterface handleOpenUniversallink:aURL delegate:self] || [TencentOAuth HandleOpenURL:aURL] || [TencentOAuth HandleUniversalLink:aURL];
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(registerApp: (NSString*)appId
                  :(BOOL)enableUL
                  :(NSString*)UL
                  :(RCTPromiseResolveBlock)resolve
                  :(RCTPromiseRejectBlock)reject)
{
    self.appId = appId;
    self.tencentOAuth = [[TencentOAuth alloc] initWithAppId:appId enableUniveralLink:enableUL universalLink:UL delegate:self];
    resolve(nil);
}

RCT_EXPORT_METHOD(openQQ:(RCTPromiseResolveBlock)resolve :(RCTPromiseRejectBlock)reject)
{
    if ([QQApiInterface openQQ]) {
        resolve(nil);
    } else {
        reject(nil, nil, nil);
    }
}

RCT_EXPORT_METHOD(shareMessage: (NSDictionary*)data
                  :(RCTPromiseResolveBlock)resolve
                  :(RCTPromiseRejectBlock)reject)
{
    _shareResolve = resolve;
    _shareReject = reject;

    NSString* thumbUrl = data[@"thumbUrl"];
    if (thumbUrl.length) {
        [self loadImageFromURLString:thumbUrl shouldDownSample:YES completionBlock:^(NSData* thumbnailData) {
            [self shareMessage:data withThumbnail:thumbnailData];
        }];
    } else {
        [self shareMessage:data withThumbnail:nil];
    }
}

- (void)shareMessage: (NSDictionary*)data withThumbnail: (NSData*)thumbnailData
{
    QQShareType type = [data[@"type"] integerValue];
    QQShareScene scene = [data[@"scene"] integerValue];
    NSString* title = data[@"title"];
    NSString* description = data[@"description"];

    switch (type) {
        case QQShareTypeText: {
            NSString* text = data[@"text"];
            QQApiTextObject* obj = [QQApiTextObject objectWithText:text];
            [self shareObject:obj toScene:scene];
            break;
        }
        case QQShareTypeImage: {
            NSString* imageUrl = data[@"imageUrl"];
            [self loadImageFromURLString:imageUrl shouldDownSample:NO completionBlock:^(NSData* data) {

                if (!data) {
                    [self onShareReject:nil :@"Image Load Failed" :nil];
                    return;
                }
                QQApiImageObject* obj = [QQApiImageObject objectWithData:data
                                                        previewImageData:nil
                                                                   title:title
                                                             description:description];
                [self shareObject:obj toScene:scene];
            }];
            break;
        }
        case QQShareTypeWeb: {
            NSString* webpageUrl = data[@"webpageUrl"];

            QQApiNewsObject* obj = [QQApiNewsObject objectWithURL:[NSURL URLWithString:webpageUrl] title:title description:description previewImageData:thumbnailData];
            [self shareObject:obj toScene:scene];
            break;
        }
        case QQShareTypeMusic: {
            NSString* musicUrl = data[@"musicUrl"];
            NSString* flashUrl = data[@"flashUrl"];
            QQApiAudioObject* obj = [QQApiAudioObject objectWithURL:[NSURL URLWithString:musicUrl] title:title description:description previewImageData:thumbnailData];
            if (flashUrl) {
                [obj setFlashURL:[NSURL URLWithString:flashUrl]];
            }
            [self shareObject:obj toScene:scene];
            break;
        }
        case QQShareTypeVideo: {
            NSString* videoUrl = data[@"videoUrl"];
            NSString* flashUrl = data[@"flashUrl"];
            QQApiVideoObject* obj = [QQApiVideoObject objectWithURL:[NSURL URLWithString:videoUrl] title:title description:description previewImageData:thumbnailData];
            if (flashUrl) {
                [obj setFlashURL:[NSURL URLWithString:flashUrl]];
            }
            [self shareObject:obj toScene:scene];
            break;
        }
        default:
            break;
    }
}

- (void)shareObject: (QQApiObject*)object toScene: (QQShareScene)scene
{
    switch (scene) {
        case QQZone:
            [object setCflag:kQQAPICtrlFlagQZoneShareOnStart];
            break;
        case Favorite:
            [object setCflag:kQQAPICtrlFlagQQShareFavorites];
            break;
        default:
            [object setCflag:kQQAPICtrlFlagQQShare];
    }

    SendMessageToQQReq* req = [SendMessageToQQReq reqWithContent:object];

    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        QQApiSendResultCode result = [QQApiInterface sendReq:req];
        if (result != EQQAPISENDSUCESS) {
            NSString* code = [NSString stringWithFormat:@"%ld", (long)result];
            [self onShareReject:code :code :nil];
            // TODO
        }
    }];
}

- (void)onShareResolve: (id)result
{
    if (self.shareResolve) {
        self.shareResolve(result);
    }
    self.shareResolve = nil;
    self.shareReject = nil;
}

- (void)onShareReject: (NSString*)code
                     :(NSString*)message
                     :(NSError*)error
{
    if (self.shareReject) {
        self.shareReject(code, message, error);
    }
    self.shareResolve = nil;
    self.shareReject = nil;
}

- (void)loadImageFromURLString: (NSString*)urlString
              shouldDownSample: (BOOL)shouldDownSample
               completionBlock: (ImageDataCallback)callback
{
    NSURL* url = [NSURL URLWithString:urlString];
    NSURLRequest* imageRequest = [NSURLRequest requestWithURL:url];

    CGSize size = shouldDownSample ? CGSizeMake(100, 100) : CGSizeZero;

    [[self.bridge moduleForName:@"ImageLoader"] loadImageWithURLRequest:imageRequest size:size scale:1 clipped:!shouldDownSample resizeMode:RCTResizeModeStretch progressBlock:nil partialLoadBlock:nil completionBlock: ^(NSError* error, UIImage* image){
        if (image) {
            callback(UIImagePNGRepresentation(image));
        } else {
            callback(nil);
        }
    }];

}

#pragma mark - QQApiInterfaceDelegate

- (void)isOnlineResponse:(NSDictionary *)response {
    //
}

- (void)onReq:(QQBaseReq *)req {
    //
}

- (void)onResp:(QQBaseResp *)r {
    NSMutableDictionary* result = @{
        @"errCode": @([r.result integerValue])
    }.mutableCopy;
    result[@"errStr"] = r.errorDescription;
    result[@"extendInfo"] = r.extendInfo;
    switch (r.type) {
        case ESENDMESSAGETOQQRESPTYPE:
        {
            [self onShareResolve:result];
            break;
        }
        default:
            break;
    }
}

#pragma mark - TencentSessionDelegate

- (void)tencentDidLogin {

}

- (void)tencentDidNotLogin:(BOOL)cancelled {

}

- (void)tencentDidNotNetWork {

}

@end
