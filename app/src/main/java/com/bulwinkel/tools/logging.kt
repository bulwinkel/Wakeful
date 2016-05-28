package com.bulwinkel.tools

import android.util.Log

inline fun Any.logd(msgProducer:()->String) {
  Log.d(this.javaClass.simpleName, msgProducer())
}

inline fun Any.loge(e: Throwable? = null, msgProducer:()->String) {
  Log.e(this.javaClass.simpleName, msgProducer(), e)
}

