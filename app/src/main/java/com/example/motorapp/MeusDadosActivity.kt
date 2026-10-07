package com.example.motorapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MeusDadosActivity : AppCompatActivity() {

    private var clienteId: Int = 1
    private lateinit var editNome: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var editCpf: TextInputEditText
    private lateinit var editTelefone: TextInputEditText
    private lateinit var btnSalvar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meus_dados)

        clienteId = intent.getIntExtra("CLIENTE_ID", 1)

        editNome = findViewById(R.id.editNomeDados)
        editEmail = findViewById(R.id.editEmailDados)
        editCpf = findViewById(R.id.editCpfDados)
        editTelefone = findViewById(R.id.editTelefoneDados)
        btnSalvar = findViewById(R.id.btnSalvarDados)

        // Carrega os dados atuais do cliente da base de dados
        carregarDadosCliente()

        btnSalvar.setOnClickListener {
            atualizarDadosCliente()
        }
    }

    private fun carregarDadosCliente() {
        val url = ApiConfig.BASE_URL + "api_clientes.php?acao=buscar&id=$clienteId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val cliente = json.getJSONObject("cliente")
                        editNome.setText(cliente.getString("nome"))
                        editEmail.setText(cliente.getString("email"))
                        editCpf.setText(cliente.optString("cpf", ""))
                        editTelefone.setText(cliente.optString("telefone", ""))
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun atualizarDadosCliente() {
        val nome = editNome.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val cpf = editCpf.text.toString().trim()
        val telefone = editTelefone.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        val url = ApiConfig.BASE_URL + "api_clientes.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()
                    if (json.getBoolean("success")) {
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao atualizar dados", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["acao"] = "atualizar"
                params["id"] = clienteId.toString()
                params["nome"] = nome
                params["email"] = email
                params["cpf"] = cpf
                params["telefone"] = telefone
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }
}