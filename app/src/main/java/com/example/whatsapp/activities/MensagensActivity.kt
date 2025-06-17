package com.example.whatsapp.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp.adapters.ConversasAdapter
import com.example.whatsapp.databinding.ActivityMensagensBinding
import com.example.whatsapp.model.Conversa
import com.example.whatsapp.model.Mensagem
import com.example.whatsapp.model.Usuario
import com.example.whatsapp.utils.Constantes
import com.example.whatsapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso

class MensagensActivity : AppCompatActivity() {

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val binding by lazy {
        ActivityMensagensBinding.inflate( layoutInflater )
    }

    private lateinit var listenerRegistration: ListenerRegistration
    private var dadosDestinatario: Usuario? = null
    private var dadosUsuarioRemetente: Usuario? = null
    private lateinit var  conversarAdapter: ConversasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( binding.root )
        recuperarDadosUsuarios()
        inicializarToolbar()
        inicializarEventoClique()
        inicializarRecyclerview()
        inicializarListeners()

    }

    private fun inicializarRecyclerview() {
        with(binding){

            conversarAdapter = ConversasAdapter()
            rvMensagens.adapter = conversarAdapter
            rvMensagens.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    private fun inicializarListeners() {

        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsuarioDestinatario = dadosDestinatario?.id
        if( idUsuarioRemetente != null && idUsuarioDestinatario != null ){

             listenerRegistration =    firestore
                .collection(Constantes.MENSAGENS)
                .document(idUsuarioRemetente)
                .collection(idUsuarioDestinatario)
                .orderBy("data", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, erro ->
                    if (erro != null) {
                        exibirMensagem("Erro ao recuperar mensagem")
                    }
                    val listaMensagem = mutableListOf<Mensagem>()
                    val documentos = querySnapshot?.documents

                    documentos?.forEach{ documentSnapshot ->
                        val mensagem = documentSnapshot.toObject(Mensagem::class.java)
                        if (mensagem != null){
                            listaMensagem.add(mensagem)
                            Log.i("exibicao_mensagem", mensagem.mensagem)
                        }
                    }

                    //Lista
                    if (listaMensagem.isNotEmpty()){
                        //Carregar os dados Adapter
                        conversarAdapter.adicionarLista(listaMensagem)
                    }
                }
        }
    }

    private fun inicializarEventoClique() {

        binding.fabEnviar.setOnClickListener {
            val mensagem = binding.editMensagem.text.toString()
            salvarMensagem( mensagem )
        }

    }

    private fun salvarMensagem( textoMensagem: String ) {
        if( textoMensagem.isNotEmpty() ){

            val idUsuarioRemetente = firebaseAuth.currentUser?.uid
            val idUsuarioDestinatario = dadosDestinatario?.id
            if( idUsuarioRemetente != null && idUsuarioDestinatario != null ){
                val mensagem = Mensagem(
                    idUsuarioRemetente, textoMensagem
                )

                //Salvar para o Remetente
                salvarMensagemFirestore(
                    idUsuarioRemetente, idUsuarioDestinatario, mensagem
                )

                //foto e nome destinatario
                val conversaRemetente = Conversa(
                    idUsuarioRemetente, idUsuarioDestinatario,
                    dadosDestinatario!!.foto, dadosDestinatario!!.nome,
                    textoMensagem
                )

                salvarConversaFirestore(conversaRemetente)

                //Salvar mesma mensagem para o destinatario
                salvarMensagemFirestore(
                    idUsuarioDestinatario, idUsuarioRemetente, mensagem
                )

                //fote e nome remetente
                val conversaDestinatario = Conversa(
                    idUsuarioDestinatario, idUsuarioRemetente,
                    dadosUsuarioRemetente!!.foto, dadosUsuarioRemetente!!.nome,
                    textoMensagem
                )

                salvarConversaFirestore(conversaDestinatario)

                binding.editMensagem.setText("")

            }

        }
    }

    private fun salvarConversaFirestore(conversa: Conversa) {

        firestore
            .collection(Constantes.CONVERSAS)
            .document(conversa.idUsuarioRemetente)
            .collection(Constantes.ULTIMAS_CONVERSAS)
            .document(conversa.idUsuarioDestinatario)
            .set(conversa)
            .addOnFailureListener {
                exibirMensagem("Error ao salvar conversa")
            }

    }

    private fun salvarMensagemFirestore(
        idUsuarioRemetente: String,
        idUsuarioDestinatario: String,
        mensagem: Mensagem
    ) {

        firestore
            .collection(Constantes.MENSAGENS)
            .document( idUsuarioRemetente )
            .collection( idUsuarioDestinatario )
            .add( mensagem )
            .addOnFailureListener {
                exibirMensagem("Erro ao enviar mensagem")
            }

    }

    private fun inicializarToolbar() {
        val toolbar = binding.tbMensagens
        setSupportActionBar( toolbar )
        supportActionBar?.apply {
            title = ""
            if( dadosDestinatario != null ){
                binding.textNome.text = dadosDestinatario!!.nome
                Picasso.get()
                    .load(dadosDestinatario!!.foto)
                    .into( binding.imageFotoPerfil )
            }
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun recuperarDadosUsuarios() {


        //Dados do Usuario Remetente
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        if(idUsuarioRemetente !=null){
            firestore
                .collection(Constantes.USUARIOS)
                .document()
                .get()
                .addOnSuccessListener {
                    documentSnapshot ->

                    val usuario = documentSnapshot.toObject(Usuario::class.java)
                    if (usuario != null){
                        dadosUsuarioRemetente = usuario
                    }
                }
        }


        //Recuperando dados do destinatario
        val extras = intent.extras
        if( extras != null ){

            val origem = extras.getString("origem")
            if( origem == Constantes.ORIGEM_CONTATO ){

                dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable(
                        "dadosDestinatario",
                        Usuario::class.java
                    )
                }else{
                    extras.getParcelable(
                        "dadosDestinatario"
                    )
                }

            }else if( origem == Constantes.ORIGEM_CONVERSA ){
                //Recuperar os dados da conversa

            }

        }

    }
}