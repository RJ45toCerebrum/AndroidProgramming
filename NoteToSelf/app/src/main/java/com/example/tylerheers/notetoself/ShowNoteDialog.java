package com.example.tylerheers.notetoself;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by tylerheers on 2/15/17.
 */

public class ShowNoteDialog extends DialogFragment
{
    private Note noteToShow;

    ImageView importantImageView;
    ImageView ideaImageView;
    ImageView todoImageView;
    TextView noteBody;
    TextView noteTitle;
    Button doneReadButton;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // you must inflate to java object before getting references to things
        // in the layout
        View showNoteDiag = inflater.inflate(R.layout.show_note_dialog, null);

        importantImageView = (ImageView)showNoteDiag.findViewById(R.id.importantImageView);
        todoImageView = (ImageView)showNoteDiag.findViewById(R.id.todoImageView);
        ideaImageView = (ImageView)showNoteDiag.findViewById(R.id.ideaImageView);
        noteBody = (TextView)showNoteDiag.findViewById(R.id.noteBodyTextView);
        noteTitle = (TextView) showNoteDiag.findViewById(R.id.noteTitle);
        doneReadButton = (Button) showNoteDiag.findViewById(R.id.noteDoneReadButton);

        noteTitle.setText(noteToShow.getTitle());
        noteBody.setText(noteToShow.getDescription());

        if(!noteToShow.isImportant()){
            importantImageView.setVisibility(View.INVISIBLE);
        }
        if (!noteToShow.isIdea()){
            ideaImageView.setVisibility(View.INVISIBLE);
        }
        if(!noteToShow.isTodo()){
            todoImageView.setVisibility(View.INVISIBLE);
        }

        doneReadButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dismiss();
            }
        });

        builder.setView(showNoteDiag).setMessage("Your Note");

        return builder.create();
    }

    public void noteSelected(Note noteSelected){
        noteToShow = noteSelected;
    }

}
