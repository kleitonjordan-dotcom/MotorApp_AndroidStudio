package com.example.motorapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Calendar

class AgendamentoActivity : AppCompatActivity() {

    private lateinit var spinnerVeiculos: Spinner
    private var clienteId: Int = 1
    private val listaVeiculosIds = ArrayList<Int>()
    private val listaVeiculosNomes = ArrayList<String>()

    // Variáveis para guardar temporariamente a data e hora escolhidas
    private var dataSelecionada: String = ""
    private var horaSelecionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendamento)

        clienteId = intent.getIntExtra("CLIENTE_ID", 1)
        spinnerVeiculos = findViewById(R.id.spinnerVeiculos)

        carregarVeiculosSpinner()

        // Em vez de enviar direto, agora chamamos o seletor de data e hora primeiro
        findViewById<Button>(R.id.btnBasico).setOnClickListener {
            abrirSeletorDataHora("Pacote Básico – Revisão Essencial")
        }

        findViewById<Button>(R.id.btnIntermediario).setOnClickListener {
            abrirSeletorDataHora("Pacote Intermediário – Manutenção Completa")
        }

        findViewById<Button>(R.id.btnCompleto).setOnClickListener {
            abrirSeletorDataHora("Pacote Completo – Diagnóstico Total")
        }
    }

    private fun carregarVeiculosSpinner() {
        val url = ApiConfig.BASE_URL + "api_veiculos.php?acao=listar&cliente_id=$clienteId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val veiculosArray = json.getJSONArray("veiculos")
                        listaVeiculosIds.clear()
                        listaVeiculosNomes.clear()

                        for (i in 0 until veiculosArray.length()) {
                            val item = veiculosArray.getJSONObject(i)
                            listaVeiculosIds.add(item.getInt("id"))
                            listaVeiculosNomes.add("${item.getString("marca")} ${item.getString("modelo")} (${item.getString("placa")})")
                        }

                        if (listaVeiculosNomes.isEmpty()) {
                            listaVeiculosNomes.add("Cadastre um veículo primeiro")
                        }

                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaVeiculosNomes)
                        spinnerVeiculos.adapter = adapter
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao carregar veículos", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun abrirSeletorDataHora(pacote: String) {
        if (listaVeiculosIds.isEmpty()) {
            Toast.makeText(this, "Você precisa ter um veículo cadastrado para agendar!", Toast.LENGTH_SHORT).show()
            return
        }

        val calendario = Calendar.getInstance()
        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        // 1. Abre o Calendário (Alterado para o formato YYYY-MM-DD que o banco aceita)
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            dataSelecionada = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)

            // 2. Após escolher a data, abre o Relógio
            abrirSeletorHora(pacote)

        }, ano, mes, dia)

        datePickerDialog.show()
    }

    private fun abrirSeletorHora(pacote: String) {
        val calendario = Calendar.getInstance()
        val horaAtual = calendario.get(Calendar.HOUR_OF_DAY)
        val minutoAtual = calendario.get(Calendar.MINUTE)

        // Abre o Relógio
        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            horaSelecionada = String.format("%02d:%02d", hourOfDay, minute)

            // 3. Com o pacote, veículo, data e hora prontos, envia para o servidor PHP!
            enviarAgendamentoParaServidor(pacote)

        }, horaAtual, minutoAtual, true)

        timePickerDialog.show()
    }

    private fun enviarAgendamentoParaServidor(pacote: String) {
        val indexSelecionado = spinnerVeiculos.selectedItemPosition
        val veiculoId = listaVeiculosIds[indexSelecionado]
        val url = ApiConfig.BASE_URL + "api_agendamentos.php"

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
                    Toast.makeText(this, "Erro ao processar agendamento", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["acao"] = "cadastrar"
                params["cliente_id"] = clienteId.toString()
                params["veiculo_id"] = veiculoId.toString()
                params["pacote"] = pacote
                params["data"] = dataSelecionada // Envia a data escolhida
                params["hora"] = horaSelecionada // Envia a hora escolhida
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }
}