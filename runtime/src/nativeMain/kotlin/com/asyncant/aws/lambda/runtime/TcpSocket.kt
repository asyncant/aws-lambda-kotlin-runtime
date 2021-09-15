package com.asyncant.aws.lambda.runtime

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.ECONNRESET
import platform.posix.ENOTCONN
import platform.posix.IPPROTO_TCP
import platform.posix.SOCK_STREAM
import platform.posix.close
import platform.posix.connect
import platform.posix.errno
import platform.posix.memset
import platform.posix.recv
import platform.posix.send
import platform.posix.sockaddr_in
import platform.posix.socket

internal actual class TcpSocket actual constructor(ip: String, port: Int) {
  private val fd = connectSocket(ip, port)

  actual fun receiveMessage(builder: MessageReceiver) {
    var received: Long
    val buffer = ByteArray(8192)

    buffer.usePinned {
      do {
        received = recv(fd, it.addressOf(0), buffer.size.toULong(), 0)
        if (received > 0) builder.append(buffer, received.toInt())
      } while (received > 0 && !builder.completed())
    }
  }

  actual fun send(data: ByteArray) {
    var index = 0
    data.usePinned {
      while (index < data.size) {
        val sent = send(fd, it.addressOf(index), (data.size - index).toULong(), 0)
        if (sent < 0) {
          if (errno == ENOTCONN || errno == ECONNRESET) throw NotConnectedException()
          else throw SocketException("Error sending data, error number: $errno")
        }
        index += sent.toInt()
      }
    }
  }

  actual fun close() {
    close(fd)
  }

  private fun connectSocket(ip: String, port: Int) = memScoped {
    val serverAddr = alloc<sockaddr_in>().apply {
      memset(ptr, 0, sizeOf<sockaddr_in>().convert())
      sin_family = AF_INET.convert()
      sin_addr.s_addr = inet_addr(ip)
      sin_port = ((port shr 8) or ((port and 0xff) shl 8)).toUShort()
    }

    val fd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)
    val connected = connect(fd, serverAddr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
    if (connected < 0) {
      close(fd)
      throw SocketException("Error connecting, error number: $errno")
    }

    fd
  }
}

