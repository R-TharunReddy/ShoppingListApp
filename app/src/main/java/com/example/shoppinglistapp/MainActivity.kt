package com.example.shoppinglistapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ShoppingListAdapter
    private var shoppingList = mutableListOf<ShoppingItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val itemInput = findViewById<EditText>(R.id.itemInput)
        val itemPriceInput = findViewById<EditText>(R.id.itemPriceInput)
        val addButton = findViewById<Button>(R.id.addButton)
        val markAllPurchasedButton = findViewById<Button>(R.id.markAllPurchasedButton)
        val clearAllButton = findViewById<Button>(R.id.clearAllButton) // Clear All Button
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        adapter = ShoppingListAdapter(shoppingList) { item, action ->
            when (action) {
                "DELETE" -> deleteItem(item)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            val itemName = itemInput.text.toString()
            val itemPrice = itemPriceInput.text.toString()
            if (itemName.isNotEmpty() && itemPrice.isNotEmpty()) {
                val formattedPrice = "$$itemPrice"
                shoppingList.add(ShoppingItem(itemName, formattedPrice))
                adapter.notifyDataSetChanged()
                itemInput.text.clear()
                itemPriceInput.text.clear()
            }
        }

        markAllPurchasedButton.setOnClickListener {
            markAllAsPurchased()
        }

        clearAllButton.setOnClickListener {
            clearAllItems()
        }
    }

    private fun deleteItem(item: ShoppingItem) {
        shoppingList.remove(item)
        adapter.notifyDataSetChanged()
    }

    private fun markAllAsPurchased() {
        val intent = Intent(this, ShoppingListActivity::class.java)
        intent.putParcelableArrayListExtra("ITEM_LIST", ArrayList(shoppingList))
        startActivity(intent)
    }

    private fun clearAllItems() {
        shoppingList.clear()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clearList -> {
                clearAllItems()
                true
            }
            R.id.settings -> {
                openSettings()
                true
            }
            R.id.analytics -> {
                showAnalytics()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openSettings() {
        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setMessage("Settings options can be implemented here.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAnalytics() {
        if (shoppingList.isEmpty()) {
            showAlert("No data", "No items to analyze.")
            return
        }

        val totalSpent = shoppingList.sumOf { it.price.removePrefix("$").toDouble() }
        val itemCount = shoppingList.size
        val mostExpensiveItem = shoppingList.maxByOrNull { it.price.removePrefix("$").toDouble() }

        val message = """
            Total Spending: $$totalSpent
            Item Count: $itemCount
            Most Expensive Item: ${mostExpensiveItem?.name ?: "N/A"} (${mostExpensiveItem?.price ?: "N/A"})
        """.trimIndent()

        showAlert("Analytics", message)
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
