import { NativeModules, Platform } from 'react-native';
import { ShareReq, QQResult, QQResultCode } from './type';
const { BCQQsdk } = NativeModules;

export function registerApp(
  appId: string,
  enableUniversalLink: boolean = false,
  universalLink?: string
): Promise<void> {
  if (Platform.OS === 'android') {
    return BCQQsdk.registerApp(appId);
  } else {
    return BCQQsdk.registerApp(appId, enableUniversalLink, universalLink);
  }
}

export async function shareMessage(message: ShareReq) {
  const share = BCQQsdk.shareMessage;
  if (share) {
    const result: QQResult = await BCQQsdk.shareMessage(message);
    if (result.errCode !== QQResultCode.success) {
      throw new QQError(result);
    }
    return result;
  } else {
    throw Error('Native method not found');
  }
}

export class QQError extends Error {
  code: number;
  constructor(result: QQResult) {
    const message = result.errStr || result.errCode.toString();
    super(message);
    this.code = result.errCode;
    this.name = 'QQError';

    Object.setPrototypeOf(this, QQError.prototype);
  }
}
