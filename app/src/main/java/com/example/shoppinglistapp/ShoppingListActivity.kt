package com.example.shoppinglistapp

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ShoppingListActivity : AppCompatActivity() {

    private val channelId = "shopping_list_notification"
    private lateinit var itemsTextView: TextView
    private var itemsList: ArrayList<ShoppingItem>? = null

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Send the notification.
            sendNotification(itemsList?.get(0)?.name ?: "Item")
        } else {
            // Inform the user that the permission was not granted.
            showAlert("Permission Required", "Notification permission is required to notify you when an item is purchased.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        itemsTextView = findViewById(R.id.itemsTextView)
        val purchaseButton = findViewById<Button>(R.id.purchaseButton)

        // Retrieve the item list passed from MainActivity
        itemsList = intent.getParcelableArrayListExtra("ITEM_LIST")
        if (itemsList != null && itemsList!!.isNotEmpty()) {
            val itemsText = itemsList!!.joinToString(separator = "\n") { "${it.name}: ${it.price}" }
            itemsTextView.text = "Selected Items:\n$itemsText"
        } else {
            itemsTextView.text = "No items selected."
        }

        // Set the OnClickListener for the "Mark as Purchased" button
        purchaseButton.setOnClickListener {
            // Check if the notification permission is granted (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Permission is granted, send the notification
                        sendNotification(itemsList?.get(0)?.name ?: "Item")
                    }
                    else -> {
                        // Request the permission
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                // For Android 12 and below, directly send the notification
                sendNotification(itemsList?.get(0)?.name ?: "Item")
            }
        }

        // Create the notification channel if necessary
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Shopping List Notification"
            val descriptionText = "Notification for Shopping List"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(itemName: String) {
        val intent = Intent(this, ShoppingListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Prebuilt system icon
            .setContentTitle("Item Purchased")
            .setContentText("You purchased: $itemName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(0, builder.build())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clearList -> {
                // Clear the displayed items
                itemsTextView.text = ""
                true
            }
            R.id.analytics -> {
                // Show analytics information
                showAnalytics()
                true
            }
            R.id.settings -> {
                // Open the settings screen or show a settings dialog
                openSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAnalytics() {
        if (itemsList == null || itemsList!!.isEmpty()) {
            showAlert("No data", "No items to analyze.")
            return
        }

        val totalSpent = itemsList!!.sumOf { it.price.removePrefix("$").toDouble() }
        val itemCount = itemsList!!.size
        val mostExpensiveItem = itemsList!!.maxByOrNull { it.price.removePrefix("$").toDouble() }

        val message = """
            Total Spending: $$totalSpent
            Item Count: $itemCount
            Most Expensive Item: ${mostExpensiveItem?.name ?: "N/A"} (${mostExpensiveItem?.price ?: "N/A"})
        """.trimIndent()

        showAlert("Analytics", message)
    }

    private fun openSettings() {
        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setMessage("Settings options can be implemented here.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
