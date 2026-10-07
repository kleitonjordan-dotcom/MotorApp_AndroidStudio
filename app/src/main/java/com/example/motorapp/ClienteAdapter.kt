package com.example.motorapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClienteAdapter(private val listaClientes: List<Cliente>) :
    RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tvNomeCliente)
        val tvTelefone: TextView = itemView.findViewById(R.id.tvTelefoneCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = listaClientes[position]
        holder.tvNome.text = cliente.nome
        holder.tvTelefone.text = cliente.telefone
    }

    override fun getItemCount(): Int = listaClientes.size
}