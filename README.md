# @phecdas/rn-qq-sdk

A bridge module of [QQ SDK](https://wiki.connect.qq.com/sdk下载) for react-native, **Share feature only**.

## Version

|  iOS  | Android |
| :---: | :-----: |
| 3.3.8 |  3.3.8  |

[Download page](https://wiki.connect.qq.com/sdk下载)

## Installation

```bash
yarn add @phecdas/rn-qq-sdk
# or
npm install --save @phecdas/rn-qq-sdk
```

Then

``` bash
cd ios; pod install
```

## Config

### iOS

1. Set your URL scheme in `info.plist` , see [iOS 环境搭建](https://wiki.connect.qq.com/ios_sdk环境搭建)
2. Set `LSApplicationQueriesSchemes` in `info.plist`

``` xml
<key>LSApplicationQueriesSchemes</key>
  <array>
    <string>mqqapi</string>
    <string>mqq</string>
    <string>mqqOpensdkSSoLogin</string>
    <string>mqqconnect</string>
    <string>mqqopensdkdataline</string>
    <string>mqqopensdkgrouptribeshare</string>
    <string>mqqopensdkfriend</string>
    <string>mqqopensdkapi</string>
    <string>mqqopensdkapiV2</string>
    <string>mqqopensdkapiV3</string>
    <string>mqzoneopensdk</string>
    <string>wtloginmqq</string>
    <string>wtloginmqq2</string>
    <string>mqqwpa</string>
    <string>mqzone</string>
    <string>mqzonev2</string>
    <string>mqzoneshare</string>
    <string>wtloginqzone</string>
    <string>mqzonewx</string>
    <string>mqzoneopensdkapiV2</string>
    <string>mqzoneopensdkapi19</string>
    <string>mqzoneopensdkapi</string>
    <string>mqqopensdkapiv4</string>
  </array>
```

3. Set the universal link, see [doc](https://wiki.connect.qq.com/qq互联ios3-3-6-sdk版本支持了universal-links的跳转方式，请开发者及时)

### Android

Add these codes into `android/app/src/main/AndroidManifest.xml` , and **set appId**.

``` xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<application>
 <activity
       android:name="com.tencent.tauth.AuthActivity"
       android:noHistory="true"
       android:launchMode="singleTask" >
    <intent-filter>
           <action android:name="android.intent.action.VIEW" />
           <category android:name="android.intent.category.DEFAULT" />
           <category android:name="android.intent.category.BROWSABLE" />
           <data android:scheme="tencent你的AppId" />
    </intent-filter>
 </activity>
<activity
       android:name="com.tencent.connect.common.AssistActivity"
       android:configChanges="orientation|keyboardHidden"
       android:screenOrientation="behind"
       android:theme="@android:style/Theme.Translucent.NoTitleBar" />
<application>
```

## Usage

Call `registerApp` once at your app's beginning, for example:

``` typescript
// App.tsx
import QQSDK from '@phecdas/rn-qq-sdk';

QQSDK.registerApp('yourAppId', true, 'https://your.universal.link')
```

## LICENCE

BSD-3-Clause
