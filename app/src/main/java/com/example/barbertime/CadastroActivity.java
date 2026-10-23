
package com.example.barbertime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class CadastroActivity extends AppCompatActivity {

    private BancoDeDados bancoDeDados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        bancoDeDados = new BancoDeDados(this);

        EditText inputCadUsername = findViewById(R.id.inputCadUsername);
        EditText inputCadEmail = findViewById(R.id.inputCadEmail);
        EditText inputCadSenha = findViewById(R.id.inputCadSenha);
        EditText inputConfSenha = findViewById(R.id.inputConfSenha);

        Button btCadastro = findViewById(R.id.btCadastro);
        Button btIrLogin = findViewById(R.id.btIrLogin);

        btCadastro.setOnClickListener(v -> {
            String username = inputCadUsername.getText().toString().trim();
            String email = inputCadEmail.getText().toString().trim();
            String senha = inputCadSenha.getText().toString();

            if (username.isEmpty() || email.isEmpty()
                    || senha.isEmpty()
                    || inputConfSenha.getText().toString().isEmpty()) {
                Toast.makeText(this,
                        "Preencha todos os campos antes de finalizar",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this,
                        "Digite um E-MAIL válido",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (senha.length() < 6) {
                Toast.makeText(this,
                        "A SENHA deve ter no mínimo 6 caracteres",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!senha.equals(inputConfSenha.getText().toString())) {
                Toast.makeText(this,
                        "SENHA e CONFIRMA SENHA precisam ser idênticos",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String senhaHash = gerarHashSenha(senha);

            if (senhaHash == null) {
                Toast.makeText(this,
                        "Erro ao proteger a senha.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Usuario usuario = new Usuario(username, email, senhaHash);
            long resultado = bancoDeDados.cadastrarUsuario(usuario);

            if (resultado != -1) {
                Toast.makeText(this,
                        "Cadastro realizado com sucesso! :)",
                        Toast.LENGTH_SHORT).show();

                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this,
                        "Nome de usuário ou e-mail já cadastrado.",
                        Toast.LENGTH_LONG).show();
            }
        });

        btIrLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private String gerarHashSenha(String senha) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec especificacao = new PBEKeySpec(
                    senha.toCharArray(), salt, 120000, 256);

            byte[] hash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1")
                    .generateSecret(especificacao)
                    .getEncoded();

            especificacao.clearPassword();

            return Base64.encodeToString(salt, Base64.NO_WRAP)
                    + ":" +
                    Base64.encodeToString(hash, Base64.NO_WRAP);

        } catch (GeneralSecurityException e) {
            return null;
        }
    }
}
