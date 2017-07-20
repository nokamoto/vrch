package vrch.slackbridge.firebase

import java.io._

import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.StorageOptions

class FirebaseStorageClient(file: String, bucket: String) {
  private[this] val AUDIOS = "audios"

  private[this] val serviceAccount = new FileInputStream(file)

  private[this] val storage = {
    StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.fromStream(serviceAccount)).build().getService
  }

  def upload(filename: String, bytes: Array[Byte]): Unit = {
    val metadata = "audio/wav"
    storage.get(bucket).create(s"$AUDIOS/$filename", bytes, metadata)
  }

  def download(filename: String): Array[Byte] = {
    storage.get(bucket).get(s"$AUDIOS/$filename").getContent()
  }
}
