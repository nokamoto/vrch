package vrch

import scala.concurrent.duration._

object ImplicitDuration {
  implicit class DurationTo(d: com.google.protobuf.duration.Duration) {
    def duration: FiniteDuration = d.seconds.seconds + d.nanos.nanos
  }
}
