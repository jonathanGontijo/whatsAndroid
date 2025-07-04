package com.example.whatsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.whatsapp.databinding.ItemMensagensDestinatarioBinding
import com.example.whatsapp.databinding.ItemMensagensRemetenteBinding
import com.example.whatsapp.model.Mensagem
import com.example.whatsapp.utils.Constantes
import com.google.firebase.auth.FirebaseAuth

class MensagensAdapter: Adapter<ViewHolder>() {

    private var listaMensagens = emptyList<Mensagem>()
    fun adicionarLista( lista: List<Mensagem> ){
        listaMensagens = lista
        notifyDataSetChanged()
    }

    class MensagensRemetentesViewHolder(
        private val binding: ItemMensagensRemetenteBinding
    ): ViewHolder(binding.root){

        fun bind(mensagem: Mensagem){
            binding.textMensagemRemetente.text = mensagem.mensagem
        }

            companion object{
                fun inflarLayout(parent: ViewGroup): MensagensRemetentesViewHolder{
                    val inflater = LayoutInflater.from( parent.context )
                    val itemView = ItemMensagensRemetenteBinding.inflate(
                        inflater, parent, false
                    )
                    return MensagensRemetentesViewHolder( itemView )
                }
            }
    }

    class MensagensDestinatariosViewHolder(
        private val binding: ItemMensagensDestinatarioBinding
    ): ViewHolder(binding.root){

        fun bind(mensagem: Mensagem){
            binding.textMensagemDestinatario.text = mensagem.mensagem

        }

        companion object{
            fun inflarLayout(parent: ViewGroup): MensagensDestinatariosViewHolder{
                val inflater = LayoutInflater.from( parent.context )
                val itemView = ItemMensagensDestinatarioBinding.inflate(
                    inflater, parent, false
                )
                return MensagensDestinatariosViewHolder( itemView )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val mensagem = listaMensagens[position]
        val idUsuarioLogado = FirebaseAuth.getInstance().currentUser?.uid.toString()
       return if (idUsuarioLogado == mensagem.idUsuario){
           Constantes.TIPO_REMETENTE
        }else {
            Constantes.TIPO_DESTINATARIO
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType == Constantes.TIPO_REMETENTE){
          return  MensagensRemetentesViewHolder.inflarLayout(parent)
        }
           return MensagensDestinatariosViewHolder.inflarLayout(parent)


    }





    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mensagem = listaMensagens[position]
        when(holder){
            is MensagensRemetentesViewHolder -> holder.bind(mensagem)
            is MensagensDestinatariosViewHolder -> holder.bind(mensagem)
        }

    }

    override fun getItemCount(): Int {
        return listaMensagens.size
    }
}