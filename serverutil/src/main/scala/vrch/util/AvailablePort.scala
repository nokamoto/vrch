package vrch.util

import java.net.ServerSocket

trait AvailablePort {
  def availablePort(): Int = {
    val socket = new ServerSocket(0)
    try {
      socket.getLocalPort
    } finally {
      socket.close()
    }
  }
}
