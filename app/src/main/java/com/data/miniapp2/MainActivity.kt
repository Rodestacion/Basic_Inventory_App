package com.data.miniapp2

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.data.miniapp2.databinding.ActivityMainBinding
import com.data.miniapp2.databinding.AddItemLayoutBinding
import com.data.miniapp2.databinding.ViewItemLayoutBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var databaseHelper:DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var inventory:MutableList<Inventory>
    private lateinit var adapter: InventoryAdapter
    private lateinit var currentUser:String
    private var tempName:String = ""
    private var tempDescription:String = ""
    private var tempQuantity:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        currentUser = intent.getStringExtra("user").toString()
        //currentUser = "ADMIN"

        recyclerView = binding.inventoryRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        inventory = getList()
        adapter = InventoryAdapter(inventory)
        recyclerView.adapter=adapter

        adapter.onUpdateClick={inventory ->
            showUpdateDialog(inventory)
        }

        adapter.onDeleteClick={inventory ->
            showDeleteDialog(inventory)
        }

        adapter.onItemClick ={inventory ->
            showViewDialog(inventory)
        }

        binding.floatAddItem.setOnClickListener {
            showAddItemDialog()
        }

        binding.materialAppBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.menuAbout->{
                    showAboutDialog()
                }
                R.id.menuLogout->{
                    val nextScreen = Intent(this,LoginScreen::class.java)
                    Toast.makeText(applicationContext, "Logout successfully", Toast.LENGTH_SHORT).show()
                    startActivity(nextScreen)
                    finish()
                }
            }
            true
        }
    }

    private fun showAboutDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this,R.style.MyDialogTheme)
        alertDialogBuilder.setTitle("About")
        alertDialogBuilder.setIcon(getImageID("inventory_icon"))
        alertDialogBuilder.setMessage("Developer: \n\n\t\tRodney Estacion\n\nPurpose: \n\n\t\tThis app is intended for user in order to managing basic information in tracking inventory item")

        alertDialogBuilder.setPositiveButton("OK"){ dialog:DialogInterface,_->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private fun getList(): MutableList<Inventory> {
        return databaseHelper.getAllList(currentUser)
    }

    private fun showViewDialog(inventory: Inventory) {
        val alertDialogBuilder = AlertDialog.Builder(this,R.style.MyDialogTheme)
        alertDialogBuilder.setTitle("Details")
        alertDialogBuilder.setIcon(getImageID("inventory_icon"))

        val dialogLayout = layoutInflater.inflate(R.layout.view_item_layout,null)
        val dialogBinding = ViewItemLayoutBinding.bind(dialogLayout)
        alertDialogBuilder.setView(dialogLayout)

        dialogBinding.txtName.text= "Name:\n\t${inventory.name}"
        dialogBinding.txtDescription.text = "Description:\n\t${inventory.description}"
        dialogBinding.txtQuantity.text = "Quantity:\n\t${inventory.quantity}"

        alertDialogBuilder.setPositiveButton("Ok") { dialog: DialogInterface,_ ->
            dialog.dismiss()
        }

        val alertDialog:AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showDeleteDialog(inventory: Inventory) {
        val alertDialogBuilder = AlertDialog.Builder(this,R.style.MyDialogTheme)
        alertDialogBuilder.setTitle("Warning for Deletion")
        alertDialogBuilder.setIcon(getImageID("baseline_delete_forever_24"))
        alertDialogBuilder.setMessage("Your going to delete the item ${inventory.name} with existing stocks of ${inventory.quantity}\n\nAre you sure you want to Continue? ")

        alertDialogBuilder.setPositiveButton("Yes") { dialog: DialogInterface,_->
            databaseHelper.deleteItem(inventory.id)
            this.inventory.remove(inventory)
            adapter.notifyDataSetChanged()
            Toast.makeText(applicationContext, "Item has successfully deleted", Toast.LENGTH_SHORT).show()
        }

        alertDialogBuilder.setNegativeButton("No") { dialog: DialogInterface,_->
            Toast.makeText(applicationContext, "Deleting of item has been cancelled", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val alertDialog:AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showUpdateDialog(inventory: Inventory){
        val alertDialogBuilder = AlertDialog.Builder(this,R.style.MyDialogTheme)
        alertDialogBuilder.setTitle("Update Inventory Item")
        alertDialogBuilder.setIcon(getImageID("inventory_icon"))

        val dialogLayout = layoutInflater.inflate(R.layout.add_item_layout,null)
        val dialogBinding = AddItemLayoutBinding.bind(dialogLayout)

        if(tempName.isNotEmpty()||tempDescription.isNotEmpty()||tempQuantity.isNotEmpty()){
            dialogBinding.etProductName.setText(tempName)
            dialogBinding.etDescription.setText(tempDescription)
            dialogBinding.etQuantity.setText(tempQuantity)
        }else{
            dialogBinding.etProductName.setText(inventory.name)
            dialogBinding.etDescription.setText(inventory.description)
            dialogBinding.etQuantity.setText(inventory.quantity.toString())
        }

        alertDialogBuilder.setView(dialogLayout)

        alertDialogBuilder.setPositiveButton("Update") { dialog, _ ->
            val name = dialogBinding.etProductName.text.toString()
            val description = dialogBinding.etDescription.text.toString()
            val quantity = dialogBinding.etQuantity.text.toString()

            if(checkNoDuplicate(inventory.id,name.uppercase())){
                val newName = name.uppercase()
                val newDescription =description.lowercase()
                val newUpdate = Inventory(inventory.id,newName,newDescription.capitalize(), quantity.toInt())
                val position = this.inventory.indexOfFirst { it.id == inventory.id }
                databaseHelper.updateItem(newUpdate)

                if (position!=-1){
                    this.inventory[position] = newUpdate
                    adapter.notifyItemChanged(position)
                }

                Toast.makeText(applicationContext, "Inventory Item has been updated", Toast.LENGTH_SHORT).show()

                tempName = ""
                tempDescription = ""
                tempQuantity = ""
            }else{
                tempName = name.uppercase()
                var lowerLetter = description.lowercase()
                tempDescription = lowerLetter.capitalize()
                tempQuantity = quantity
                
                showUpdateDialog(inventory)

                Toast.makeText(applicationContext, "Item name already exist in inventory", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            tempName = ""
            tempDescription = ""
            tempQuantity = ""
            Toast.makeText(applicationContext, "Updating inventory was cancelled", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val alertDialog:AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun checkNoDuplicate(id: Int, itemName: String):Boolean{
        var check:Boolean = true
        repeat(this.inventory.size){
            if(inventory[it].id!=id){
                if(inventory[it].name==itemName){
                    check = false
                }
            }
        }
        return check
    }

    private fun checkNotExist(itemName: String):Boolean{
        var check:Boolean = true
        repeat(this.inventory.size){
            if(inventory[it].name==itemName){
                check = false
            }
        }
        return check
    }

    private fun showAddItemDialog(){
        val alertDialogBuilder = AlertDialog.Builder(this,R.style.MyDialogTheme)
        alertDialogBuilder.setTitle("Add New Item in Inventory")

        val dialogLayout = layoutInflater.inflate(R.layout.add_item_layout,null)
        val dialogBinding = AddItemLayoutBinding.bind(dialogLayout)

        if(tempName.isNotEmpty()||tempDescription.isNotEmpty()||tempQuantity.isNotEmpty()){
            dialogBinding.etProductName.setText(tempName)
            dialogBinding.etDescription.setText(tempDescription)
            dialogBinding.etQuantity.setText(tempQuantity)
        }

        alertDialogBuilder.setView(dialogLayout)
        alertDialogBuilder.setIcon(getImageID("inventory_icon"))

        alertDialogBuilder.setPositiveButton("Save"){dialog, _->
            val name = dialogBinding.etProductName.text.toString()
            val description = dialogBinding.etDescription.text.toString()
            val quantity = dialogBinding.etQuantity.text.toString()

            if(name.isNotEmpty()&&description.isNotEmpty()&&quantity.isNotEmpty()){
                val newName = name.uppercase()
                val newDescription =description.lowercase()

                if(checkNotExist(newName)){
                    var newItem = Inventory(0,newName,newDescription.capitalize(), quantity.toInt())
                    databaseHelper.insertItem(newItem,currentUser)

                    //Due to error need to add first the item in database before getting the ID
                    val getID = databaseHelper.lastItem()
                    newItem = Inventory(getID,newName,newDescription.capitalize(), quantity.toInt())
                    this.inventory.add(newItem)
                    recyclerView.adapter?.notifyDataSetChanged()
                    Toast.makeText(applicationContext, "New item was successfully added to the inventory", Toast.LENGTH_SHORT).show()
                    tempName = ""
                    tempDescription = ""
                    tempQuantity = ""
                }else{
                    tempName = name.uppercase()
                    val lowerLetter = description.lowercase()
                    tempDescription = lowerLetter.capitalize()
                    tempQuantity = quantity
                    showAddItemDialog()
                    Toast.makeText(applicationContext, "Item already exist", Toast.LENGTH_SHORT).show()
                }


            }else{
                tempName = name.uppercase()
                val lowerLetter = description.lowercase()
                tempDescription = lowerLetter.capitalize()
                tempQuantity = quantity
                showAddItemDialog()
                Toast.makeText(applicationContext, "Incomplete Input", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialogBuilder.setNegativeButton("Cancel"){dialog, _->
            tempName = ""
            tempDescription = ""
            tempQuantity = ""

            Toast.makeText(applicationContext, "No item was added", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val alertDialog:AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getImageID(imageName: String): Int {
        //val myPackage = android.content.ContextWrapper(context)
        return resources.getIdentifier(imageName,"drawable",packageName)
    }
}