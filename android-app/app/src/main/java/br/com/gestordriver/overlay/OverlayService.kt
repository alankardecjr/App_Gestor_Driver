package br.com.gestordriver.overlay

import android.app.Service
import android.content.Intent
import android.os.IBinder

class OverlayService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
