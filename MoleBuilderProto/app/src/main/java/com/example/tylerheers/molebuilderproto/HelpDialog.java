package com.example.tylerheers.molebuilderproto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// Just to show the help dialog
public class HelpDialog extends DialogFragment
{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View hd = inflater.inflate(R.layout.helpmenu_dialog, null);

        Button closebutton = (Button)hd.findViewById(R.id.closeHelpDialog);
        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        initWebView(hd);
        builder.setView(hd);
        return builder.create();
    }

    private void initWebView(View view)
    {
        TextView tv = (TextView) view.findViewById(R.id.video_demon);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
//        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
//        startActivity(browserIntent);
    }
}
