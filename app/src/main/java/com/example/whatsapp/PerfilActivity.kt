package com.example.whatsapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatsapp.databinding.ActivityCadastroBinding
import com.example.whatsapp.databinding.ActivityPerfilBinding
import com.example.whatsapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class PerfilActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPerfilBinding.inflate( layoutInflater )
    }
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }
    
    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){ uri ->
        if (uri != null){
            binding.imagePerfil.setImageURI(uri)
            uploadImageStorage(uri)
        }else{
            exibirMensagem("Nenhuma mensagem selecionada")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( binding.root )
        inicializarToolbar()
        solicitarPermissoes()
        inicializarEventosClique()
    }

    private fun uploadImageStorage(uri: Uri) {
        val idUsuario = firebaseAuth.currentUser?.uid
        if (idUsuario !=null) {

            storage
                .getReference("fotos")
                .child("usuarios")
                .child("id")
                .child("perfil.jpg")
                .putFile(uri)
                .addOnSuccessListener { task ->
                    exibirMensagem("Sucesso ao fazer upload da imagem")
                    task.metadata
                        ?.reference
                        ?.downloadUrl
                        ?.addOnSuccessListener{ url ->
                            val dados = mapOf(
                                "foto" to url.toString()
                            )
                            atualizarDadosPerfil(idUsuario, dados)
                    }
                }.addOnFailureListener {
                    exibirMensagem("Erro ao fazer upload da image")
                }
        }
    }

    private fun atualizarDadosPerfil(idUsuario: String, dados: Map<String, String>){
        firestore
            .collection("usuarios")
            .document(idUsuario).update(dados)
            .addOnSuccessListener {
                exibirMensagem("Sucesso ao atualizar perfil")
            }.addOnFailureListener {
                exibirMensagem("Error ao atualizar o perfil")
            }
    }

    private fun inicializarEventosClique() {
        binding.fabSelecionar.setOnClickListener{
            if (temPermissaoGaleria){
                gerenciadorGaleria.launch("image/*")
            }else{
                exibirMensagem("Não tem permissão para acessar a galeria")
            }
        }

        binding.btnAtualizarPerfil.setOnClickListener {
            val nomeUsuario = binding.editNomePerfil.text.toString()
            if (nomeUsuario.isNotEmpty()){
               val idUsuario = firebaseAuth.currentUser?.uid
                if (idUsuario !=null){
                    val dados = mapOf(
                        "nome" to nomeUsuario
                    )
                    atualizarDadosPerfil(idUsuario, dados)
                }

            }else{
                exibirMensagem("Preencha o nome para atualizar")
            }
        }
    }

    private fun solicitarPermissoes() {

        //Verifico se usuário já tem permissão
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        //LISTA DE PERMISSÕES NEGADAS
        val listaPermissoesNegadas = mutableListOf<String>()
        if( !temPermissaoCamera )
            listaPermissoesNegadas.add( Manifest.permission.CAMERA )
        if( !temPermissaoGaleria )
            listaPermissoesNegadas.add( Manifest.permission.READ_MEDIA_IMAGES )

        if( listaPermissoesNegadas.isNotEmpty() ){

            //Solicitar multiplas permissões
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ){ permissoes ->

                temPermissaoCamera = permissoes[Manifest.permission.CAMERA]
                    ?: temPermissaoCamera

                temPermissaoGaleria = permissoes[Manifest.permission.READ_MEDIA_IMAGES]
                    ?: temPermissaoGaleria

            }
            gerenciadorPermissoes.launch( listaPermissoesNegadas.toTypedArray() )

        }

    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarPerfil.tbPrincipal
        setSupportActionBar( toolbar )
        supportActionBar?.apply {
            title = "Editar perfil"
            setDisplayHomeAsUpEnabled(true)
        }
    }

}