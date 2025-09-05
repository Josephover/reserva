package com.example.dbejercicio

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var editTextProducto: EditText
    private lateinit var editTextCantidad: EditText
    private lateinit var editTextPrecio: EditText

    private lateinit var listViewProductos: ListView
    private lateinit var adapterProductos: ArrayAdapter<String>
    private var productoIdSeleccionado: Long? = null
    private val listaIds = ArrayList<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa Firebase
        FirebaseApp.initializeApp(this)

        databaseHelper = DatabaseHelper(this)
        editTextProducto = findViewById(R.id.editTextProducto)
        editTextCantidad = findViewById(R.id.editTextCantidad)
        editTextPrecio = findViewById(R.id.editTextPrecio)
        listViewProductos = findViewById(R.id.ListViewTarea)

        findViewById<Button>(R.id.btnAgregarProducto).setOnClickListener { agregarProducto() }
        findViewById<Button>(R.id.btnActualizarProducto).setOnClickListener { actualizarProducto() }
        findViewById<Button>(R.id.btnEliminar).setOnClickListener { eliminarProducto() }

        listViewProductos.setOnItemClickListener { _, _, position, _ ->
            seleccionarProducto(position)
        }

        listarProductos()
    }

    // Guardar en Firestore
    private fun guardarProductoEnFirestore(nombre: String, cantidad: Int, precio: Double) {
        val db = FirebaseFirestore.getInstance()
        val producto = hashMapOf(
            "nombre" to nombre,
            "cantidad" to cantidad,
            "precio" to precio
        )
        db.collection("productos")
            .add(producto)
    }

    // Actualizar en Firestore
    private fun actualizarProductoEnFirestore(nombre: String, cantidad: Int, precio: Double) {
        val db = FirebaseFirestore.getInstance()
        db.collection("productos")
            .whereEqualTo("nombre", nombre)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("productos").document(document.id)
                        .update("cantidad", cantidad, "precio", precio)
                }
            }
    }

    // Eliminar en Firestore
    private fun eliminarProductoEnFirestore(nombre: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("productos")
            .whereEqualTo("nombre", nombre)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("productos").document(document.id).delete()
                }
            }
    }

    private fun agregarProducto() {
        val nombre = editTextProducto.text.toString().trim()
        val cantidad = editTextCantidad.text.toString().trim().toIntOrNull() ?: 0
        val precio = editTextPrecio.text.toString().trim().toDoubleOrNull() ?: 0.0
        if (nombre.isEmpty() || cantidad <= 0 || precio <= 0.0) {
            Toast.makeText(this, "Ingrese nombre, cantidad y precio válidos", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            val id = databaseHelper.agregarProducto(nombre, cantidad, precio)
            runOnUiThread {
                if (id > -1) {
                    guardarProductoEnFirestore(nombre, cantidad, precio)
                    editTextProducto.text.clear()
                    editTextCantidad.text.clear()
                    editTextPrecio.text.clear()
                    productoIdSeleccionado = null
                    listarProductos()
                    Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al agregar producto", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun actualizarProducto() {
        val id = productoIdSeleccionado
        val nombre = editTextProducto.text.toString().trim()
        val cantidad = editTextCantidad.text.toString().trim().toIntOrNull() ?: 0
        val precio = editTextPrecio.text.toString().trim().toDoubleOrNull() ?: 0.0
        if (id == null) {
            Toast.makeText(this, "Seleccione un producto", Toast.LENGTH_SHORT).show()
            return
        }
        if (nombre.isEmpty() || cantidad <= 0 || precio <= 0.0) {
            Toast.makeText(this, "Ingrese nombre, cantidad y precio válidos", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            val filas = databaseHelper.actualizarProducto(id, nombre, cantidad, precio)
            runOnUiThread {
                if (filas > 0) {
                    actualizarProductoEnFirestore(nombre, cantidad, precio)
                    editTextProducto.text.clear()
                    editTextCantidad.text.clear()
                    editTextPrecio.text.clear()
                    productoIdSeleccionado = null
                    listarProductos()
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al actualizar producto", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun eliminarProducto() {
        val id = productoIdSeleccionado
        if (id == null) {
            Toast.makeText(this, "Seleccione un producto", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            // Obtener nombre antes de eliminar
            var nombre: String? = null
            val db = databaseHelper.readableDatabase
            val cursor = db.rawQuery("SELECT nombre FROM productos WHERE id = ?", arrayOf(id.toString()))
            if (cursor.moveToFirst()) {
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            }
            cursor.close()
            db.close()

            val filas = databaseHelper.eliminarProducto(id)
            runOnUiThread {
                if (filas > 0 && nombre != null) {
                    eliminarProductoEnFirestore(nombre)
                    editTextProducto.text.clear()
                    editTextCantidad.text.clear()
                    editTextPrecio.text.clear()
                    productoIdSeleccionado = null
                    listarProductos()
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al eliminar producto", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
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
                adapterProductos = ArrayAdapter(this, android.R.layout.simple_list_item_1, productos)
                listViewProductos.adapter = adapterProductos
            }
        }.start()
    }

    private fun seleccionarProducto(position: Int) {
        val id = listaIds.getOrNull(position) ?: return
        Thread {
            val db = databaseHelper.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM productos WHERE id = ?", arrayOf(id.toString()))
            if (cursor.moveToFirst()) {
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
                productoIdSeleccionado = id
                runOnUiThread {
                    editTextProducto.setText(nombre)
                    editTextCantidad.setText(cantidad.toString())
                    editTextPrecio.setText(precio.toString())
                }
            }
            cursor.close()
            db.close()
        }.start()
    }
}