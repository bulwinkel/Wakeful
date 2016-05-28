package com.bulwinkel.wakeful

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.os.PowerManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.bulwinkel.tools.logd
import com.bulwinkel.tools.loge
import java.io.FileDescriptor
import java.io.PrintWriter

class WakefulTileService : TileService() {

  private val powerManager by lazy { getSystemService(Context.POWER_SERVICE) as PowerManager }
  private val wakelock by lazy { powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Wakeful") }

  private var tileState = Tile.STATE_INACTIVE
  private fun setTileState(tile: Tile, state: Int) {
    tileState = state
    tile.state = state
    tile.updateTile()
  }

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
        setTileState(tile, Tile.STATE_INACTIVE)
        logd { "wakelock released, state = ${tile.state}" }
        stopSelf()
      } else {
        wakelock.acquire()
        setTileState(tile, Tile.STATE_ACTIVE)
        logd { "wakelock aquired, state = ${tile.state}" }
        startService(Intent(this, WakefulTileService::class.java))
      }
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
    setTileState(qsTile, tileState)
  }

  override fun onBind(intent: Intent?): IBinder? {
    logd { "onBind(intent: $intent, action: ${intent?.action}, extras: ${intent?.extras})" }
    return super.onBind(intent)
  }

  override fun onStopListening() {
    logd { "onStopListening" }
    super.onStopListening()
  }

  override fun onConfigurationChanged(newConfig: Configuration?) {
    logd { "onConfigurationChanged(newConfig: $newConfig)" }
    super.onConfigurationChanged(newConfig)
  }

  override fun onRebind(intent: Intent?) {
    logd { "onRebind(intent: $intent, action: ${intent?.action}, extras: ${intent?.extras})" }
    super.onRebind(intent)
  }

  override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
    logd { "dump(fileDescriptor: $fd, writer: $writer, args: $args)" }
    super.dump(fd, writer, args)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    logd { "onStartCommand(intent: $intent, action: ${intent?.action}, extras: ${intent?.extras}, flags: $flags, startId: $startId)" }
    return super.onStartCommand(intent, flags, startId)
  }

  override fun onLowMemory() {
    logd { "onLowMemory" }
    super.onLowMemory()
  }

  override fun onTaskRemoved(intent: Intent?) {
    logd { "onTaskRemoved(intent: $intent, action: ${intent?.action}, extras: ${intent?.extras})" }
    super.onTaskRemoved(intent)
  }

  override fun onTrimMemory(level: Int) {
    logd { "onTrimMemory(level: $level)" }
    super.onTrimMemory(level)
  }

  override fun onUnbind(intent: Intent?): Boolean {
    logd { "onUnbind(intent: $intent, action: ${intent?.action}, extras: ${intent?.extras})" }
    return super.onUnbind(intent)
  }

  override fun onDestroy() {
    logd { "onDestroy" }
    super.onDestroy()
  }
}