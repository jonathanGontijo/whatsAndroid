package com.example.whatsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.whatsapp.databinding.ItemContatosBinding
import com.example.whatsapp.databinding.ItemConversasBinding
import com.example.whatsapp.model.Conversa
import com.squareup.picasso.Picasso

class ConversasAdapter (
    private val onClick: (Conversa) -> Unit
) : Adapter<ConversasAdapter.ConversasViewHolder>() {


    private var listaConversa = emptyList<Conversa>()
    fun adicionarLista( lista: List<Conversa> ){
        listaConversa = lista
        notifyDataSetChanged()
    }

    inner class ConversasViewHolder(
        private val binding: ItemConversasBinding
    ) : RecyclerView.ViewHolder( binding.root ){

        fun bind( conversa: Conversa ){

            binding.textConversaNome.text = conversa.nome
            binding.textConversaNome.text = conversa.ultimaMensagem
            Picasso.get()
                .load( conversa.foto )
                .into( binding.imageConversaFoto )

            //evento de clique
            binding.clItemConversa.setOnClickListener{
                onClick(conversa)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversasViewHolder {
        val inflater = LayoutInflater.from( parent.context )
        val itemView = ItemConversasBinding.inflate(
            inflater, parent, false
        )
        return ConversasViewHolder( itemView )

    }



    override fun onBindViewHolder(holder: ConversasViewHolder, position: Int) {
        val conversa = listaConversa[position]
        holder.bind( conversa )
    }

    override fun getItemCount(): Int {
        return  listaConversa.size
    }
}