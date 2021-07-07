package com.asyncant.http

/** A blocking http client with no thread-safety guarantees. */
expect class BlockingHttpClient() {
  fun get(uri: String, headers: Map<String, String>): HttpResponse
  fun post(uri: String, payload: ByteArray, headers: Map<String, String>): HttpResponse
  fun close()
}

data class HttpResponse(val body: ByteArray, val headers: Map<String, List<String>>, val statusCode: Int) {
  override fun toString() = "HttpResponse(status=${statusCode},headers=${headers},body=${body.decodeToString()})"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is HttpResponse) return false

    if (!body.contentEquals(other.body)) return false
    if (headers != other.headers) return false
    if (statusCode != other.statusCode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = body.contentHashCode()
    result = 31 * result + headers.hashCode()
    result = 31 * result + statusCode
    return result
  }
}

