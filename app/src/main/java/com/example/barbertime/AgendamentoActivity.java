package com.example.barbertime;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AgendamentoActivity extends AppCompatActivity {

    private final String[] todosHorarios = {
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
            "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
            "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
            "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00"
    };

    private TextView txtData;
    private Spinner spHorario;
    private EditText txtNome, txtTelefone;
    private CheckBox chkCorte, chkBarba, chkLavagem, chkPintura;
    private Button btFinalizar;

    private BancoDeDados banco;
    private String dataSelecionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamento);

        View raiz = findViewById(R.id.agendamento);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            int inferior = Math.max(insets.getInsets(WindowInsetsCompat.Type.ime()).bottom,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), inferior);
            return insets;
        });

        inicializarComponentes();
        banco = new BancoDeDados(this);
        configurarHorarioInicial();
        configurarEventos();
    }

    private void inicializarComponentes() {
        txtData = findViewById(R.id.txtDataAgendamento);
        spHorario = findViewById(R.id.spHorarioAgendamento);
        txtNome = findViewById(R.id.txtNomeAgendamento);
        txtTelefone = findViewById(R.id.txtTelefoneAgendamento);
        chkCorte = findViewById(R.id.chkCorte);
        chkBarba = findViewById(R.id.chkBarba);
        chkLavagem = findViewById(R.id.chkLavagem);
        chkPintura = findViewById(R.id.chkPintura);
        btFinalizar = findViewById(R.id.btFinalizarAgendamento);
    }

    private void configurarHorarioInicial() {
        List<String> horarios = new ArrayList<>();
        horarios.add("Selecione uma data primeiro");
        atualizarSpinner(horarios);
    }

    private void configurarEventos() {
        txtData.setOnClickListener(v -> abrirCalendario());
        btFinalizar.setOnClickListener(v -> validarAgendamento());
    }

    private void abrirCalendario() {
        Calendar hoje = Calendar.getInstance();

        DatePickerDialog seletor = new DatePickerDialog(this, (view, ano, mes, dia) -> {
            Calendar data = Calendar.getInstance();
            data.set(ano, mes, dia, 0, 0, 0);
            data.set(Calendar.MILLISECOND, 0);

            if (data.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Toast.makeText(this, "Não há atendimento aos domingos.", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar dataMinima = zerarHora(Calendar.getInstance());
            if (data.before(dataMinima)) {
                Toast.makeText(this, "Selecione uma data (ATUAL ou FUTURA)", Toast.LENGTH_SHORT).show();
                return;
            }

            dataSelecionada = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(data.getTime());
            txtData.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(data.getTime()));
            carregarHorarios();
        }, hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH));

        seletor.getDatePicker().setMinDate(zerarHora(Calendar.getInstance()).getTimeInMillis());
        seletor.show();
    }

    private Calendar zerarHora(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private void carregarHorarios() {
        List<String> horariosOcupados = banco.buscarHorariosOcupados(dataSelecionada);
        List<String> horariosDisponiveis = new ArrayList<>();

        Calendar agora = Calendar.getInstance();
        Calendar dataEscolhida = Calendar.getInstance();
        dataEscolhida.setTime(java.sql.Date.valueOf(dataSelecionada));

        boolean ehHoje = dataEscolhida.get(Calendar.YEAR) == agora.get(Calendar.YEAR)
                && dataEscolhida.get(Calendar.DAY_OF_YEAR) == agora.get(Calendar.DAY_OF_YEAR);

        int minutosAtuais = agora.get(Calendar.HOUR_OF_DAY) * 60 + agora.get(Calendar.MINUTE);

        for (String horario : todosHorarios) {
            if (horariosOcupados.contains(horario)) continue;

            if (ehHoje) {
                String[] partes = horario.split(":");
                int minutosHorario = Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
                if (minutosHorario <= minutosAtuais) continue;
            }

            horariosDisponiveis.add(horario);
        }

        if (horariosDisponiveis.isEmpty()) {
            Toast.makeText(this, "Não há horários disponíveis para essa data", Toast.LENGTH_LONG).show();
        }

        atualizarSpinner(horariosDisponiveis);
    }

    private void atualizarSpinner(List<String> itens) {
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itens);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHorario.setAdapter(adaptador);
    }

    private void validarAgendamento() {
        String nome = txtNome.getText().toString().trim();
        String telefone = txtTelefone.getText().toString().trim();

        if (nome.isEmpty()) { txtNome.setError("Informe seu NOME"); txtNome.requestFocus(); return; }
        if (telefone.isEmpty()) { txtTelefone.setError("Informe seu TELEFONE"); txtTelefone.requestFocus(); return; }

        if (telefone.replaceAll("\\D", "").length() < 10) {
            txtTelefone.setError("Informe um telefone válido");
            txtTelefone.requestFocus();
            return;
        }

        if (dataSelecionada.isEmpty()) {
            Toast.makeText(this, "Selecione uma data", Toast.LENGTH_SHORT).show();
            return;
        }

        Object itemSelecionado = spHorario.getSelectedItem();
        if (itemSelecionado == null || !itemSelecionado.toString().matches("\\d{2}:\\d{2}")) {
            Toast.makeText(this, "Escolha um horário válido", Toast.LENGTH_SHORT).show();
            return;
        }

        montarResumo(nome, telefone, itemSelecionado.toString());
    }

    private void montarResumo(String nome, String telefone, String horario) {
        List<String> nomesServicos = new ArrayList<>();
        List<Double> precosServicos = new ArrayList<>();

        if (chkCorte.isChecked()) { nomesServicos.add("Corte"); precosServicos.add(35.0); }
        if (chkBarba.isChecked()) { nomesServicos.add("Barba"); precosServicos.add(30.0); }
        if (chkLavagem.isChecked()) { nomesServicos.add("Lavagem e cuidados"); precosServicos.add(20.0); }
        if (chkPintura.isChecked()) { nomesServicos.add("Pintura"); precosServicos.add(100.0); }

        if (nomesServicos.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um serviço", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = 0;
        StringBuilder listaServicos = new StringBuilder();
        Locale ptBr = new Locale("pt", "BR");

        for (int i = 0; i < nomesServicos.size(); i++) {
            double preco = precosServicos.get(i);
            total += preco;
            listaServicos.append(nomesServicos.get(i)).append(" - ").append(String.format(ptBr, "R$ %.2f", preco));
            if (i < nomesServicos.size() - 1) listaServicos.append("\n");
        }

        String resumo = "Cliente: " + nome + "\nTelefone: " + telefone
                + "\nData: " + txtData.getText() + "\nHorário: " + horario
                + "\n\nServiços:\n\n" + listaServicos + "\n\nTotal: " + String.format(ptBr, "R$ %.2f", total);

        exibirConfirmacao(nome, telefone, horario, total, nomesServicos, precosServicos, resumo);
    }

    private void exibirConfirmacao(String nome, String telefone, String horario, double total,
                                   List<String> nomesServicos, List<Double> precosServicos, String resumo) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar agendamento")
                .setMessage(resumo)
                .setNegativeButton("Voltar", (d, w) -> d.dismiss())
                .setPositiveButton("Confirmar", (d, w) -> salvarAgendamento(nome, telefone, horario, total, nomesServicos, precosServicos))
                .show();
    }

    private void salvarAgendamento(String nome, String telefone, String horario, double total,
                                   List<String> nomesServicos, List<Double> precosServicos) {
        SharedPreferences prefs = getSharedPreferences("BarberTimePrefs", MODE_PRIVATE);
        long idUsuario = prefs.getLong("id_usuario", -1L);

        if (idUsuario <= 0) {
            Toast.makeText(this, "Faça login novamente para agendar", Toast.LENGTH_LONG).show();
            return;
        }

        long resultado = -1;
        try {
            resultado = banco.salvarAgendamento((int) idUsuario, nome, telefone, dataSelecionada,
                    horario, total, nomesServicos, precosServicos);
        } catch (Exception e) {
            resultado = -1;
        }

        if (resultado <= 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Agendamento não realizado")
                    .setMessage(
                            "Não foi possível salvar o agendamento. "
                                    + "O horário pode ter sido ocupado "
                                    + "por outra reserva. Consulte os "
                                    + "horários disponíveis e tente novamente."
                    )
                    .setPositiveButton(
                            "OK",
                            (dialog, which) -> carregarHorarios()
                    )
                    .show();

            return;
        }

        String mensagem = "Seu agendamento foi salvo com sucesso! :)\n\nData: " + txtData.getText()
                + "\nHorário: " + horario + "\nTotal: " + String.format(new Locale("pt", "BR"), "R$ %.2f", total);

        AlertDialog sucessoDialog = new AlertDialog.Builder(this)
                .setTitle("Agendamento confirmado!")
                .setMessage(mensagem)
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> finish())
                .create();

        sucessoDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (banco != null) banco.close();
        super.onDestroy();
    }
}