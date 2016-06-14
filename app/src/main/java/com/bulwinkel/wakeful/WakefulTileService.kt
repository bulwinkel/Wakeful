package com.bulwinkel.wakeful

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.PowerManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.bulwinkel.tools.logd
import com.bulwinkel.tools.loge
import java.lang.ref.WeakReference

class WakefulTileService : TileService() {

  private val ID_NOTIFICATION = 1
  private val ID_DONE_INTENT = 1001
  private val ACTION_STAY_ALIVE = "ACTION_STAY_ALIVE"
  private val ACTION_ALLOW_SLEEP = "ACTION_ALLOW_SLEEP"

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
      val intent = Intent(this, WakefulTileService::class.java).setAction(ACTION_STAY_ALIVE)
      startService(intent)
      showNotification()
      registerReceiver(broadcastReceiver, intentFilter)
      //yah - todo - show a notification while the service is running - remove when screen goes off
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
      removeNotification()
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

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

    intent?.let {
      when (it.action) {
        ACTION_ALLOW_SLEEP -> releaseWakeLock()
        else -> logd { "onStartCommand: not processing intent with action: ${intent.action}" }
      }
    }

    return super.onStartCommand(intent, flags, startId)
  }

  private fun showNotification() {

    val title = getString(R.string.app_name)

    val doneIntent = Intent(this, this.javaClass).setAction(ACTION_ALLOW_SLEEP)
    val donePendingIntent = PendingIntent.getService(this, ID_DONE_INTENT, doneIntent, FLAG_UPDATE_CURRENT);

    val doneAction = Notification.Action.Builder(null, "Done", donePendingIntent).build()

    val builder = Notification.Builder(this)
        .setSmallIcon(R.drawable.ic_notification_default)
        .setContentTitle(title)
        .setActions(doneAction)

    startForeground(ID_NOTIFICATION, builder.build())
  }

  private fun removeNotification() {
    stopForeground(true)
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