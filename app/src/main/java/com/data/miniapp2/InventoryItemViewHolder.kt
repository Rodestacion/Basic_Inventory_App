package com.data.miniapp2

import androidx.recyclerview.widget.RecyclerView
import com.data.miniapp2.databinding.ItemListLayoutBinding

class InventoryItemViewHolder(val binding:ItemListLayoutBinding):RecyclerView.ViewHolder(binding.root){

    fun itemBinding(details:Inventory){
        binding.txtItemName.text = details.name
        binding.txtItemDescription.text = details.description
        binding.txtItemQuantity.text = details.quantity.toString()
    }
}