package com.example.dbejercicio

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.Spinner
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.ArrayAdapter
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

class PagarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagar)

        val total = intent.getDoubleExtra("total", 0.0)
        val textTotal = findViewById<TextView>(R.id.textTotal)
        textTotal.text = "Total: $total"

        // Recibe y muestra los productos en el ListView
        val productos = intent.getStringArrayListExtra("productos") ?: arrayListOf()
        val listView = findViewById<ListView>(R.id.listViewProductosPagar)
        val productosAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, productos)
        listView.adapter = productosAdapter

        // Spinner con layout personalizado
        val spinnerMetodoPago = findViewById<Spinner>(R.id.spinnerMetodoPago)
        val metodos = arrayOf(getString(R.string.efectivo), getString(R.string.tarjeta))
        val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item_metodo_pago, metodos)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_metodo_pago)
        spinnerMetodoPago.adapter = spinnerAdapter

        val layoutTarjeta = findViewById<LinearLayout>(R.id.layoutTarjeta)
        val editNumeroTarjeta = findViewById<EditText>(R.id.editNumeroTarjeta)
        val editNombreTarjeta = findViewById<EditText>(R.id.editNombreTarjeta)

        spinnerMetodoPago.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                layoutTarjeta.visibility = if (metodos[position] == getString(R.string.tarjeta)) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        findViewById<Button>(R.id.btnPagar).setOnClickListener {
            if (spinnerMetodoPago.selectedItem == getString(R.string.tarjeta)) {
                val numero = editNumeroTarjeta.text.toString()
                val nombre = editNombreTarjeta.text.toString()
                if (numero.isBlank() || nombre.isBlank()) {
                    Toast.makeText(this, getString(R.string.completa_datos_tarjeta), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            Toast.makeText(this, "Pagado con éxito", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}