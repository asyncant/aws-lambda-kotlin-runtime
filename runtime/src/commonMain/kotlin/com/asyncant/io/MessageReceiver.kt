package com.asyncant.io

internal interface MessageReceiver {
  fun append(data: ByteArray, size: Int)
  fun completed(): Boolean
}