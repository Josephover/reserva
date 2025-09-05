package com.example.dbejercicio

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "dbEjercicio.db"
        private const val DATABASE_VERSION = 3

        private const val TABLE_PRODUCTOS = "productos"
        private const val COLUMN_PRODUCTO_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_CANTIDAD = "cantidad"
        private const val COLUMN_PRECIO = "precio"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableProductos = ("CREATE TABLE $TABLE_PRODUCTOS ("
                + "$COLUMN_PRODUCTO_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NOMBRE TEXT, "
                + "$COLUMN_CANTIDAD INTEGER, "
                + "$COLUMN_PRECIO REAL);")
        db.execSQL(createTableProductos)

        val createTableHistorial = ("CREATE TABLE historial ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "producto_id INTEGER, "
                + "nombre TEXT, "
                + "precio REAL, "
                + "fecha TEXT);")
        db.execSQL(createTableHistorial)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTOS")
        db.execSQL("DROP TABLE IF EXISTS historial")
        onCreate(db)
    }

    fun agregarProducto(nombre: String, cantidad: Int, precio: Double): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NOMBRE, nombre)
        values.put(COLUMN_CANTIDAD, cantidad)
        values.put(COLUMN_PRECIO, precio)
        val id = db.insert(TABLE_PRODUCTOS, null, values)
        db.close()
        return id
    }

    fun actualizarProducto(id: Long, nombre: String, cantidad: Int, precio: Double): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NOMBRE, nombre)
        values.put(COLUMN_CANTIDAD, cantidad)
        values.put(COLUMN_PRECIO, precio)
        val filas = db.update(TABLE_PRODUCTOS, values, "$COLUMN_PRODUCTO_ID=?", arrayOf(id.toString()))
        db.close()
        return filas
    }

    fun eliminarProducto(id: Long): Int {
        val db = this.writableDatabase
        val filas = db.delete(TABLE_PRODUCTOS, "$COLUMN_PRODUCTO_ID=?", arrayOf(id.toString()))
        db.close()
        return filas
    }

    fun listarProductos(): ArrayList<String> {
        val lista = ArrayList<String>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_PRODUCTOS"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE))
                val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRECIO))
                lista.add("Producto: $nombre\nCantidad: $cantidad\nPrecio: $precio")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun registrarReserva(productoId: Long, nombre: String, precio: Double) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("producto_id", productoId)
        values.put("nombre", nombre)
        values.put("precio", precio)
        values.put("fecha", System.currentTimeMillis().toString())
        db.insert("historial", null, values)
        db.close()
    }
}