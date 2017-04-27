package com.example.tylerheers.molebuilderproto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by tylerheers on 4/26/17.
 *
 * Dialog to show molecule information, and
 * links to find out more about the molecule.
 */


public class MoleInfoCard extends DialogFragment
{
    PubChemCompoundDescription compoundDescription;
    boolean valid = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mi = inflater.inflate(R.layout.mole_info_card, null);

        initUIElements(mi);

        builder.setView(mi);
        return builder.create();
    }

    private void initUIElements(View mi)
    {
        TextView moleNameText = (TextView) mi.findViewById(R.id.moleNameInfoCard);
        moleNameText.setText(compoundDescription.title);
        TextView cidText = (TextView) mi.findViewById(R.id.moleCIDInfoCard);
        cidText.setText("CID: " + String.valueOf(compoundDescription.cid));


        LinearLayout recordInfoLayout = (LinearLayout) mi.findViewById(R.id.recordInfoLayout);
        ViewGroup.LayoutParams lp = recordInfoLayout.getLayoutParams();

        for (int i = 0; i < compoundDescription.recordSize(); i++)
        {
            PubChemCompoundDescription.Record r = compoundDescription.get(i);
            insertRecord(recordInfoLayout, r, recordInfoLayout.getWidth() / 3, 300);
            if(i > 3)
                break;
        }

        Button b = (Button) mi.findViewById(R.id.infoCardCloseButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void insertRecord(LinearLayout l, PubChemCompoundDescription.Record record,
                              int width, int height)
    {
        LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = li.inflate(R.layout.molecule_record, l, false);

        TextView description = (TextView) v.findViewById(R.id.descriptioAreaInfoCard);
        TextView source = (TextView) v.findViewById(R.id.sourceAreaInfoCard);
        TextView url = (TextView) v.findViewById(R.id.urlAreaInfoCard);

        prepareTextView(description, record.description, false);
        prepareTextView(source, record.source, true);
        prepareTextView(url, record.url, true);

        l.addView(v);
    }

    private void prepareTextView(TextView tv, String text, boolean setSingleLine)
    {
        tv.setTextSize(18);
        tv.setPadding(22, 22, 22, 22);
        tv.setLineSpacing(2, 1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        if(setSingleLine)
            tv.setSingleLine();

        tv.setText(text);
    }

    public void setCompoundDescription(PubChemCompoundDescription cd)
    {
        if(cd != null) {
            valid = true;
            compoundDescription = cd;
        }
    }
}
