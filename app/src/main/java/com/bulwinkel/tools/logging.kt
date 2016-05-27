package com.bulwinkel.tools

import android.util.Log

inline fun Any.logd(msgProducer:()->String) {
  Log.d(this.javaClass.simpleName, msgProducer())
}

