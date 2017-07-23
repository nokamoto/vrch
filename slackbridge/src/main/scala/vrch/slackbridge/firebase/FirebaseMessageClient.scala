package vrch.slackbridge.firebase

import java.io.FileInputStream

import com.google.firebase.auth.FirebaseCredentials
import com.google.firebase.database._
import com.google.firebase.tasks.{OnCompleteListener, Task}
import com.google.firebase.{FirebaseApp, FirebaseOptions}

import scala.concurrent.{Future, Promise}

class FirebaseMessageClient(config: FirebaseConfig, room: String) extends vrch.Logger {
  import config._

  private[this] val serviceAccount = new FileInputStream(json)

  private[this] val options = new FirebaseOptions.Builder()
    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
    .setDatabaseUrl(url)
    .build()

  FirebaseApp.initializeApp(options)

  private def roomRef(): DatabaseReference = FirebaseDatabase.getInstance().getReference(s"/rooms/$room")

  private def messageRef(): DatabaseReference = FirebaseDatabase.getInstance().getReference(s"/messages/$room")

  def context(): Future[FirebaseRoom] = {
    val promise = Promise[FirebaseRoom]

    roomRef().addListenerForSingleValueEvent(new ValueEventListener {
      override def onCancelled(error: DatabaseError): Unit = promise.failure(error.toException)

      override def onDataChange(snapshot: DataSnapshot): Unit = {
        try {
          promise.success(FirebaseRoom(snapshot))
        } catch {
          case e: Exception => promise.failure(e)
        }
      }
    })

    promise.future
  }

  def addListener(callback: FirebaseMessage => Unit): Unit = {
    messageRef().orderByChild(FirebaseMessage.CREATED_AT).startAt(System.currentTimeMillis()).addChildEventListener(
      new ChildEventListener {
        override def onCancelled(error: DatabaseError): Unit = logger.error("cancelled.", error.toException)

        override def onChildChanged(snapshot: DataSnapshot, previousChildName: String): Unit = ()

        override def onChildMoved(snapshot: DataSnapshot, previousChildName: String): Unit = ()

        override def onChildAdded(snapshot: DataSnapshot, previousChildName: String): Unit = {
          try {
            val message = FirebaseMessage(snapshot)

            logger.info(s"added: $message")

            callback(message)
          } catch {
            case e: Exception => logger.error("on child added failed.", e)
          }
        }

        override def onChildRemoved(snapshot: DataSnapshot): Unit = ()
      }
    )
  }

  def write(message: FirebaseMessage): Future[Unit] = {
    logger.info(s"write: $message")

    val promise = Promise[Unit]
    val pushed = messageRef().push().getKey

    messageRef().child(pushed).setValue(message.toMap).addOnCompleteListener(new OnCompleteListener[Void] {
      override def onComplete(task: Task[Void]): Unit = {
        if (task.isSuccessful) promise.success(()) else promise.failure(task.getException)
      }
    })

    promise.future
  }
}
