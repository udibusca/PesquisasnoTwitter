package com.example.root.pesquisasnotwitter_andre;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.View.OnClickListener;
import android.widget.TextView;

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

    private final OnClickListener saveButtonListener = new OnClickListener() {
                // add/update search if neither query nor tag is empty
                @Override
                public void onClick(View view) {
                    String query = queryEditText.getText().toString();
                    String tag = tagEditText.getText().toString();

                    if (!query.isEmpty() && !tag.isEmpty()) {
                        // hide the virtual keyboard
                        ((InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                                view.getWindowToken(), 0);

                        addTaggedSearch(tag, query); // add/update the search
                        queryEditText.setText(""); // clear queryEditText
                        tagEditText.setText(""); // clear tagEditText
                        queryEditText.requestFocus(); // queryEditText gets focus
                    }
                }
            };

// adiciona uma nova pesquisa ao arquivo de salvamento e, então, atualiza todos
// os componentes Button
    private void addTaggedSearch(String tag, String query) {
        // obter um SharedPreferences.Editor para armazenar um novo par de tags / consultas

        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query); // store current search
        preferencesEditor.apply(); // store the updated preferences

        // se a tag for nova, adicionar e classificar tags, exibir lista atualizada
        if (!tags.contains(tag)) {
            tags.add(tag); // add new tag
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged(); // atualiza tags no RecyclerView
        }
      }

    // itemClickListener ativa o navegador Web para exibir resultados da busca
       OnItemClickListener itemClickListener = new OnItemClickListener(){
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id)
         {
             // obtém a string de consulta e cria uma URL representando a busca
                String tag = ((TextView) view).getText().toString();
                String urlString = getString(R.string.searchURL) +
                        Uri.encode(savedSearches.getString(tag, ""), "UTF-8");
             // cria um objeto Intent para ativar um navegador Web
             Intent webIntent = new Intent(Intent.ACTION_VIEW,
                     Uri.parse(urlString));
             startActivity(webIntent); // ativa o navegador Web para ver os resultados

         }
       };// fim da declaração de itemClickListener
}
