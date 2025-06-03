package com.example.whatsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatsapp.databinding.ActivityCadastroBinding
import com.example.whatsapp.databinding.ActivityLoginBinding
import com.example.whatsapp.model.Usuario
import com.example.whatsapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {

    private  val binding by lazy{
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContentView(binding.root)
        inicializarToolbar()
        inicializarEventosClique()
    }

    private fun inicializarEventosClique() {
        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()){
                cadastrarUsuario(nome, email,senha)
            }
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {
        firebaseAuth.createUserWithEmailAndPassword(
            email, senha
        ).addOnCompleteListener { resultado ->
            if(resultado.isSuccessful){

                val idUsuario = resultado.result.user?.uid
                if (idUsuario != null){
                    val usuario = Usuario(
                        idUsuario, nome, email,
                    )
                    salvarUsuarioFirestore(
                        usuario
                    )
                }



            }
        }.addOnFailureListener() { erro ->
            try {
                throw erro
            } catch (erroCredenciaisInvalidas: FirebaseAuthInvalidCredentialsException){
                exibirMensagem("E-mail inválido, digite um outro e-mail")
            } catch (erroUsuarioExistente: FirebaseAuthUserCollisionException){
                exibirMensagem("E-mail já pertence a outro usuário")
            }catch (erroSenhaFraca: FirebaseAuthWeakPasswordException){
                exibirMensagem("Senha Fraca")
            }

        }
    }

    private fun salvarUsuarioFirestore(usuario: Usuario) {
        firestore
            .collection("usuarios")
            .document(usuario.id)
            .set(usuario).
            addOnCompleteListener {
                exibirMensagem("Sucesso ao fazer seu cadastro")
                startActivity(
                    Intent(
                        applicationContext, MainActivity::class.java
                    )
                )
            }.addOnFailureListener {
                exibirMensagem("Erro ao fazer seu cadastro")
            }
    }

    private fun validarCampos(): Boolean {

        nome = binding.editNome.text.toString()
        email = binding.editEmail.text.toString()
        senha = binding.editSenha.text.toString()

        if (nome.isNotEmpty()){
            binding.textInputLayoutNome.error = null

            if (email.isNotEmpty()){
                binding.textInputLayoutEmail.error = null

                if (senha.isNotEmpty()){
                    binding.textInputLayoutSenha.error = null
                    return true

                }else{
                    binding.textInputLayoutSenha.error = "Preencha o sue senha!"
                    return false
                }

            }else{
                binding.textInputLayoutEmail.error = "Preencha o sue e-mail!"
                return false
            }

        }else {
            binding.textInputLayoutNome.error = "Preencha o sue nome!"
            return  false

        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Faça o seu cadastro"
            setDisplayHomeAsUpEnabled(true)
        }
    }
}


