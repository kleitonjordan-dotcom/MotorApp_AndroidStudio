package com.example.motorapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private var clienteId: Int = 1
    private lateinit var tvBoasVindas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Recupera o ID e o Nome do Login
        clienteId = intent.getIntExtra("CLIENTE_ID", 1)
        val nomeCliente = intent.getStringExtra("NOME_USUARIO") ?: "Cliente"

        tvBoasVindas = findViewById(R.id.tvBoasVindas)

        // Exibe a saudação personalizada
        tvBoasVindas.text = "Olá, $nomeCliente"

        // Configurar clique no card da Dashboard: "MEUS VEÍCULOS" (Ver a lista)
        val cardMeusVeiculos = findViewById<CardView>(R.id.cardMeusVeiculos)
        cardMeusVeiculos.setOnClickListener {
            val intent = Intent(this, VeiculosActivity::class.java)
            intent.putExtra("CLIENTE_ID", clienteId)
            startActivity(intent)
        }

        // Configurar clique no NOVO card da Dashboard: "CADASTRAR VEÍCULO"
        val cardCadastrarVeiculo = findViewById<CardView>(R.id.cardCadastrarVeiculo)
        cardCadastrarVeiculo.setOnClickListener {
            val intent = Intent(this, CadastrarVeiculoActivity::class.java)
            intent.putExtra("CLIENTE_ID", clienteId)
            startActivity(intent)
        }

        // Configurar clique no card: "MEUS DADOS"
        val btnMenuDados = findViewById<CardView>(R.id.btnMenuDados)
        btnMenuDados.setOnClickListener {
            // Aqui você pode abrir a activity de dados futuramente
        }

        // Botão Sair no Topo
        val btnSair = findViewById<android.widget.Button>(R.id.btnSair)
        btnSair.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}