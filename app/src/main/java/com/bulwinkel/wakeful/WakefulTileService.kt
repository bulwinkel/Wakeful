package com.bulwinkel.wakeful

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.IBinder
import android.os.PowerManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.bulwinkel.tools.logd
import com.bulwinkel.tools.loge
import java.io.FileDescriptor
import java.io.PrintWriter
import java.lang.ref.WeakReference

class WakefulTileService : TileService() {

  private val powerManager by lazy { getSystemService(Context.POWER_SERVICE) as PowerManager }
  private val wakelock by lazy { powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Wakeful") }
  private val broadcastReceiver by lazy { WakefulBroadcastReceiver(this) }

  private val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)

  private var tileState = Tile.STATE_INACTIVE
  private fun setTileState(tile: Tile, state: Int) {
    tileState = state
    tile.state = state
    tile.updateTile()
  }

  private fun acquireWakeLock() {
    val tile = qsTile
    if (tile != null) {
      wakelock.acquire()
      setTileState(tile, Tile.STATE_ACTIVE)
      startService(Intent(this, WakefulTileService::class.java))
      registerReceiver(broadcastReceiver, intentFilter)
      logd { "wakelock aquired, state = ${tile.state}" }
    } else {
      loge { "qsTile == $tile" }
    }
  }

  fun releaseWakeLock() {
    val tile = qsTile
    if (tile != null) {
      wakelock.release()
      setTileState(tile, Tile.STATE_INACTIVE)
      logd { "wakelock released, state = ${tile.state}" }
      unregisterReceiver(broadcastReceiver)
      stopSelf()
    } else {
      loge { "qsTile == $tile" }
    }
  }

  override fun onCreate() {
    super.onCreate()
    logd { "onCreate" }
  }

  override fun onClick() {
    super.onClick()
    logd { "onClick" }
    if (wakelock.isHeld) {
      releaseWakeLock()
    } else {
      acquireWakeLock()
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

private class WakefulBroadcastReceiver(service: WakefulTileService) : BroadcastReceiver() {

  private val weakService: WeakReference<WakefulTileService>

  init {
    weakService = WeakReference(service)
  }

  override fun onReceive(p0: Context?, p1: Intent?) {
    val service = weakService.get()
    logd { "onReceive: service == $service" }
    service?.releaseWakeLock()
  }

}