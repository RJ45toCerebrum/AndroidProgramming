package com.example.tylerheers.biojavatut;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

// BioJava
import org.biojava.bio.alignment.Alignment;
import org.biojava.bio.alignment.AlignmentPair;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.compound.AmbiguityDNACompoundSet;
import org.biojava3.core.sequence.transcription.TranscriptionEngine;

public class MainActivity extends AppCompatActivity {

    TextView dnaSeqView;
    TextView dnaSeqComplement;
    TextView rnaSeq;
    EditText seqEntry;
    ImageButton compareButton;

    String allowedChars = "atcgATCG";
    InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if(source != null && !allowedChars.contains("" + source)){
                return "";
            }
            return null;
        }
    };


    DNASequence currentSeq;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seqEntry = (EditText)findViewById(R.id.seqEntry);
        seqEntry.setFilters(new InputFilter[]{filter, new InputFilter.AllCaps()});

        compareButton = (ImageButton) findViewById(R.id.compareButton);
        dnaSeqView = (TextView)findViewById(R.id.dnaSeq);
        dnaSeqComplement = (TextView)findViewById(R.id.dnaSeqComplement);
        rnaSeq = (TextView) findViewById(R.id.rnaSeq);

        AmbiguityDNACompoundSet ambiguityDNACompoundSet = AmbiguityDNACompoundSet.getDNACompoundSet();
        DNASequence dna = new DNASequence("WWW",ambiguityDNACompoundSet);

        compareButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(seqEntry.length() == 0){
                    return;
                }

                String currentSequence = seqEntry.getText().toString();
                currentSeq = new DNASequence(currentSequence);
                compareSeq();
            }
        });
    }


    public void compareSeq(){
        dnaSeqView.setText(currentSeq.toString());
        dnaSeqComplement.setText(currentSeq.getComplement().getSequenceAsString());
        rnaSeq.setText(currentSeq.getRNASequence().toString());
    }
}
