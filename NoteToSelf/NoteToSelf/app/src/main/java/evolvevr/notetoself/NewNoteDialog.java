package evolvevr.notetoself;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by Tyler on 2/15/2017.
 */
// page 292
public class NewNoteDialog extends DialogFragment
{
    EditText titleEditText;
    EditText noteDescriptionEditText;
    CheckBox ideaCheckBox;
    CheckBox todoCheckBox;
    CheckBox importantCheckBox;
    Button cancelButton;
    Button createButton;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.new_note_dialog, null);

        titleEditText = (EditText)dialogView.findViewById(R.id.noteTitle);
        noteDescriptionEditText = (EditText)dialogView.findViewById(R.id.noteDescription);
        ideaCheckBox = (CheckBox)dialogView.findViewById(R.id.noteIdeaCheckBox);
        todoCheckBox = (CheckBox)dialogView.findViewById(R.id.noteTodoCheckBox);
        importantCheckBox = (CheckBox)dialogView.findViewById(R.id.noteImportantCheckBox);
        cancelButton = (Button)dialogView.findViewById(R.id.noteCancelButton);
        createButton = (Button) dialogView.findViewById(R.id.noteCreateButton);

        builder.setView(dialogView).setMessage("Add new Note");

        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dismiss();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Note newNote = new Note();
                newNote.setTitle(titleEditText.getText().toString());
                newNote.setDescription(noteDescriptionEditText.getText().toString());
                newNote.setIdea(ideaCheckBox.isChecked());
                newNote.setImportant(importantCheckBox.isChecked());
                newNote.setTodo(todoCheckBox.isChecked());

                MainActivity callingActivity = (MainActivity) getActivity();
                callingActivity.createNewNote(newNote);

                dismiss();
            }
        });

        return builder.create();
    }
}
