package com.example.motorapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.widget.Button

class VeiculosActivity : AppCompatActivity() {

    private lateinit var containerVeiculos: LinearLayout
    private var clienteId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_veiculos)

        // Recupera o ID do cliente enviado pela MainActivity
        clienteId = intent.getIntExtra("CLIENTE_ID", 1)

        // Inicializa apenas o container onde a lista de carros será montada
        containerVeiculos = findViewById(R.id.containerVeiculos)
    }

    override fun onResume() {
        super.onResume()
        carregarVeiculos() // Carrega os carros assim que a tela abre
    }

    private fun carregarVeiculos() {
        val url = ApiConfig.BASE_URL + "api_veiculos.php?acao=listar&cliente_id=$clienteId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val veiculosArray = json.getJSONArray("veiculos")
                        containerVeiculos.removeAllViews()

                        if (veiculosArray.length() == 0) {
                            val tvVazio = TextView(this).apply {
                                text = "Nenhum carro cadastrado ainda."
                                setTextColor(Color.parseColor("#777777"))
                                textSize = 14f
                                setPadding(4, 8, 4, 8)
                            }
                            containerVeiculos.addView(tvVazio)
                        } else {
                            for (i in 0 until veiculosArray.length()) {
                                val item = veiculosArray.getJSONObject(i)
                                adicionarCardVeiculo(
                                    item.getInt("id"), // Passando o ID do veículo
                                    item.getString("marca"),
                                    item.getString("modelo"),
                                    item.getString("placa"),
                                    item.optString("ano", "-")
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar lista", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão ao buscar veículos", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun adicionarCardVeiculo(veiculoId: Int, marca: String, modelo: String, placa: String, ano: String) {
        val card = CardView(this).apply {
            setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            radius = 12f
            useCompatPadding = true
            cardElevation = 2f
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val tvTitulo = TextView(this).apply {
            text = "🚗 $marca $modelo"
            setTextColor(Color.parseColor("#111111"))
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
        }

        val tvDetalhes = TextView(this).apply {
            text = "Placa: $placa  |  Ano: $ano"
            setTextColor(Color.parseColor("#666666"))
            textSize = 13f
            setPadding(0, 4, 0, 12)
        }

        // Botão de Excluir estilizado com contexto explícito
        val btnExcluir = Button(this).apply {
            text = "Excluir Veículo"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#FF5252"))
            setTextColor(Color.parseColor("#FFFFFF"))
            setOnClickListener {
                android.app.AlertDialog.Builder(this@VeiculosActivity)
                    .setTitle("Excluir Veículo")
                    .setMessage("Deseja realmente remover este $marca $modelo?")
                    .setPositiveButton("Sim") { _, _ ->
                        deletarVeiculo(veiculoId)
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }
        }

        layout.addView(tvTitulo)
        layout.addView(tvDetalhes)
        layout.addView(btnExcluir)
        card.addView(layout)
        containerVeiculos.addView(card)
    }

    private fun deletarVeiculo(veiculoId: Int) {
        val url = ApiConfig.BASE_URL + "api_veiculos.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                    if (json.getBoolean("success")) {
                        carregarVeiculos() // Recarrega a lista após excluir
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar resposta", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["acao"] = "excluir"
                params["veiculo_id"] = veiculoId.toString()
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}