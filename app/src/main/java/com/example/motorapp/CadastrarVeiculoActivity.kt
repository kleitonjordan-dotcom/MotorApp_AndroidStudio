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

class CadastrarVeiculoActivity : AppCompatActivity() {

    private var clienteId: Int = 1
    private lateinit var etMarca: EditText
    private lateinit var etModelo: EditText
    private lateinit var etAno: EditText
    private lateinit var etPlaca: EditText
    private lateinit var btnSalvarCarro: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_veiculo)

        // Recupera o ID do cliente enviado pela MainActivity
        clienteId = intent.getIntExtra("CLIENTE_ID", 1)

        // Referencia os componentes do XML
        etMarca = findViewById(R.id.etMarca)
        etModelo = findViewById(R.id.etModelo)
        etAno = findViewById(R.id.etAno)
        etPlaca = findViewById(R.id.etPlaca)
        btnSalvarCarro = findViewById(R.id.btnSalvarCarro)

        // Ação do botão salvar
        btnSalvarCarro.setOnClickListener {
            cadastrarVeiculo()
        }
    }

    private fun cadastrarVeiculo() {
        val marca = etMarca.text.toString().trim()
        val modelo = etModelo.text.toString().trim()
        val ano = etAno.text.toString().trim()
        val placa = etPlaca.text.toString().trim()

        // Validação simples para não enviar campos vazios
        if (marca.isEmpty() || modelo.isEmpty() || ano.isEmpty() || placa.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // URL da sua API (ajuste o parâmetro 'acao' conforme o seu PHP espera)
        val url = ApiConfig.BASE_URL + "api_veiculos.php?acao=cadastrar"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Carro cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish() // Fecha a tela e volta para a lista/dashboard
                    } else {
                        val mensagem = json.optString("message", "Erro ao cadastrar")
                        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar resposta do servidor", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["cliente_id"] = clienteId.toString()
                params["marca"] = marca
                params["modelo"] = modelo
                params["ano"] = ano
                params["placa"] = placa
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}