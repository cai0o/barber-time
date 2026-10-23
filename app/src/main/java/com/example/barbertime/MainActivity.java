package com.example.barbertime;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);

                    return insets;
                });

        CardView cardNovoAgendamento =
                findViewById(R.id.cardNovoAgendamento);
        CardView cardDados = findViewById(R.id.cardDados);
        CardView cardHistorico = findViewById(R.id.cardHistorico);

        cardNovoAgendamento.setOnClickListener(v ->
                startActivity(new Intent(this, AgendamentoActivity.class)));

        cardDados.setOnClickListener(v ->
                startActivity(new Intent(this, DadosActivity.class)));

        cardHistorico.setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoActivity.class)));

        CardView cardSair = findViewById(R.id.cardSair);

        cardSair.setOnClickListener(v -> {
            SharedPreferences preferencias =
                    getSharedPreferences("BarberTimePrefs", MODE_PRIVATE);

            preferencias.edit().remove("id_usuario").apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });
    }
}
