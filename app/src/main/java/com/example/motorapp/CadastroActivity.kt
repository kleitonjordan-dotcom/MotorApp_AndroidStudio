package com.example.motorapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class CadastroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro2) // Certifique-se que está carregando o layout correto

        // Associa os componentes do novo layout moderno
        val etNome = findViewById<EditText>(R.id.etNome)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etTelefone = findViewById<EditText>(R.id.etTelefone)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)
        val btnVoltarLogin = findViewById<Button>(R.id.btnVoltarLogin)

        btnVoltarLogin.setOnClickListener {
            finish()
        }

        btnCadastrar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val telefone = etTelefone.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (nome.isNotEmpty() && email.isNotEmpty() && telefone.isNotEmpty() && senha.isNotEmpty()) {
                realizarCadastro(nome, email, telefone, senha)
            } else {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun realizarCadastro(nome: String, email: String, telefone: String, senha: String) {
        //val url = ApiConfig.BASE_URL + "api_login.php" // ou seu endpoint de cadastro de usuário
        val url = ApiConfig.BASE_URL + "api_cadastro.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                    if (json.getBoolean("success")) {
                        finish() // Volta para a tela de login após cadastrar com sucesso
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
                params["acao"] = "cadastrar"
                params["nome"] = nome
                params["email"] = email
                params["telefone"] = telefone
                params["senha"] = senha
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}