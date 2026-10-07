package com.example.motorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnIrCadastro = findViewById<Button>(R.id.btnIrCadastro)

        // Ação para abrir a Tela de Cadastro
        btnIrCadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        // Ação do Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                realizarLogin(email, senha)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun realizarLogin(email: String, senha: String) {
        val url = ApiConfig.BASE_URL + "api_login.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val nome = json.optString("nome", "Usuário")

                        // Alinhado com o PHP que envia "id"
                        val clienteId = json.optInt("id", 1)

                        Toast.makeText(this, "Bem-vindo, $nome!", Toast.LENGTH_SHORT).show()

                        // Mecânico (dono da oficina) vai para o painel da oficina;
                        // cliente continua indo para o menu normal (MainActivity)
                        val tipo = json.optString("tipo", "cliente")
                        val destino = if (tipo == "mecanico") MecanicoActivity::class.java else MainActivity::class.java

                        val intent = Intent(this@LoginActivity, destino)
                        intent.putExtra("NOME_USUARIO", nome)
                        intent.putExtra("CLIENTE_ID", clienteId)
                        startActivity(intent)
                        finish()
                    }else {
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro no formato da resposta", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["senha"] = senha
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}