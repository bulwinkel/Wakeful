package com.bulwinkel.wakeful

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.widget.Button

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    val wakelock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Wakeful")

    val button = findViewById(R.id.button) as Button
    button.setOnClickListener {
      if (wakelock.isHeld){
        wakelock.release()
      } else {
        wakelock.acquire()
      }
    }
  }
}
