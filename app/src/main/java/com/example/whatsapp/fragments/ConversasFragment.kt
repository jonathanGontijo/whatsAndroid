package com.example.whatsapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.whatsapp.R
import com.example.whatsapp.adapters.ContatosAdapter
import com.example.whatsapp.databinding.FragmentContatosBinding
import com.example.whatsapp.databinding.FragmentConversasBinding
import com.example.whatsapp.model.Conversa
import com.example.whatsapp.utils.Constantes
import com.example.whatsapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ConversasFragment : Fragment() {

    private lateinit var binding: FragmentConversasBinding
    private lateinit var eventoSnapshot: ListenerRegistration


    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentConversasBinding.inflate(
            inflater, container, false
        )

        return binding
    }

    override fun onStart() {
        super.onStart()
        adicionarListenerConversas()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }

    private fun adicionarListenerConversas() {

        val idusuarioRemetente = firebaseAuth.currentUser?.uid
        if (idusuarioRemetente != null) {


            eventoSnapshot = firestore
                .collection(Constantes.USUARIOS)
                .document(idusuarioRemetente)
                .collection(Constantes.ULTIMAS_CONVERSAS)
                .addSnapshotListener { querySnapshot, erro ->
                    if (erro != null){
                        activity?.exibirMensagem("Erro ao recuperar conversas")
                    }
                    val listaConversas = mutableListOf<Conversa>()
                    val documentos = querySnapshot?.documents

                    documentos?.forEach { documentSnapshot ->
                        val conversa = documentSnapshot.toObject(Conversa::class.java)
                        if (conversa != null){
                            listaConversas.add(conversa)
                        }
                    }

                    //atualizar o adapter
                    if (listaConversas.isNotEmpty()){

                    }
                }
        }
    }


}