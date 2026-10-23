package com.example.barbertime;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BancoDeDados extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "BarberTime.db";
    private static final int VERSAO_BANCO = 1;

    public BancoDeDados(Context context) {
        super(context, NOME_BANCO, null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlUsuarios =
                "CREATE TABLE usuarios (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                        "email TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                        "senha_hash TEXT NOT NULL" +
                        ")";

        db.execSQL(sqlUsuarios);

        String sqlAgendamentos =
                "CREATE TABLE agendamentos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "id_usuario INTEGER NOT NULL, " +
                        "nome_cliente TEXT NOT NULL, " +
                        "telefone_cliente TEXT NOT NULL, " +
                        "data TEXT NOT NULL, " +
                        "horario TEXT NOT NULL, " +
                        "total REAL NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'ATIVO' " +
                        "CHECK (status IN ('ATIVO', 'CANCELADO')), " +
                        "FOREIGN KEY (id_usuario) REFERENCES usuarios(id)" +
                        ")";

        db.execSQL(sqlAgendamentos);

        String sqlIndiceHorarios =
                "CREATE UNIQUE INDEX indice_horarios_ativos " +
                        "ON agendamentos (data, horario) " +
                        "WHERE status = 'ATIVO'";

        db.execSQL(sqlIndiceHorarios);

        String sqlServicos =
                "CREATE TABLE servicos_agendamento (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "id_agendamento INTEGER NOT NULL, " +
                        "nome_servico TEXT NOT NULL, " +
                        "preco REAL NOT NULL CHECK (preco >= 0), " +
                        "FOREIGN KEY (id_agendamento) REFERENCES agendamentos(id)" +
                        ")";

        db.execSQL(sqlServicos);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int versaoAntiga,
            int versaoNova) {
        // As atualizações serão implementadas quando necessárias.
    }

    public long cadastrarUsuario(Usuario usuario) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("username", usuario.getUsername());
        valores.put("email", usuario.getEmail());
        valores.put("senha_hash", usuario.getSenhaHash());

        return db.insert("usuarios", null, valores);
    }

    public List<String> buscarHorariosOcupados(String data) {
        List<String> horariosOcupados = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT horario FROM agendamentos " +
                        "WHERE data = ? AND status = 'ATIVO'",
                new String[]{data}
        );

        try {
            while (cursor.moveToNext()) {
                horariosOcupados.add(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }

        return horariosOcupados;
    }


    public long salvarAgendamento(
            long idUsuario,
            String nomeCliente,
            String telefoneCliente,
            String data,
            String horario,
            double total,
            List<String> nomesServicos,
            List<Double> precosServicos) {

        if (idUsuario <= 0
                || nomeCliente == null || nomeCliente.trim().isEmpty()
                || telefoneCliente == null || telefoneCliente.trim().isEmpty()
                || data == null || !data.matches("\\d{4}-\\d{2}-\\d{2}")
                || horario == null || !horario.matches("\\d{2}:\\d{2}")
                || Double.isNaN(total) || Double.isInfinite(total)
                || total < 0
                || nomesServicos == null || precosServicos == null
                || nomesServicos.isEmpty()
                || nomesServicos.size() != precosServicos.size()) {
            return -1;
        }

        double soma = 0;

        for (int i = 0; i < nomesServicos.size(); i++) {
            String nome = nomesServicos.get(i);
            Double preco = precosServicos.get(i);

            if (nome == null || nome.trim().isEmpty()
                    || preco == null
                    || Double.isNaN(preco)
                    || Double.isInfinite(preco)
                    || preco < 0) {
                return -1;
            }

            soma += preco;
        }

        if (Math.abs(soma - total) > 0.001) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        long idAgendamento = -1;
        boolean transacaoIniciada = false;

        try {
            db.beginTransaction();
            transacaoIniciada = true;

            ContentValues valores = new ContentValues();
            valores.put("id_usuario", idUsuario);
            valores.put("nome_cliente", nomeCliente.trim());
            valores.put("telefone_cliente", telefoneCliente.trim());
            valores.put("data", data);
            valores.put("horario", horario);
            valores.put("total", total);
            valores.put("status", "ATIVO");

            idAgendamento = db.insertOrThrow(
                    "agendamentos", null, valores);

            for (int i = 0; i < nomesServicos.size(); i++) {
                ContentValues servico = new ContentValues();
                servico.put("id_agendamento", idAgendamento);
                servico.put("nome_servico",
                        nomesServicos.get(i).trim());
                servico.put("preco", precosServicos.get(i));

                db.insertOrThrow(
                        "servicos_agendamento", null, servico);
            }

            db.setTransactionSuccessful();

        } catch (SQLException e) {
            Log.e("BancoDeDados",
                    "Erro ao salvar agendamento.", e);
            idAgendamento = -1;

        } finally {
            if (transacaoIniciada) {
                db.endTransaction();
            }
        }

        return idAgendamento;
    }

    public Cursor buscarAgendamentos(long idUsuario) {
        SQLiteDatabase db = getReadableDatabase();

        String sql =
                "SELECT a.data, a.horario, a.nome_cliente, " +
                        "a.telefone_cliente, " +
                        "(SELECT group_concat(s.nome_servico, ', ') " +
                        "FROM servicos_agendamento s " +
                        "WHERE s.id_agendamento = a.id) AS servicos " +
                        "FROM agendamentos a " +
                        "WHERE a.id_usuario = ? " +
                        "ORDER BY a.data DESC, a.horario DESC";

        return db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario)
        });
    }

    public Cursor buscarUsuario(long idUsuario) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT username, email FROM usuarios WHERE id = ?",
                new String[]{String.valueOf(idUsuario)}
        );
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}