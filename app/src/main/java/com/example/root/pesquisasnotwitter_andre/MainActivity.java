package com.example.root.pesquisasnotwitter_andre;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;

import static java.util.Collections.*;

public class MainActivity extends Activity {

    // nome do arquivo XML de SharedPreferences que armazena as pesquisas salvas
    private static final String SEARCHES = "searches";
    private EditText queryEditText;          // EditText onde o usuário digita uma consulta
    private EditText tagEditText;            // EditText onde o usuário identifica uma consulta
    private SharedPreferences savedSearches; // pesquisas favoritas do usuário
    private ArrayList<String> tags;          // lista de identificadores das pesquisas salvas
    private ArrayAdapter<String> adapter;    // vincula identificadores a ListView
    private ArrayAdapter<String> listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // obtém referências para os EditText
        queryEditText = (EditText) findViewById(R.id.queryEditText);
        tagEditText = (EditText) findViewById(R.id.tagEditText);

        // obtém os SharedPreferences que contêm as pesquisas salvas do usuário
        savedSearches = getSharedPreferences(SEARCHES, MODE_PRIVATE);

        // armazena os identificadores salvos em um ArrayList e, então, os ordena
        tags = new ArrayList<String>(savedSearches.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);

        // cria ArrayAdapter e o utiliza para vincular os identificadores a ListView
        adapter = new ArrayAdapter<String>(this, R.layout.activity_main, tags);
        setListAdapter(adapter);

        // registra receptor para salvar uma pesquisa nova ou editada
        ImageButton saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);

        // registra receptor que pesquisa quando o usuário toca em um identificador
        getListView().setOnItemClickListener(itemClickListener);

        // configura o receptor que permite ao usuário excluir ou editar uma pesquisa
        getListView().setOnItemLongClickListener(itemLongClickListener);

    }

    public OnClickListener saveButtonListener =  new OnClickListener() {
                // add/update search if neither query nor tag is empty
                @Override
                public void onClick(View v) {

                    if (queryEditText.getText().length() > 0 &&
                            tagEditText.getText().length() > 0){

                        addTaggedSearch(queryEditText.getText().toString(),
                                tagEditText.getText().toString());
                        queryEditText.setText(""); // clear queryEditText
                        tagEditText.setText(""); // clear tagEditText

                        ((InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                                tagEditText.getWindowToken(), 0);
                    }else{
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(MainActivity.this);

                        // configura o título da caixa de diálogo e a mensagem a ser exibida
                        builder.setMessage(R.string.missingMessage);
                        // fornece um botão OK que simplesmente remove a caixa de diálogo
                        builder.setPositiveButton(R.string.OK, null);
                        // cria AlertDialog a partir de AlertDialog.Builder
                        AlertDialog errorDialog = builder.create();
                        errorDialog.show();
                    }
                }
            };

// adiciona uma nova pesquisa ao arquivo de salvamento e, então, atualiza todos
// os componentes Button
 private void addTaggedSearch(String query, String tag)
 {

 if (!tags.contains(tag))
            {
            tags.add(tag); // adiciona o novo identificador
             Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            }

    public void setListAdapter(ArrayAdapter<String> listAdapter) {
        this.listAdapter = listAdapter;
    }
}
}
