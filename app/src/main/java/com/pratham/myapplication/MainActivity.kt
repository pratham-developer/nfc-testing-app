package com.pratham.myapplication

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilter: IntentFilter
    private lateinit var tagTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tagTextView = findViewById(R.id.tagTextView)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Check if NFC is available and enabled
        if (nfcAdapter == null || !nfcAdapter.isEnabled) {
            tagTextView.text = "NFC is not available or not enabled."
            return
        }

        // Configure PendingIntent for foreground dispatch
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set up the intent filter to detect TECH_DISCOVERED NFC tags
        intentFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
    }

    override fun onResume() {
        super.onResume()
        // Enable foreground dispatch to prioritize this app for NFC scanning
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, arrayOf(intentFilter), null)
    }

    override fun onPause() {
        super.onPause()
        // Disable foreground dispatch when the app is not in focus
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("NFC", "New Intent Received: ${intent.action}")

        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            // Get the tag from the intent and display the UID
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let { displayUid(it) }
        } else {
            Log.d("NFC", "NFC action not recognized.")
        }
    }

    private fun displayUid(tag: Tag) {
        // Extract and format the UID from the Tag object
        val uidBytes = tag.id
        val uid = uidBytes.joinToString(":") { String.format("%02X", it) }

        // Display the UID on the screen
        tagTextView.text = "Card UID: $uid"
        Log.d("NFC", "Card UID: $uid")
    }
}
