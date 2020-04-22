export enum QQShareScene {
  Session = 0,
  Qzone,
}

export enum QQShareType {
  QQShareTypeText = 0,
  QQShareTypeImage,
  QQShareTypeMusic,
  QQShareTypeVideo,
  QQShareTypeWeb,
}

export enum QQResultCode {
  success = 0,
  paramError = -1,
  invalidGroup = -2,
  uploadPhotoFailed = -3,
  cancel = -4,
  clientInternalError = -5,
}

export interface QQResult {
  errCode: QQResultCode;
  errStr?: string;
  extendInfo?: string;
}

interface BaseShareReq {
  scene: QQShareScene;
}

export interface ShareTextReq extends BaseShareReq {
  type: QQShareType.QQShareTypeText;
  text: string;
}

interface ShareMediaReq extends BaseShareReq {
  title: string;
  description: string;
  thumbUrl: string;
}

export interface ShareWebpageReq extends ShareMediaReq {
  type: QQShareType.QQShareTypeWeb;
  webpageUrl: string;
}

export interface ShareImageReq extends ShareMediaReq {
  type: QQShareType.QQShareTypeImage;
  imageUrl: string;
}

export type ShareReq = ShareTextReq | ShareImageReq | ShareWebpageReq;
