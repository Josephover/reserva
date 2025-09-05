package com.example.dbejercicio

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent

class HistorialActivity : AppCompatActivity() {

    private val listaIds = ArrayList<Long>()
    private var reservaSeleccionadaId: Long? = null
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        listView = findViewById(R.id.listViewHistorial)
        val btnEliminarReserva = findViewById<Button>(R.id.btnEliminarReserva)
        val btnIrAPagar = findViewById<Button>(R.id.btnIrAPagar)

        listarHistorial()

        listView.setOnItemClickListener { _, _, position, _ ->
            reservaSeleccionadaId = listaIds.getOrNull(position)
        }

        btnEliminarReserva.setOnClickListener {
            val id = reservaSeleccionadaId
            if (id == null) {
                Toast.makeText(this, "Seleccione una reserva", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Thread {
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.writableDatabase
                val filas = db.delete("historial", "id=?", arrayOf(id.toString()))
                db.close()
                runOnUiThread {
                    if (filas > 0) {
                        Toast.makeText(this, "Reserva eliminada", Toast.LENGTH_SHORT).show()
                        reservaSeleccionadaId = null
                        listarHistorial()
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        btnIrAPagar.setOnClickListener {
            val total = obtenerTotalReservas()
            val productos = obtenerProductosReservados()
            val intent = Intent(this, PagarActivity::class.java)
            intent.putExtra("total", total)
            intent.putStringArrayListExtra("productos", productos)
            startActivity(intent)
        }
    }

    private fun listarHistorial() {
        Thread {
            val dbHelper = DatabaseHelper(this)
            val lista = ArrayList<String>()
            listaIds.clear()
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT id, nombre, precio, fecha FROM historial ORDER BY fecha DESC", null)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                    val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
                    val fechaRaw = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                    val fechaLegible = try {
                        val millis = fechaRaw.toLong()
                        sdf.format(Date(millis))
                    } catch (e: Exception) {
                        fechaRaw
                    }
                    lista.add("Reservado: $nombre\nPrecio: $precio\nFecha: $fechaLegible")
                    listaIds.add(id)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            runOnUiThread {
                adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lista)
                listView.adapter = adapter
            }
        }.start()
    }

    private fun obtenerTotalReservas(): Double {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(precio) as total FROM historial", null)
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
        }
        cursor.close()
        db.close()
        return total
    }
    private fun obtenerProductosReservados(): ArrayList<String> {
        val productos = ArrayList<String>()
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT nombre, precio FROM historial ORDER BY fecha DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
                productos.add("Producto: $nombre\nPrecio: $precio")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return productos
    }
}