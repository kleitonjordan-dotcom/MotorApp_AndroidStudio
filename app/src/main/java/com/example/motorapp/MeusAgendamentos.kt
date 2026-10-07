package com.example.motorapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

data class AgendamentoModel(
    val id: Int,
    val pacote: String,
    val veiculo: String,
    val data: String,
    val hora: String,
    val status: String = "Pendente" // status definido pelo mecânico
)

class AgendamentoAdapter(
    private val lista: List<AgendamentoModel>,
    private val onCancelarClick: (Int) -> Unit
) : RecyclerView.Adapter<AgendamentoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtPacote: TextView = itemView.findViewById(R.id.txtPacoteCard)
        val txtVeiculo: TextView = itemView.findViewById(R.id.txtVeiculoCard)
        val txtData: TextView = itemView.findViewById(R.id.txtDataCard)
        val txtHora: TextView = itemView.findViewById(R.id.txtHoraCard)
        val btnCancelar: Button = itemView.findViewById(R.id.btnCancelarAgendamento)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatusCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agendamento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtPacote.text = item.pacote
        holder.txtVeiculo.text = "Veículo: ${item.veiculo}"

        // Converte visualmente de YYYY-MM-DD para DD/MM/YYYY (Formato Brasileiro)
        val dataExibicao = if (item.data.contains("-") && item.data.length == 10) {
            val partes = item.data.split("-")
            if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else item.data
        } else {
            item.data
        }

        holder.txtData.text = "📅 $dataExibicao"
        holder.txtHora.text = "⏰ ${item.hora}"

        // Mostra a resposta do mecânico (Pendente, Confirmado, Em Manutenção...)
        StatusAgendamento.aplicarSelo(holder.txtStatus, item.status)

        // Carro já na oficina ou serviço pronto: não dá mais para cancelar
        when (item.status) {
            StatusAgendamento.EM_MANUTENCAO, StatusAgendamento.CONCLUIDO -> {
                holder.btnCancelar.visibility = View.GONE
            }
            StatusAgendamento.CANCELADO -> {
                holder.btnCancelar.visibility = View.VISIBLE
                holder.btnCancelar.text = "Remover da lista"
            }
            else -> {
                holder.btnCancelar.visibility = View.VISIBLE
                holder.btnCancelar.text = "Cancelar Agendamento"
            }
        }

        holder.btnCancelar.setOnClickListener {
            onCancelarClick(item.id)
        }
    }

    override fun getItemCount(): Int = lista.size
}

class MeusAgendamentosActivity : AppCompatActivity() {

    private lateinit var recyclerAgendamentos: RecyclerView
    private val listaAgendamentos = ArrayList<AgendamentoModel>()
    private var clienteId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meus_agendamentos)

        clienteId = intent.getIntExtra("CLIENTE_ID", 1)

        recyclerAgendamentos = findViewById(R.id.recyclerAgendamentos)
        recyclerAgendamentos.layoutManager = LinearLayoutManager(this)

        carregarAgendamentosDaApi()
    }

    private fun carregarAgendamentosDaApi() {
        val url = ApiConfig.BASE_URL + "api_agendamentos.php?acao=listar&cliente_id=$clienteId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.optBoolean("success", false)) {
                        val array = json.optJSONArray("agendamentos")
                        listaAgendamentos.clear()

                        if (array != null) {
                            for (i in 0 until array.length()) {
                                val item = array.getJSONObject(i)
                                listaAgendamentos.add(
                                    AgendamentoModel(
                                        id = item.optInt("id", 0),
                                        pacote = item.optString("pacote", "Serviço"),
                                        veiculo = item.optString("veiculo_nome", "Veículo"),
                                        data = item.optString("data", "A definir"),
                                        hora = item.optString("hora", "A definir"),
                                        status = item.optString("status", "Pendente")
                                    )
                                )
                            }
                        }

                        val adapter = AgendamentoAdapter(listaAgendamentos) { agendamentoId ->
                            confirmarCancelamento(agendamentoId)
                        }
                        recyclerAgendamentos.adapter = adapter
                    } else {
                        Toast.makeText(this, "Nenhum agendamento encontrado", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar dados", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun confirmarCancelamento(idAgendamento: Int) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Agendamento")
            .setMessage("Tem certeza que deseja cancelar este agendamento?")
            .setPositiveButton("Sim") { _, _ ->
                executarCancelamentoNaApi(idAgendamento)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun executarCancelamentoNaApi(idAgendamento: Int) {
        val url = ApiConfig.BASE_URL + "api_agendamentos.php?acao=cancelar&id=$idAgendamento"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.optBoolean("success", false)) {
                        Toast.makeText(this, "Agendamento cancelado com sucesso!", Toast.LENGTH_SHORT).show()
                        carregarAgendamentosDaApi() // Recarrega a lista
                    } else {
                        Toast.makeText(this, json.optString("message", "Erro ao cancelar"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar resposta", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }
}