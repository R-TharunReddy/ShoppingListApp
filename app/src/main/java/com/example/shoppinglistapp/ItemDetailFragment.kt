package com.example.shoppinglistapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ItemDetailFragment : Fragment() {

    private lateinit var itemNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_detail, container, false)
        itemNameTextView = view.findViewById(R.id.itemNameTextView)
        return view
    }

    fun setItemName(itemName: String) {
        itemNameTextView.text = itemName
    }
}
