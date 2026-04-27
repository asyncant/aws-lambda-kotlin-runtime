package com.asyncant.io

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

internal actual class TcpSocket actual constructor(ip: String, port: Int) {
  private val socket: Socket = Socket().apply {
    tcpNoDelay = true
    connect(InetSocketAddress(ip, port))
  }
  private val input = socket.getInputStream()
  private val output = socket.getOutputStream()

  actual fun receiveMessage(builder: MessageReceiver) {
    val buffer = ByteArray(8192)
    do {
      val received = input.read(buffer)
      if (received > 0) builder.append(buffer, received)
      else if (received == -1) return
    } while (!builder.completed())
  }

  actual fun send(data: ByteArray) {
    try {
      output.write(data)
      output.flush()
    } catch (e: java.net.SocketException) {
      throw NotConnectedException()
    } catch (e: IOException) {
      throw SocketException("Error sending data: ${e.message}")
    }
  }

  actual fun close() {
    socket.close()
  }
}
