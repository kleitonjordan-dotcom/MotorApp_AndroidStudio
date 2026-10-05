package com.example.motorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private var clienteId: Int = 1
    private lateinit var tvBoasVindas: TextView
    private lateinit var tvStatusAgendamento: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Recupera o ID e o Nome do Login
        clienteId = intent.getIntExtra("CLIENTE_ID", 1)
        val nomeCliente = intent.getStringExtra("NOME_USUARIO") ?: "Cliente"

        tvBoasVindas = findViewById(R.id.tvBoasVindas)
        tvBoasVindas.text = "Olá, $nomeCliente"

        // Referência correta para o texto do status dentro do card de agendamentos
        tvStatusAgendamento = findViewById(R.id.tvStatusAgendamento)

        // Configurar clique no card da Dashboard: "MEUS VEÍCULOS" (Ver a lista)
        val cardMeusVeiculos = findViewById<CardView>(R.id.cardMeusVeiculos)
        cardMeusVeiculos.setOnClickListener {
            val intent = Intent(this, VeiculosActivity::class.java)
            intent.putExtra("CLIENTE_ID", clienteId)
            startActivity(intent)
        }

        // Configurar clique no card: "CADASTRAR VEÍCULO"
        val cardCadastrarVeiculo = findViewById<CardView>(R.id.cardCadastrarVeiculo)
        cardCadastrarVeiculo.setOnClickListener {
            val intent = Intent(this, CadastrarVeiculoActivity::class.java)
            intent.putExtra("CLIENTE_ID", clienteId)
            startActivity(intent)
        }

        // Configurar clique no card: "AGENDAMENTOS" (Abre a lista de agendamentos)
        val cardProximoAgendamento = findViewById<CardView>(R.id.cardProximoAgendamento)
        cardProximoAgendamento.setOnClickListener {
            val intent = Intent(this, MeusAgendamentosActivity::class.java)
            intent.putExtra("CLIENTE_ID", clienteId)
            startActivity(intent)
        }

        // Configurar clique no card: "NOVO AGENDAMENTO"
        val cardNovoAgendamento = findViewById<CardView>(R.id.btnMenuAgendamentos)
        cardNovoAgendamento.setOnClickListener {
            val intent = Intent(this, AgendamentoActivity::class.java)
            intent.putExtra("CLIENTE_ID", clienteId)
            startActivity(intent)
        }

        // Configurar clique no card: "MEUS DADOS"
        // Configurar clique no card: "MEUS DADOS"
        val btnMenuDados = findViewById<CardView>(R.id.btnMenuDados)
        btnMenuDados.setOnClickListener {
            val intent = Intent(this, MeusDadosActivity::class.java)
            intent.putExtra("CLIENTE_ID", clienteId)
            startActivity(intent)
        }

        // Botão Sair no Topo
        val btnSair = findViewById<Button>(R.id.btnSair)
        btnSair.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza o nome e o agendamento sempre que a tela principal ganha foco novamente
        carregarNomeCliente()
        verificarAgendamentoAtivo()
    }

    private fun carregarNomeCliente() {
        val url = ApiConfig.BASE_URL + "api_clientes.php?acao=buscar&id=$clienteId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val cliente = json.getJSONObject("cliente")
                        val nome = cliente.getString("nome")
                        tvBoasVindas.text = "Olá, $nome"
                    }
                } catch (e: Exception) {
                    // Mantém o valor atual em caso de erro
                }
            },
            {
                // Erro de conexão ignorado para não atrapalhar
            }
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun verificarAgendamentoAtivo() {
        val url = ApiConfig.BASE_URL + "api_agendamentos.php?acao=listar&cliente_id=$clienteId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val array = json.getJSONArray("agendamentos")
                        if (array.length() > 0) {
                            // Pega o agendamento mais recente
                            val ultimo = array.getJSONObject(0)
                            val pacote = ultimo.getString("pacote")
                            val data = ultimo.getString("data")
                            val hora = ultimo.getString("hora")

                            // Converte visualmente de YYYY-MM-DD para DD/MM/YYYY (Formato Brasileiro)
                            val dataExibicao = if (data.contains("-") && data.length == 10) {
                                val partes = data.split("-")
                                if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else data
                            } else {
                                data
                            }

                            // Mostra resumido no card do menu principal com a data formatada
                            tvStatusAgendamento.text = "$pacote\n$dataExibicao às $hora"
                        } else {
                            tvStatusAgendamento.text = "Nenhum agendamento ativo"
                        }
                    }
                } catch (e: Exception) {
                    // Mantém o estado atual em caso de erro silencioso
                }
            },
            {
                // Erro de conexão ignorado para não atrapalhar o menu
            }
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }
}