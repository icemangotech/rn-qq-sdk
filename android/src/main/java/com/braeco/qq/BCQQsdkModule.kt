package com.braeco.qq

import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.common.util.UriUtil
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.facebook.react.bridge.*
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzonePublish
import com.tencent.connect.share.QzoneShare
import com.tencent.tauth.Tencent
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class BCQQsdkModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val INVOKE_FAILED = "registerApp required."
  private val NOT_REGISTERED = "WeChat API invoke returns false."
  private val NOT_SUPPORTED = "Feature unsupported"


  private var mAppId: String? = null
  private val appName: String = getAppName(reactContext)
  private var mTencent: Tencent? = null

  override fun getName(): String {
    return "BCQQsdk"
  }

  @ReactMethod
  fun registerApp(appId: String, promise: Promise) {
    mAppId = appId
    if (mTencent == null) {
      mTencent = Tencent.createInstance(appId, reactContext)
    }
    promise.resolve(null)
  }

  @ReactMethod
  fun shareMessage(data: ReadableMap, promise: Promise) {
    if (mTencent == null) {
      promise.reject("", NOT_REGISTERED)
      return
    }
    val thumbUrl = if (data.hasKey("thumbUrl")) extractString(data, "thumbUrl") else ""
    if (thumbUrl?.isNotEmpty() == true) {
      loadImage(thumbUrl, true) {
        shareData(data, it, promise)
      }
    } else {
      shareData(data, null, promise)
    }
  }

  private fun shareData(data: ReadableMap, thumb: Bitmap?, promise: Promise) {
    val type = QQShareType.fromInt(data.getInt("type"))
    val scene = QQShareScene.fromInt(data.getInt("scene"))
    val title = extractString(data, "title")
    val description = extractString(data, "description")

    val bundle = Bundle()

    when(type) {
      QQShareType.WXShareTypeText -> {
        when(scene) {
          QQShareScene.Session -> {
            promise.reject("", NOT_SUPPORTED)
          }
          QQShareScene.QZone -> {
            bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
            bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, extractString(data, "text"))
            shareToQzone(bundle)
          }
        }
      }
      QQShareType.WXShareTypeImage -> {
        val imageUrl = extractString(data, "imageUrl")
        loadImage(imageUrl, false) {
          if (it != null) {
            val filePath = saveBitmapToFile(it)
            when(scene) {
              QQShareScene.Session -> {
                bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, filePath);
                bundle.putString(QQShare.SHARE_TO_QQ_TITLE, title);
                bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, description);
              }
              QQShareScene.QZone -> {
                bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, filePath);
                bundle.putString(QQShare.SHARE_TO_QQ_TITLE, title);
                bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, description);
                bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT,QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
              }
            }
            shareToQQ(bundle)
          } else {
            promise.reject("", "Loading image failed.")
          }
        }
      }
      QQShareType.WXShareTypeMusic -> {
        val musicUrl = extractString(data, "musicUrl")
        val thumbPath = if (thumb != null) saveBitmapToFile(thumb) else null
        when(scene) {
          QQShareScene.Session -> {
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
            if(thumbPath != null) {
              bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, thumbPath);
            }
            bundle.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, extractString(data, "flashUrl"))
            bundle.putString(QQShare.SHARE_TO_QQ_TITLE, title)
            bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, musicUrl)
            bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, description)
            bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName)

            shareToQQ(bundle)
          }
          QQShareScene.QZone -> {
            val imageUrls = ArrayList<String>()
            if (thumbPath != null) imageUrls.add(thumbPath)
            bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
            bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, title)
            bundle.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, description)
            bundle.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, musicUrl)
            bundle.putString(QzoneShare.SHARE_TO_QQ_AUDIO_URL, extractString(data, "flashUrl"))
            bundle.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls)

            shareToQzone(bundle)
          }
        }
      }
    }

  }

  private fun shareToQQ(bundle: Bundle) {

  }

  private fun shareToQzone(bundle: Bundle) {

  }

  /**
   * 获取应用的名称
   * @param reactContext
   * @return
   */
  private fun getAppName(reactContext: ReactApplicationContext): String {
    val packageManager: PackageManager = reactContext.packageManager
    var applicationInfo: ApplicationInfo? = null
    try {
      applicationInfo = packageManager.getApplicationInfo(reactContext.packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
    }
    return ((if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "").toString())
  }

  private fun loadImage(url: String?, shouldDownSample: Boolean, completionCallback: (Bitmap?) -> Unit) {
    var imageUri: Uri?
    try {
      imageUri = Uri.parse(url)

      if (imageUri.scheme == null) {
        imageUri = getResourceDrawableUri(reactApplicationContext, url)
      }
    } catch (e: Exception) {
      imageUri = null
    }

    if (imageUri != null) {
      val resizeOptions = if (shouldDownSample) ResizeOptions(100, 100) else null
      getImage(imageUri, resizeOptions) {
        completionCallback(it)
      }
    } else {
      completionCallback(null)
    }
  }

  private fun getImage(uri: Uri, resizeOptions: ResizeOptions?, completionCallback: (Bitmap?) -> Unit) {
    val dataSubscriber = object : BaseBitmapDataSubscriber() {
      override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
        completionCallback(null)
      }

      /**
       * The bitmap provided to this method is only guaranteed to be around for the lifespan of the
       * method.
       *
       *
       * The framework will free the bitmap's memory after this method has completed.
       * @param bitmap
       */
      override fun onNewResultImpl(bitmap: Bitmap?) {
        if (bitmap != null) {
          if (bitmap.config != null) {
            completionCallback(bitmap.copy(bitmap.config, true))
          } else {
            completionCallback(bitmap.copy(Bitmap.Config.ARGB_8888, true))
          }
        } else {
          completionCallback(null)
        }
      }

    }

    val imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
    if (resizeOptions != null) {
      imageRequestBuilder.resizeOptions = resizeOptions
    }

    val imageRequest = imageRequestBuilder.build()
    val imagePipeline = Fresco.getImagePipeline()
    val dataSource = imagePipeline.fetchDecodedImage(imageRequest, null)
    dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance())

  }

  private fun getResourceDrawableUri(context: Context, name: String?): Uri? {
    if (name == null || name.isEmpty()) {
      return null
    }
    val imageName = name.toLowerCase().replace("-", "_")
    val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

    return if (resId == 0) null else {
      Uri.Builder().scheme(UriUtil.LOCAL_RESOURCE_SCHEME).path(resId.toString()).build()
    }
  }

  private fun extractString(data: ReadableMap, key: String): String? {
    return if (data.hasKey(key)) data.getString(key) else null
  }

  private fun saveBitmapToFile(bitmap: Bitmap): String? {
    val pictureFile: File = getOutputMediaFile(null) ?: return null
    try {
      val fos = FileOutputStream(pictureFile)
      bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
      fos.close()
    } catch (e: FileNotFoundException) {
      Log.d(TAG, "File not found: " + e.message)
    } catch (e: IOException) {
      Log.d(TAG, "Error accessing file: " + e.message)
    }
    return pictureFile.absolutePath
  }

  private fun getOutputMediaFile(ext: String? = "jpg"): File? {

    val mediaStorageDir = currentActivity!!.externalCacheDir
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        return null
      }
    }
    val timeStamp: String = SimpleDateFormat("ddMMyyyy_HHmm").format(Date())
    val mediaFile: File
    val mImageName = "RN_$timeStamp.$ext"
    mediaFile = File(mediaStorageDir.path + File.separator + mImageName)
    Log.d("path is", mediaFile.path)
    return mediaFile
  }

}
