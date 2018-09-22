package com.example.root.pesquisasnotwitter_andre;


// MainActivity.java
// Gerencia suas pesquisas favoritas no Twitter para
// facilitar o acesso e exibir no navegador Web do dispositivo
import java.util.Collections;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends ListActivity {

    // nome do arquivo XML de SharedPreferences que armazena as pesquisas salvas
    private static final String SEARCHES = "searches";

    private EditText queryEditText;          // EditText onde o usuário digita uma consulta
    private EditText tagEditText;            // EditText onde o usuário identifica uma consulta
    private SharedPreferences savedSearches; // pesquisas favoritas do usuário
    private ArrayList<String> tags;          // lista de identificadores das pesquisas salvas
    private ArrayAdapter<String> adapter;    // vincula identificadores a ListView


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
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, tags);
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
                                INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
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

 // itemLongClickListener exibe uma caixa de diálogo que permite ao usuário excluir
         // ou editar uma pesquisa salva
         OnItemLongClickListener itemLongClickListener =
             new OnItemLongClickListener()
 {
         @Override
         public boolean onItemLongClick(AdapterView<?> parent, View view,
                 int position, long id)
         {
             // obtém o identificador em que o usuário fez o pressionamento longo
             final String tag = ((TextView) view).getText().toString();

             // cria um novo componente AlertDialog
             AlertDialog.Builder builder =  new AlertDialog.Builder(MainActivity.this);

             // configura o título do componente AlertDialog
             builder.setTitle(getString(R.string.shareEditDeleteTitle, tag));

             // define a lista de itens a exibir na caixa de diálogo
             builder.setItems(R.array.dialog_items,new DialogInterface.OnClickListener() {
                 // responde ao toque do usuário, compartilhando, editando ou
                 // excluindo uma pesquisa salva
                 @Override
                 public void onClick(DialogInterface dialog, int which)
                 {
                     switch (which)
                     {
                         case 0: // compartilha
                             shareSearch(tag);
                             break;
                             case 1: // edita
                               // configura componentes EditText para corresponder ao
                                // identificador e à consulta escolhidos
                             tagEditText.setText(tag);
                             queryEditText.setText(
                                     savedSearches.getString(tag, ""));
                             break;
                         case 2: // exclui
                             deleteSearch(tag);
                             break;
                         }
                     }
                 } // fim de DialogInterface.OnClickListener
             );    // fim da chamada a builder.setItems

             // configura o componente Button negativo de AlertDialog
             builder.setNegativeButton(getString(R.string.cancel),
                     new DialogInterface.OnClickListener()
             {
                 // chamado quando o componente Button “Cancel” é clicado
                 public void onClick(DialogInterface dialog, int id)
                 {
                     dialog.cancel(); // remove o componente AlertDialog
                 }
             }
             ); // fim da chamada a setNegativeButton

             builder.create().show(); // exibe o componente AlertDialog
             return true;
             } // fim do método onItemLongClick
         }; // fim da declaração de OnItemLongClickListener

    // permite escolher um aplicativo para compartilhar a URL de uma pesquisa salva
    private void shareSearch(String tag) {
        // cria a URL que representa a pesquisa
        String urlString = getString(R.string.searchURL) + Uri.encode(savedSearches.getString(tag, ""), "UTF-8");

        // cria um objeto Intent para compartilhar urlString
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.shareSubject));
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.shareMessage, urlString));
        shareIntent.setType("text/plain");

        // display apps that can share plain text
        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareSearch)));
    }

     // exclui uma pesquisa depois que o usuário confirma a operação de exclusão
     private void deleteSearch(final String tag) {
          // cria um novo componente AlertDialog
          AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);

          // configura a mensagem do componente AlertDialog
          confirmBuilder.setMessage(
                  getString(R.string.confirmMessage, tag));

          // configura o elemento Button negativo do componente AlertDialog
          confirmBuilder.setNegativeButton(getString(R.string.cancel),
                  new DialogInterface.OnClickListener()
          {
              // chamado quando o componente Button “Cancel” é clicado
              public void onClick(DialogInterface dialog, int id)
              {
                  dialog.cancel(); // remove a caixa de diálogo
                  }
              }
          ); // fim da chamada a setNegativeButton

           // configura o elemento Button positivo do componente AlertDialog
           confirmBuilder.setPositiveButton(getString(R.string.delete),
                   new DialogInterface.OnClickListener()
           {
               // chamado quando o componente Button “Cancel” é clicado
               public void onClick(DialogInterface dialog, int id)
               {
                   tags.remove(tag); // remove o identificador de tags

                   // obtém o SharedPreferences.Editor para remover pesquisa salva
                   SharedPreferences.Editor preferencesEditor =
                           savedSearches.edit();
                   preferencesEditor.remove(tag); // remove a pesquisa
                   preferencesEditor.apply(); // salva as alterações

                   // vincula novamente o ArrayList de identificadores a ListView para
                   // mostrar a lista atualizada
                    adapter.notifyDataSetChanged();
               }
           } // fim de OnClickListener
       );    // fim da chamada a setPositiveButton

          confirmBuilder.create().show(); // exibe o componente AlertDialog
     } // fim do método deleteSearch
 } // fim da classe MainActivity
