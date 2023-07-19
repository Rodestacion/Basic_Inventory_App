package com.data.miniapp2

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.data.miniapp2.databinding.ItemListLayoutBinding

class InventoryAdapter (private val details:List<Inventory>):RecyclerView.Adapter<InventoryItemViewHolder>(){
    var onUpdateClick:((Inventory)->Unit)?=null
    var onDeleteClick:((Inventory)->Unit)?=null
    var onItemClick:((Inventory)->Unit)?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemListLayoutBinding.inflate(inflater,parent,false)
        return InventoryItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return details.size
    }

    override fun onBindViewHolder(holder: InventoryItemViewHolder, position: Int) {
        holder.itemBinding(details[position])

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(details[position])
        }

        holder.binding.apply {
            btnUpdateItem.setOnClickListener {
                onUpdateClick?.invoke(details[position])
            }
            btnDeleteItem.setOnClickListener {
                onDeleteClick?.invoke(details[position])
            }
        }

    }
}