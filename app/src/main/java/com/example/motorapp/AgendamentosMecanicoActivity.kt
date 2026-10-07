package com.example.motorapp

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
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

// ---------------------------------------------------------------------
// Modelo de um agendamento na visão do mecânico
// (nome diferente do AgendamentoModel do cliente para não dar conflito)
// ---------------------------------------------------------------------
data class AgendamentoMecanico(
    val id: Int,
    val pacote: String,
    val data: String,
    val hora: String,
    val status: String,
    val clienteNome: String,
    val telefone: String,
    val veiculo: String,
    val placa: String
)

// ---------------------------------------------------------------------
// Adapter da lista: monta cada card e decide quais botões aparecem
// ---------------------------------------------------------------------
class AgendamentoMecanicoAdapter(
    private val lista: List<AgendamentoMecanico>,
    private val onMudarStatus: (AgendamentoMecanico, String) -> Unit,
    private val onLigar: (String) -> Unit
) : RecyclerView.Adapter<AgendamentoMecanicoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtPacote: TextView = itemView.findViewById(R.id.txtPacoteMec)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatusMec)
        val txtDataHora: TextView = itemView.findViewById(R.id.txtDataHoraMec)
        val txtVeiculo: TextView = itemView.findViewById(R.id.txtVeiculoMec)
        val txtCliente: TextView = itemView.findViewById(R.id.txtClienteMec)
        val txtTelefone: TextView = itemView.findViewById(R.id.txtTelefoneMec)
        val layoutBotoes: LinearLayout = itemView.findViewById(R.id.layoutBotoesMec)
        val btnPrincipal: Button = itemView.findViewById(R.id.btnAcaoPrincipal)
        val btnSecundario: Button = itemView.findViewById(R.id.btnAcaoSecundaria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agendamento_mecanico, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.txtPacote.text = "📦 ${item.pacote}"
        StatusAgendamento.aplicarSelo(holder.txtStatus, item.status)
        holder.txtDataHora.text = "📅 ${StatusAgendamento.formatarData(item.data)} às ${item.hora}"
        holder.txtVeiculo.text = if (item.placa.isNotEmpty()) "🚗 ${item.veiculo}  •  ${item.placa}" else "🚗 ${item.veiculo}"
        holder.txtCliente.text = "👤 ${item.clienteNome}"

        if (item.telefone.isNotEmpty()) {
            holder.txtTelefone.visibility = View.VISIBLE
            holder.txtTelefone.text = "📞 ${item.telefone}  (toque para ligar)"
            holder.txtTelefone.setOnClickListener { onLigar(item.telefone) }
        } else {
            holder.txtTelefone.visibility = View.GONE
        }

        // Botões mudam conforme a etapa do serviço
        holder.btnSecundario.visibility = View.VISIBLE
        holder.layoutBotoes.visibility = View.VISIBLE
        when (item.status) {
            StatusAgendamento.PENDENTE -> {
                configurarBotao(holder.btnPrincipal, "✔ Confirmar", StatusAgendamento.CONFIRMADO) { onMudarStatus(item, StatusAgendamento.CONFIRMADO) }
                configurarBotao(holder.btnSecundario, "✖ Recusar", StatusAgendamento.CANCELADO) { onMudarStatus(item, StatusAgendamento.CANCELADO) }
            }
            StatusAgendamento.CONFIRMADO -> {
                configurarBotao(holder.btnPrincipal, "🛠 Iniciar serviço", StatusAgendamento.EM_MANUTENCAO) { onMudarStatus(item, StatusAgendamento.EM_MANUTENCAO) }
                configurarBotao(holder.btnSecundario, "✖ Cancelar", StatusAgendamento.CANCELADO) { onMudarStatus(item, StatusAgendamento.CANCELADO) }
            }
            StatusAgendamento.EM_MANUTENCAO -> {
                configurarBotao(holder.btnPrincipal, "🏁 Concluir serviço", StatusAgendamento.CONCLUIDO) { onMudarStatus(item, StatusAgendamento.CONCLUIDO) }
                holder.btnSecundario.visibility = View.GONE
            }
            StatusAgendamento.CANCELADO -> {
                // Permite desfazer uma recusa feita por engano
                configurarBotao(holder.btnPrincipal, "↩ Reabrir como pendente", StatusAgendamento.PENDENTE) { onMudarStatus(item, StatusAgendamento.PENDENTE) }
                holder.btnSecundario.visibility = View.GONE
            }
            else -> holder.layoutBotoes.visibility = View.GONE // Concluído: nada a fazer
        }
    }

    private fun configurarBotao(botao: Button, texto: String, statusCor: String, acao: () -> Unit) {
        botao.text = texto
        botao.backgroundTintList = ColorStateList.valueOf(StatusAgendamento.cor(statusCor))
        botao.setOnClickListener { acao() }
    }

    override fun getItemCount(): Int = lista.size
}

// ---------------------------------------------------------------------
// Tela da lista
// ---------------------------------------------------------------------
class AgendamentosMecanicoActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var tvVazia: TextView
    private lateinit var tvQtd: TextView
    private val lista = ArrayList<AgendamentoMecanico>()

    private var mecanicoId = 0
    private var filtro = StatusAgendamento.TODOS
    private var apenasHoje = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendamentos_mecanico)

        mecanicoId = intent.getIntExtra("MECANICO_ID", 0)
        filtro = intent.getStringExtra("FILTRO") ?: StatusAgendamento.TODOS
        apenasHoje = intent.getBooleanExtra("APENAS_HOJE", false)

        findViewById<TextView>(R.id.tvTituloLista).text = when {
            apenasHoje -> "Serviços de hoje"
            filtro == StatusAgendamento.TODOS -> "Todos os agendamentos"
            else -> filtro
        }
        findViewById<Button>(R.id.btnVoltarLista).setOnClickListener { finish() }

        tvVazia = findViewById(R.id.tvListaVazia)
        tvQtd = findViewById(R.id.tvQtdLista)
        recycler = findViewById(R.id.recyclerAgendamentosMecanico)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = AgendamentoMecanicoAdapter(lista, ::perguntarMudancaStatus, ::abrirDiscador)
    }

    override fun onResume() {
        super.onResume()
        carregar()
    }

    private fun carregar() {
        var url = ApiConfig.BASE_URL + "api_mecanico.php?acao=listar&mecanico_id=$mecanicoId" +
                "&status=" + Uri.encode(filtro)
        if (apenasHoje) url += "&periodo=hoje"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (!json.getBoolean("success")) {
                        Toast.makeText(this, json.optString("message"), Toast.LENGTH_SHORT).show()
                        return@StringRequest
                    }
                    val array = json.getJSONArray("agendamentos")
                    lista.clear()
                    for (i in 0 until array.length()) {
                        val a = array.getJSONObject(i)
                        lista.add(
                            AgendamentoMecanico(
                                id = a.getInt("id"),
                                pacote = a.optString("pacote", "Serviço"),
                                data = a.optString("data_agendamento", ""),
                                hora = a.optString("hora_agendamento", "").take(5),
                                status = a.optString("status", StatusAgendamento.PENDENTE),
                                clienteNome = a.optString("cliente_nome", "Cliente"),
                                telefone = a.optString("telefone", ""),
                                veiculo = "${a.optString("marca", "")} ${a.optString("modelo", "")}".trim(),
                                placa = a.optString("placa", "").uppercase()
                            )
                        )
                    }
                    recycler.adapter?.notifyDataSetChanged()

                    val qtd = lista.size
                    tvQtd.text = if (qtd == 1) "1 agendamento" else "$qtd agendamentos"
                    tvVazia.visibility = if (qtd == 0) View.VISIBLE else View.GONE
                    recycler.visibility = if (qtd == 0) View.GONE else View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar lista", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }

    /** Pede confirmação antes de mudar o status (evita toque acidental). */
    private fun perguntarMudancaStatus(item: AgendamentoMecanico, novoStatus: String) {
        val mensagem = when (novoStatus) {
            StatusAgendamento.CONFIRMADO -> "Confirmar o agendamento de ${item.clienteNome} para ${StatusAgendamento.formatarData(item.data)} às ${item.hora}?"
            StatusAgendamento.EM_MANUTENCAO -> "Marcar o ${item.veiculo} como EM MANUTENÇÃO?"
            StatusAgendamento.CONCLUIDO -> "Marcar o serviço do ${item.veiculo} como CONCLUÍDO?"
            StatusAgendamento.CANCELADO -> "Deseja realmente recusar/cancelar este agendamento?"
            else -> "Mudar o status para \"$novoStatus\"?"
        }
        AlertDialog.Builder(this)
            .setTitle(item.pacote)
            .setMessage(mensagem)
            .setPositiveButton("Sim") { _, _ -> mudarStatus(item.id, novoStatus) }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun mudarStatus(id: Int, novoStatus: String) {
        val url = ApiConfig.BASE_URL + "api_mecanico.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    Toast.makeText(this, json.optString("message"), Toast.LENGTH_SHORT).show()
                    if (json.getBoolean("success")) carregar()
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
                params["acao"] = "atualizar"
                params["mecanico_id"] = mecanicoId.toString()
                params["id"] = id.toString()
                params["status"] = novoStatus
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    /** Abre o discador com o número do cliente (não precisa de permissão). */
    private fun abrirDiscador(telefone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefone"))
        startActivity(intent)
    }
}
