package com.example.tylerheers.notetoself;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

    // This class is an Adapter that connects
    // to our ListView in This activity and provides
    // easy update to list info in the listview
    public class NoteAdapter extends BaseAdapter {

        JsonSerializer serializer;
        ArrayList<Note> notes = new ArrayList<>();

        // This will create new serializer and attempt to load data
        // if attempt fail make new list. No notes
        public NoteAdapter()
        {
            serializer = new JsonSerializer("NoteToSelf.json", getApplicationContext());

            try {
                notes = serializer.load();
            }
            catch (Exception e){
                Log.i("Error Message", e.getMessage());
                notes = new ArrayList<>();
            }
        }

        @Override
        public int getCount() {
            return notes.size();
        }


        @Override
        public View getView(int item, View v, ViewGroup g) {

            if (v == null) {
                LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.note_item, g, false);
            }

            ImageView importantImage = (ImageView) v.findViewById(R.id.importantImageListItem);
            ImageView ideaImage = (ImageView) v.findViewById(R.id.ideaImageListItem);
            ImageView todoImage = (ImageView) v.findViewById(R.id.todoImageListItem);
            TextView title = (TextView) v.findViewById(R.id.noteTitleListItem);
            TextView description = (TextView) v.findViewById(R.id.noteDescriptionListItem);

            Note showNote = notes.get(item);
            if (!showNote.isImportant()) {
                importantImage.setVisibility(View.INVISIBLE);
            }
            if (!showNote.isIdea()) {
                ideaImage.setVisibility(View.INVISIBLE);
            }
            if (!showNote.isTodo()) {
                todoImage.setVisibility(View.INVISIBLE);
            }

            title.setText(showNote.getTitle());
            description.setText(showNote.getDescription());


            return v;
        }

        @Override
        public Note getItem(int item) {
            return notes.get(item);
        }

        @Override
        public long getItemId(int id) {
            return id;
        }

        public void addNote(Note note) {
            if (note != null) {
                notes.add(note);
                this.notifyDataSetChanged();
            }
        }

        // save the notes by calling serializers save method
        // returns true on successful save
        public boolean saveNotes()
        {
            boolean isSuccess = true;
            try  {
                serializer.save(notes);
            }
            catch(Exception e) {
                Log.e("Error Saving Notes","", e);
                isSuccess = false;
            }

            return isSuccess;
        }
    }


    private ImageButton addNoteButton;
    private ImageButton settingsButton;
    private ListView noteView;
    private NoteAdapter adapter;

    SharedPreferences prefs;
    boolean soundFX = false;
    int animSpeed = SettingsActivity.MEDIUM_ANIM_SPEED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNoteButton = (ImageButton) findViewById(R.id.addNoteButton);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        noteView = (ListView) findViewById(R.id.noteListView);
        adapter = new NoteAdapter();

        noteView.setAdapter(adapter);

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewNoteDialog diag = new NewNoteDialog();
                diag.show(getFragmentManager(), null);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        noteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> view, View v, int item, long id) {
                Note n = adapter.getItem(item);
                ShowNoteDialog dialog = new ShowNoteDialog();
                dialog.noteSelected(n);
                dialog.show(getFragmentManager(), "123");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Toast.makeText(this, "Starting", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume(){
        super.onResume();

        prefs = getSharedPreferences("Note To Self", MODE_PRIVATE);
        soundFX = prefs.getBoolean(SettingsActivity.SOUND_FX_SETTING, soundFX);
        animSpeed = prefs.getInt(SettingsActivity.ANIM_SPEED_SETTING, animSpeed);
    }

    @Override
    protected void onPause(){
        super.onPause();

        adapter.saveNotes();
        Toast.makeText(this, "Pausing", Toast.LENGTH_SHORT).show();
    }

    public void createNewNote(Note newNote) {
        // Del later
        Toast.makeText(this, newNote.getTitle(), Toast.LENGTH_SHORT).show();
        adapter.addNote(newNote);
    }
}
