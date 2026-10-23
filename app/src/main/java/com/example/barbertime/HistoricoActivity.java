package com.example.barbertime;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HistoricoActivity extends AppCompatActivity {

    private BancoDeDados banco;
    private LinearLayout containerAgendamentos;
    private LinearLayout layoutHistoricoVazio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        banco = new BancoDeDados(this);

        containerAgendamentos =
                findViewById(R.id.containerAgendamentos);

        layoutHistoricoVazio =
                findViewById(R.id.layoutHistoricoVazio);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarAgendamentos();
    }

    private void carregarAgendamentos() {
        containerAgendamentos.removeAllViews();

        SharedPreferences preferencias =
                getSharedPreferences(
                        "BarberTimePrefs", MODE_PRIVATE);

        long idUsuario =
                preferencias.getLong("id_usuario", -1L);

        if (idUsuario <= 0) {
            layoutHistoricoVazio.setVisibility(View.VISIBLE);
            return;
        }

        Cursor cursor = banco.buscarAgendamentos(idUsuario);

        try {
            if (!cursor.moveToFirst()) {
                layoutHistoricoVazio.setVisibility(View.VISIBLE);
                return;
            }

            layoutHistoricoVazio.setVisibility(View.GONE);

            do {
                View item = getLayoutInflater().inflate(
                        R.layout.item_agendamento,
                        containerAgendamentos,
                        false
                );

                TextView txtData =
                        item.findViewById(R.id.txtItemData);

                TextView txtHorario =
                        item.findViewById(R.id.txtItemHorario);

                TextView txtServicos =
                        item.findViewById(R.id.txtItemServicos);

                TextView txtNome =
                        item.findViewById(R.id.txtItemNome);

                TextView txtTelefone =
                        item.findViewById(R.id.txtItemTelefone);

                String data = cursor.getString(
                        cursor.getColumnIndexOrThrow("data"));

                String[] partesData = data.split("-");

                if (partesData.length == 3) {
                    data = partesData[2] + "/" +
                            partesData[1] + "/" +
                            partesData[0];
                }

                txtData.setText(data);

                txtHorario.setText(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("horario"))
                );

                txtServicos.setText(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("servicos"))
                );

                txtNome.setText(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("nome_cliente"))
                );

                txtTelefone.setText(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("telefone_cliente"))
                );

                containerAgendamentos.addView(item);

            } while (cursor.moveToNext());

        } finally {
            cursor.close();
        }
    }
}