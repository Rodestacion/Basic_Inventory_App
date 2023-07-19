package com.data.miniapp2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.lang.Exception

class DatabaseHelper (context:Context):SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION){
    companion object{
        val DATABASE_NAME = "inventory.db"
        val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE login(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT,
                    password TEXT
                )
            """.trimIndent())

        db.execSQL(
            """
                CREATE TABLE inventories(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product TEXT,
                    description TEXT,
                    quantity INT,
                    user TEXT
                )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS inventory")
        onCreate(db)
    }

    fun insertUser(user:User){
        val db = writableDatabase
        val sql = "INSERT INTO login (username,password) VALUES (?,?)"
        val args = arrayOf(user.username,user.password)
        db.execSQL(sql,args)
    }

    fun getUserList():MutableList<User>{
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM login",null)
        val userList = mutableListOf<User>()

        while (cursor.moveToNext()){
            val id = cursor.getInt(0)
            val username = cursor.getString(1)
            val password = cursor.getString(2)

            val newUser = User(id,username,password)
            userList.add(newUser)
        }
        cursor.close()
        return userList
    }

    fun getAllList(currentUser:String):MutableList<Inventory>{
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM inventories",null)
        val inventory = mutableListOf<Inventory>()

        while (cursor.moveToNext()){
            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            val description = cursor.getString(2)
            val quantity = cursor.getInt(3)
            val user = cursor.getString(4)

            if (currentUser == user){
                val newInventory = Inventory(id,name,description,quantity)
                inventory.add(newInventory)
            }
        }
        cursor.close()
        return inventory
    }

    fun lastItem(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM inventories",null)
        var extractID: Int = 0

        try {
            cursor.moveToLast()
            extractID = cursor.getInt(0)
            cursor.close()
        }catch (exception:Exception){
            extractID=0
        }finally {
            cursor.close()
        }

        return extractID
    }

    fun insertItem(product:Inventory,user:String){
        val db = writableDatabase
        val sql = "INSERT INTO inventories (product,description,quantity,user) VALUES (?,?,?,?)"
        val args = arrayOf(product.name,product.description,product.quantity,user)
        db.execSQL(sql,args)
    }

    fun deleteItem(id:Int){
        val db = writableDatabase
        val deleteQuery = "DELETE FROM inventories WHERE id = $id"
        db.execSQL(deleteQuery)
    }

    fun updateItem(inventory: Inventory){
        val db = writableDatabase
        val updateQuery = "UPDATE inventories SET product='${inventory.name}',description='${inventory.description}',quantity='${inventory.quantity}' WHERE id = '${inventory.id}'"
        db.execSQL(updateQuery)
    }



}