package com.example.whatsapp.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatsapp.R
import com.example.whatsapp.databinding.ActivityCadastroBinding
import com.example.whatsapp.databinding.ActivityMensagensBinding
import com.example.whatsapp.model.Usuario
import com.example.whatsapp.utils.Constants
import com.squareup.picasso.Picasso

class MensagensActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMensagensBinding.inflate( layoutInflater )
    }

    private var dadosDestinatario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mensagens)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setContentView(binding.root)
        recuperarDadosUsuarioDestinatario()
        inicializarToolbar()

    }

    private fun inicializarToolbar() {
        val toolbar = binding.tbMensagens
        setSupportActionBar( toolbar )
        supportActionBar?.apply {
            title = ""
            if (dadosDestinatario != null){
                binding.textNome.text = dadosDestinatario!!.nome
                Picasso.get()
                    .load(dadosDestinatario!!.foto)
                    .into(binding.imageFotoPerfil)

            }
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun recuperarDadosUsuarioDestinatario() {
        val extras = intent.extras
        if (extras != null){
            val origem = extras.getString("origem")
            if (origem == Constants.ORIGEM_CONTATO){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    dadosDestinatario = extras.getParcelable("dadosDestinataio", Usuario::class.java)
                }else{
                    dadosDestinatario = extras.getParcelable("dadosDestinataio"
                    )
                }

            }else if (origem == Constants.ORIGEM_CONVERSA){

            }
        }
    }


}