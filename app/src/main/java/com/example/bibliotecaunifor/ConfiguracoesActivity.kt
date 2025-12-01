package com.example.bibliotecaunifor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliotecaunifor.api.RetrofitClient
import com.example.bibliotecaunifor.databinding.ActivityConfiguracoesBinding
import com.example.bibliotecaunifor.models.ChangePasswordRequest
import com.example.bibliotecaunifor.models.ChangePasswordResponse
import com.example.bibliotecaunifor.utils.AuthUtils
import com.google.android.material.button.MaterialButton

class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 🔹 Botão de regulamento
        binding.tvRegulamento.setOnClickListener {
            showRegulamentoDialog()
        }

        // 🔹 Botão de alterar senha
        binding.tvAlterarSenha.setOnClickListener {
            showAlterarSenhaDialog()
        }

        // 🔹 Botão de sair (logout)
        binding.tvSair.setOnClickListener {
            showLogoutDialog()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showRegulamentoDialog() {
        AlertDialog.Builder(this)
            .setTitle("Regulamento da Biblioteca")
            .setMessage(
                """
            1. Cadastro e acesso
            - O uso da biblioteca é permitido a alunos e colaboradores regularmente cadastrados.
            - O acesso ao sistema é pessoal e intransferível. Não compartilhe sua matrícula ou senha.

            2. Empréstimo de livros
            - Cada usuário pode manter um número limitado de livros em empréstimo simultâneo, conforme regras da instituição.
            - O empréstimo é de uso exclusivo do usuário cadastrado e não deve ser repassado a terceiros.

            3. Prazos e renovação
            - O prazo padrão de empréstimo é de 7 (sete) dias corridos, salvo orientações específicas da biblioteca.
            - A renovação pode ser feita se:
              • o livro não estiver reservado por outro usuário; e
              • não houver atraso no empréstimo atual.
            - A biblioteca pode alterar prazos em períodos especiais (férias, recesso, etc.).

            4. Atrasos e penalidades
            - Em caso de atraso na devolução, poderão ser aplicadas sanções como:
              • suspensão temporária de novos empréstimos; e/ou
              • outras penalidades definidas pela biblioteca.
            - Em caso de perda ou dano do exemplar, o usuário será responsável pela reposição
              do livro ou por outra forma de compensação definida pela biblioteca.

            5. Conservação do acervo
            - É proibido escrever, rasurar, amassar, arrancar páginas ou danificar qualquer material da biblioteca.
            - Não use alimentos ou bebidas próximos aos livros para evitar danos.

            6. Uso do espaço da biblioteca
            - Mantenha silêncio e respeito aos demais usuários.
            - O uso de celular deve ser discreto e com som desligado.
            - Não é permitido reservar lugares deixando materiais pessoais sem uso por longos períodos.

            7. Disposições gerais
            - O não cumprimento deste regulamento pode acarretar suspensão de empréstimos
              e outras medidas administrativas.
            - Situações não previstas neste regulamento serão avaliadas pela equipe da biblioteca.

            Ao utilizar a biblioteca física ou digital, você declara estar ciente e de acordo com estas regras.
            """.trimIndent()
            )
            .setPositiveButton("Entendi") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun showAlterarSenhaDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_alterar_senha, null)

        val etSenhaAtual = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSenhaAtual)
        val etSenhaNova = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSenhaNova)
        val etSenhaConfirmacao = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSenhaConfirmacao)
        val btnAlterar = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAlterar)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.create()

        btnAlterar.setOnClickListener {
            val current = etSenhaAtual.text?.toString()?.trim() ?: ""
            val nova = etSenhaNova.text?.toString()?.trim() ?: ""
            val confirm = etSenhaConfirmacao.text?.toString()?.trim() ?: ""

            // validações básicas
            if (current.isEmpty() || nova.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nova.length < 6) {
                Toast.makeText(this, "A nova senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nova != confirm) {
                Toast.makeText(this, "A confirmação da senha não confere", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val body = com.example.bibliotecaunifor.models.ChangePasswordRequest(
                currentPassword = current,
                newPassword = nova
            )

            RetrofitClient.authApi.changePassword(body)
                .enqueue(object : retrofit2.Callback<com.example.bibliotecaunifor.models.ChangePasswordResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.example.bibliotecaunifor.models.ChangePasswordResponse>,
                        response: retrofit2.Response<com.example.bibliotecaunifor.models.ChangePasswordResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(
                                this@ConfiguracoesActivity,
                                "Senha alterada com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            alertDialog.dismiss()
                        } else {
                            val msg = when (response.code()) {
                                400 -> "Dados inválidos. Verifique as informações."
                                403 -> "Senha atual incorreta."
                                else -> "Erro ao alterar senha. Tente novamente."
                            }
                            Toast.makeText(
                                this@ConfiguracoesActivity,
                                msg,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.example.bibliotecaunifor.models.ChangePasswordResponse>,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            this@ConfiguracoesActivity,
                            "Falha de conexão: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }


    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sair da conta")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("SAIR") { _, _ ->
                logoutAndGoToLogin()
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun logoutAndGoToLogin() {
        AuthUtils.clear(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
