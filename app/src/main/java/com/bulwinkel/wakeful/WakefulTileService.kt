package com.bulwinkel.wakeful

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.bulwinkel.tools.logd
import com.bulwinkel.tools.loge

class WakefulTileService : TileService() {

  private val powerManager by lazy { getSystemService(Context.POWER_SERVICE) as PowerManager }
  private val wakelock by lazy { powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Wakeful") }

  override fun onCreate() {
    super.onCreate()
    logd { "onCreate" }
  }

  override fun onClick() {
    super.onClick()
    logd { "onClick" }
    val tile = qsTile
    if (tile != null) {
      if (wakelock.isHeld) {
        wakelock.release()
        tile.state = Tile.STATE_INACTIVE
        logd { "wakelock released, state = ${qsTile.state}" }
      } else {
        wakelock.acquire()
        tile.state = Tile.STATE_ACTIVE
        logd { "wakelock aquired, state = ${qsTile.state}" }
      }
      tile.updateTile()
    } else {
      loge { "qsTile == $tile" }
    }
  }

  override fun onTileRemoved() {
    super.onTileRemoved()
    logd { "onTileRemoved" }
  }

  override fun onTileAdded() {
    super.onTileAdded()
    logd { "onTileAdded" }
  }

  override fun onStartListening() {
    super.onStartListening()
    logd { "onStartListening" }
  }

  override fun onBind(intent: Intent?): IBinder? {
    logd { "onBind($intent)" }
    return super.onBind(intent)
  }

  override fun onStopListening() {
    super.onStopListening()
    logd { "onStopListening" }
  }

  override fun onDestroy() {
    super.onDestroy()
    logd { "onDestroy" }
  }
}