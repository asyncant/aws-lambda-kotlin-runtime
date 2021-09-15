package com.asyncant.aws.lambda.runtime

import com.asyncant.http.HttpResponse

internal class HttpResponseBuilder : MessageReceiver {
  private var statusCode: Int = -1
  private var dataBuffer = ByteArray(16384)
  private var dataSize: Int = 0
  private lateinit var headers: Map<String, List<String>>
  private var contentLength: Int = -1

  override fun append(data: ByteArray, size: Int) {
    if (dataSize + size > dataBuffer.size) {
      dataBuffer = dataBuffer.copyOf(dataSize + size * 4)
    }
    data.copyInto(dataBuffer, dataSize, endIndex = size)
    dataSize += size

    if (statusCode != -1) return
    val separatorIndex = dataBuffer.indexOfHeaderBodySeparator(dataSize)
    if (separatorIndex == -1) return

    val headerLines = dataBuffer.decodeToString(endIndex = separatorIndex).split("\r\n")

    val statusLine = headerLines.first()
    check(statusLine.startsWith("HTTP/1.1")) { "Expected HTTP/1.1 in response." }
    statusCode = statusLine.split(' ', limit = 3)[1].toInt()

    headers = headerLines.subList(1, headerLines.size)
      .map { it.split(": ", limit = 2) }
      .groupBy({ it[0] }, { it[1] })

    contentLength = checkNotNull(headers["Content-Length"]?.get(0)?.toInt()) { "Expected Content-Length header." }

    dataBuffer = dataBuffer.copyInto(ByteArray(contentLength), startIndex = separatorIndex + 4, endIndex = dataSize)
    dataSize = dataSize - separatorIndex - 4
  }

  fun build(): HttpResponse {
    return HttpResponse(dataBuffer, headers, statusCode)
  }

  override fun completed() = statusCode != -1 && dataSize == contentLength
}

private tailrec fun ByteArray.indexOfHeaderBodySeparator(size: Int, start: Int = 0): Int {
  if (start + 4 > size) return -1
  if (
    this[start] == 13.toByte() && this[start + 1] == 10.toByte() &&
    this[start + 2] == 13.toByte() && this[start + 3] == 10.toByte()
  ) return start
  return this.indexOfHeaderBodySeparator(size, start + 1)
}
