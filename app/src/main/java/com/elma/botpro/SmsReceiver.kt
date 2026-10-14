package com.elma.botpro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.telephony.SmsManager
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return
        val pending = goAsync()
        Thread {
            try {
                val extras = intent.extras ?: return@Thread
                val pdus = extras.get("pdus") as? Array<*> ?: return@Thread
                val format = extras.getString("format")
                val messages = pdus.mapNotNull { pdu ->
                    val bytes = pdu as? ByteArray ?: return@mapNotNull null
                    runCatching { SmsMessage.createFromPdu(bytes, format) }.getOrNull()
                }
                if (messages.isEmpty()) return@Thread
                val sender = messages.firstNotNullOfOrNull { it.originatingAddress } ?: return@Thread
                val body = messages.joinToString("") { it.messageBody.orEmpty() }
                val store = BrainStore(context.applicationContext)
                val reply = store.findLocalAnswer(body) ?: return@Thread
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) return@Thread
                val manager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                manager.sendTextMessage(sender, null, reply, null, null)
            } catch (e: Exception) {
                Log.e("ELMA_SMS", "Échec du traitement SMS", e)
            } finally {
                pending.finish()
            }
        }.start()
    }
}
