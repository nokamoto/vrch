package vrch.slackbridge.firebase

import java.io._

import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.{Bucket, StorageOptions}
import vrchcfg.FirebaseCfg

class FirebaseStorageClient(config: FirebaseCfg) {
  import config._

  private[this] val AUDIOS = "audios"

  private[this] val serviceAccount = new FileInputStream(adminsdkJsonPath)

  private[this] val storage = {
    StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.fromStream(serviceAccount)).build().getService
  }

  private[this] def bucket(): Bucket = storage.get(storageBucket)

  def upload(filename: String, bytes: Array[Byte]): Unit = {
    val metadata = "audio/wav"
    bucket().create(s"$AUDIOS/$filename", bytes, metadata)
  }

  def download(filename: String): Array[Byte] = {
    bucket().get(s"$AUDIOS/$filename").getContent()
  }
}
