package com.asyncant.http

import com.asyncant.http.HttpResponseBuilder.HttpResponseState.BODY
import com.asyncant.http.HttpResponseBuilder.HttpResponseState.CHUNKS
import com.asyncant.http.HttpResponseBuilder.HttpResponseState.DONE
import com.asyncant.http.HttpResponseBuilder.HttpResponseState.HEADER
import com.asyncant.io.MessageReceiver

internal class HttpResponseBuilder : MessageReceiver {
  private var state: HttpResponseState = HEADER
  private var statusCode: Int = -1
  private var dataBuffer = ByteArray(16384)
  private var dataSize: Int = 0
  private var contentLength: Int = -1
  private lateinit var headers: Map<String, List<String>>

  override fun append(data: ByteArray, size: Int) {
    when (state) {
      HEADER -> {
        appendToBuffer(data, size)

        val separatorIndex = dataBuffer.indexOfHeaderBodySeparator(dataSize)
        if (separatorIndex == -1) return

        val headerLines = dataBuffer.decodeToString(endIndex = separatorIndex).split("\r\n")

        val statusLine = headerLines.first()
        check(statusLine.startsWith("HTTP/1.1")) { "Expected HTTP/1.1 in response." }
        statusCode = statusLine.split(' ', limit = 3)[1].toInt()

        headers = headerLines.subList(1, headerLines.size)
          .map { it.split(": ", limit = 2) }
          .groupBy({ it[0] }, { it[1] })

        if (headers["Transfer-Encoding"]?.get(0) == "chunked") {
          dataBuffer = dataBuffer.copyInto(dataBuffer, 0, separatorIndex + 4, dataSize)
          dataSize = dataSize - separatorIndex - 4
          state = if (readChunksToBuffer()) DONE else CHUNKS
        } else {
          contentLength = checkNotNull(headers["Content-Length"]?.get(0)?.toInt()) { "Expected Content-Length header." }

          dataBuffer = dataBuffer.copyInto(ByteArray(contentLength), 0, separatorIndex + 4, dataSize)
          dataSize = dataSize - separatorIndex - 4
          state = if (dataSize == contentLength) DONE else BODY
        }
      }

      BODY -> {
        appendToBuffer(data, size)
        if (dataSize == contentLength) state = DONE
      }

      CHUNKS -> {
        appendToBuffer(data, size)

        // Check if last 5 bytes are the last chunk, 0\r\n\r\n
        if (
          dataSize >= 5 && dataBuffer[dataSize - 5] == 48.toByte() && dataBuffer[dataSize - 4] == 13.toByte() &&
          dataBuffer[dataSize - 3] == 10.toByte() && dataBuffer[dataSize - 2] == 13.toByte() &&
          dataBuffer[dataSize - 1] == 10.toByte()
        ) {
          if (readChunksToBuffer()) state = DONE
        }
      }

      DONE -> error("Response builder is already in done state.")
    }
  }

  fun build(): HttpResponse = HttpResponse(dataBuffer, headers, statusCode)

  override fun completed() = state == DONE

  private fun readChunksToBuffer(): Boolean {
    val data = ByteArray(dataSize)
    var dataIndex = 0
    var readIndex = 0
    while (readIndex < dataSize) {
      val line = dataBuffer.readLine(readIndex, dataSize) ?: return false
      readIndex += line.length + 2

      val chunkSize = line.substringBefore(';').toInt(16)
      if (chunkSize == 0) {
        dataSize = dataIndex
        dataBuffer = data.copyInto(ByteArray(dataIndex), endIndex = dataIndex)
        return true
      }

      dataBuffer.copyInto(data, startIndex = readIndex, endIndex = readIndex + chunkSize, destinationOffset = dataIndex)
      readIndex += chunkSize + 2
      dataIndex += chunkSize
    }

    return false
  }

  private fun appendToBuffer(data: ByteArray, size: Int) {
    if (dataSize + size > dataBuffer.size) dataBuffer = dataBuffer.copyOf(dataSize + size * 4)
    data.copyInto(this.dataBuffer, this.dataSize, endIndex = size)
    dataSize += size
  }

  private fun ByteArray.readLine(start: Int, end: Int): String? {
    for (i in start until end - 1) {
      if (this[i] == 13.toByte() && this[i + 1] == 10.toByte()) return decodeToString(start, i)
    }
    return null
  }

  private enum class HttpResponseState {
    HEADER,
    BODY,
    CHUNKS,
    DONE
  }

  private tailrec fun ByteArray.indexOfHeaderBodySeparator(size: Int, start: Int = 0): Int {
    if (start + 4 > size) return -1
    if (
      this[start] == 13.toByte() && this[start + 1] == 10.toByte() &&
      this[start + 2] == 13.toByte() && this[start + 3] == 10.toByte()
    ) return start
    return this.indexOfHeaderBodySeparator(size, start + 1)
  }
}
