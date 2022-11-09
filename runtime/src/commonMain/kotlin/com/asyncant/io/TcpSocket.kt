package com.asyncant.io

internal expect class TcpSocket(ip: String, port: Int) {
  fun receiveMessage(builder: MessageReceiver)
  fun send(data: ByteArray)
  fun close()
}

open class SocketException(message: String) : IllegalStateException(message)
class NotConnectedException : SocketException("The socket is not connected.")

