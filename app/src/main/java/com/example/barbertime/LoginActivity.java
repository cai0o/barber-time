
package com.example.barbertime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.security.MessageDigest;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class LoginActivity extends AppCompatActivity {

    private BancoDeDados bancoDeDados;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this);

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        View raiz = findViewById(R.id.login);

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets teclado = insets.getInsets(
                    WindowInsetsCompat.Type.ime());

            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars());

            int inferior = Math.max(teclado.bottom, barras.bottom);

            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    inferior
            );

            return insets;
        });

        bancoDeDados = new BancoDeDados(getApplicationContext());

        EditText inputLogEmail = findViewById(R.id.inputLogEmail);
        EditText inputLogSenha = findViewById(R.id.inputLogSenha);
        Button btLogin = findViewById(R.id.btLogin);
        Button btTelaCadastro = findViewById(R.id.btTelaCadastro);

        btLogin.setOnClickListener(v -> {

            String login = inputLogEmail.getText().toString().trim();
            String senha = inputLogSenha.getText().toString();

            if (login.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this,
                        "Os campos \"E-mail\" e \"Senha\" devem estar preenchidos!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btLogin.setEnabled(false);

            executor.execute(() -> {

                long idUsuario = -1L;
                boolean autenticado = false;
                boolean erroBanco = false;

                try {
                    SQLiteDatabase db =
                            bancoDeDados.getReadableDatabase();

                    try (Cursor cursor = db.query(
                            "usuarios",
                            new String[]{"id", "senha_hash"},
                            "username = ? COLLATE NOCASE OR email = ? COLLATE NOCASE",
                            new String[]{login, login},
                            null, null, null, "1")) {

                        if (cursor.moveToFirst()) {
                            idUsuario = cursor.getLong(
                                    cursor.getColumnIndexOrThrow("id"));

                            String senhaHash = cursor.getString(
                                    cursor.getColumnIndexOrThrow("senha_hash"));

                            autenticado = verificarSenha(senha, senhaHash);
                        }
                    }

                } catch (Exception e) {
                    Log.e("LoginActivity", "Erro ao realizar login", e);
                    erroBanco = true;
                }

                final long usuarioId = idUsuario;
                final boolean resultadoLogin = autenticado;
                final boolean falhaBanco = erroBanco;

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (falhaBanco) {
                        Toast.makeText(this,
                                "Não foi possível realizar o login. Tente novamente.",
                                Toast.LENGTH_SHORT).show();
                        btLogin.setEnabled(true);
                        return;
                    }

                    if (resultadoLogin) {
                        SharedPreferences preferencias =
                                getSharedPreferences(
                                        "BarberTimePrefs", MODE_PRIVATE);

                        preferencias.edit()
                                .putLong("id_usuario", usuarioId)
                                .apply();

                        startActivity(
                                new Intent(this, MainActivity.class));

                        finish();
                        return;
                    }

                    Toast.makeText(this,
                            "Usuário ou senha não encontrados",
                            Toast.LENGTH_SHORT).show();

                    btLogin.setEnabled(true);
                });
            });
        });

        btTelaCadastro.setOnClickListener(v -> {
            startActivity(
                    new Intent(this, CadastroActivity.class));
        });
    }

    private boolean verificarSenha(String senha, String senhaHash) {

        if (senhaHash == null || senhaHash.isEmpty()) {
            return false;
        }

        PBEKeySpec especificacao = null;

        try {
            String[] partes = senhaHash.split(":");

            if (partes.length != 2) {
                return false;
            }

            byte[] salt = Base64.decode(
                    partes[0], Base64.NO_WRAP);

            byte[] hashSalvo = Base64.decode(
                    partes[1], Base64.NO_WRAP);

            if (salt.length == 0 || hashSalvo.length != 32) {
                return false;
            }

            especificacao = new PBEKeySpec(
                    senha.toCharArray(),
                    salt,
                    120000,
                    256
            );

            byte[] hashDigitado = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1")
                    .generateSecret(especificacao)
                    .getEncoded();

            return MessageDigest.isEqual(
                    hashSalvo, hashDigitado);

        } catch (GeneralSecurityException
                 | IllegalArgumentException e) {
            Log.e("LoginActivity", "Erro ao verificar senha", e);
            return false;

        } finally {
            if (especificacao != null) {
                especificacao.clearPassword();
            }
        }
    }

    @Override
    protected void onDestroy() {
        executor.execute(() -> bancoDeDados.close());
        executor.shutdown();
        super.onDestroy();
    }
}
