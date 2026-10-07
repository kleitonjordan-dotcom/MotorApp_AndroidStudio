package com.example.motorapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MENU PRINCIPAL DO MECÂNICO (dono da oficina).
 * Aberto pela LoginActivity quando o usuário tem tipo = 'mecanico'.
 * Mostra contadores por status; cada card abre a lista já filtrada.
 */
class MecanicoActivity : AppCompatActivity() {

    private var mecanicoId: Int = 0

    private lateinit var tvQtdHoje: TextView
    private lateinit var tvQtdPendentes: TextView
    private lateinit var tvQtdConfirmados: TextView
    private lateinit var tvQtdManutencao: TextView
    private lateinit var tvQtdConcluidos: TextView
    private lateinit var tvCancelados: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mecanico)

        // Mesmo "extra" que a LoginActivity já manda para o cliente
        mecanicoId = intent.getIntExtra("CLIENTE_ID", 0)
        val nome = intent.getStringExtra("NOME_USUARIO") ?: "Mecânico"
        findViewById<TextView>(R.id.tvBoasVindasMecanico).text = "Olá, $nome"

        val hoje = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
        findViewById<TextView>(R.id.tvDataHoje).text = hoje

        tvQtdHoje = findViewById(R.id.tvQtdHoje)
        tvQtdPendentes = findViewById(R.id.tvQtdPendentes)
        tvQtdConfirmados = findViewById(R.id.tvQtdConfirmados)
        tvQtdManutencao = findViewById(R.id.tvQtdManutencao)
        tvQtdConcluidos = findViewById(R.id.tvQtdConcluidos)
        tvCancelados = findViewById(R.id.tvCancelados)

        // Cada card abre a lista de agendamentos já filtrada
        configurarCard(R.id.cardHoje, StatusAgendamento.TODOS, apenasHoje = true)
        configurarCard(R.id.cardPendentes, StatusAgendamento.PENDENTE)
        configurarCard(R.id.cardConfirmados, StatusAgendamento.CONFIRMADO)
        configurarCard(R.id.cardManutencao, StatusAgendamento.EM_MANUTENCAO)
        configurarCard(R.id.cardConcluidos, StatusAgendamento.CONCLUIDO)
        configurarCard(R.id.cardTodos, StatusAgendamento.TODOS)
        configurarCard(R.id.cardCancelados, StatusAgendamento.CANCELADO)

        // Sair: volta para o login e limpa a pilha de telas
        findViewById<Button>(R.id.btnSairMecanico).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarResumo() // atualiza os números sempre que volta da lista
    }

    private fun configurarCard(cardId: Int, filtro: String, apenasHoje: Boolean = false) {
        findViewById<CardView>(cardId).setOnClickListener {
            val intent = Intent(this, AgendamentosMecanicoActivity::class.java)
            intent.putExtra("MECANICO_ID", mecanicoId)
            intent.putExtra("FILTRO", filtro)
            intent.putExtra("APENAS_HOJE", apenasHoje)
            startActivity(intent)
        }
    }

    private fun carregarResumo() {
        val url = ApiConfig.BASE_URL + "api_mecanico.php?acao=resumo&mecanico_id=$mecanicoId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val r = json.getJSONObject("resumo")
                        tvQtdHoje.text = r.optInt("hoje").toString()
                        tvQtdPendentes.text = r.optInt(StatusAgendamento.PENDENTE).toString()
                        tvQtdConfirmados.text = r.optInt(StatusAgendamento.CONFIRMADO).toString()
                        tvQtdManutencao.text = r.optInt(StatusAgendamento.EM_MANUTENCAO).toString()
                        tvQtdConcluidos.text = r.optInt(StatusAgendamento.CONCLUIDO).toString()
                        tvCancelados.text = "❌ Cancelados / recusados (${r.optInt(StatusAgendamento.CANCELADO)})"
                    } else {
                        Toast.makeText(this, json.optString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar resumo", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }
}
