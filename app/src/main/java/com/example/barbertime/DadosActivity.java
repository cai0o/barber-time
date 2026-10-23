package com.example.barbertime;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DadosActivity extends AppCompatActivity {

    private BancoDeDados banco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados);

        banco = new BancoDeDados(this);

        TextView txtUsername = findViewById(R.id.txtDadosUsername);
        TextView txtEmail = findViewById(R.id.txtDadosEmail);
        Button btVoltar = findViewById(R.id.btVoltarMenu);

        SharedPreferences preferencias =
                getSharedPreferences("BarberTimePrefs", MODE_PRIVATE);

        long idUsuario = preferencias.getLong("id_usuario", -1L);

        if (idUsuario > 0) {
            Cursor cursor = banco.buscarUsuario(idUsuario);

            try {
                if (cursor.moveToFirst()) {
                    txtUsername.setText(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow("username"))
                    );

                    txtEmail.setText(
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow("email"))
                    );
                } else {
                    txtUsername.setText("Usuário não encontrado");
                    txtEmail.setText("");
                }
            } finally {
                cursor.close();
            }
        } else {
            txtUsername.setText("Usuário não identificado");
            txtEmail.setText("");
        }

        btVoltar.setOnClickListener(v -> finish());
    }
}