package com.example.dbejercicio

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class UsuarioActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val listaIds = ArrayList<Long>()
    private var productoSeleccionadoId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuario)
        findViewById<ImageButton>(R.id.btnHistorial).setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
        databaseHelper = DatabaseHelper(this)
        listView = findViewById(R.id.listViewProductosUsuario)
        val btnReservar = findViewById<Button>(R.id.btnReservar)

        listarProductos()

        listView.setOnItemClickListener { _, _, position, _ ->
            productoSeleccionadoId = listaIds.getOrNull(position)
        }

        btnReservar.setOnClickListener {
            val id = productoSeleccionadoId
            if (id == null) {
                Toast.makeText(this, "Seleccione un producto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Thread {
                val db = databaseHelper.writableDatabase
                val cursor = db.rawQuery("SELECT nombre, cantidad, precio FROM productos WHERE id = ?", arrayOf(id.toString()))
                if (cursor.moveToFirst()) {
                    val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"))
                    val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                    val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
                    if (cantidad > 0) {
                        val nuevoValor = cantidad - 1
                        db.execSQL("UPDATE productos SET cantidad = ? WHERE id = ?", arrayOf(nuevoValor, id))
                        // Registrar en historial
                        databaseHelper.registrarReserva(id, nombre, precio)
                        runOnUiThread {
                            Toast.makeText(this, "Producto reservado", Toast.LENGTH_SHORT).show()
                            listarProductos()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "No hay stock disponible", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                cursor.close()
                db.close()
            }.start()
        }
    }

    private fun listarProductos() {
        Thread {
            val productos = ArrayList<String>()
            listaIds.clear()
            val db = databaseHelper.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM productos", null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                    val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"))
                    val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
                    productos.add("Producto: $nombre\nCantidad: $cantidad\nPrecio: $precio")
                    listaIds.add(id)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            runOnUiThread {
                adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, productos)
                listView.adapter = adapter
            }
        }.start()
    }
}