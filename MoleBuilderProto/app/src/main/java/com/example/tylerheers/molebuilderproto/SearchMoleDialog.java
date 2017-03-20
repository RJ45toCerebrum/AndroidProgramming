package com.example.tylerheers.molebuilderproto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tyler on 3/11/2017.
 */

public class SearchMoleDialog extends DialogFragment
       implements IAsyncResult<String>,
       View.OnClickListener
{
    PostRequest requestSDF;
    EditText searchText;
    MoleRenderer2D renderer2D;
    MDLV2000Reader sdfReader;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View searchMoleView = inflater.inflate(R.layout.search_mole_dialog, null);
        searchMoleView.setVisibility(View.VISIBLE);

        searchText = (EditText) searchMoleView.findViewById(R.id.searchMoleEditText);
        Button searchButton = (Button) searchMoleView.findViewById(R.id.searchMoleButton);
        Button cancelButton = (Button) searchMoleView.findViewById(R.id.cancelSearchButton);

        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dismiss();
            }
        });
        searchButton.setOnClickListener(this);

        builder.setView(searchMoleView);
        return builder.create();
    }

    public void onClick(View v)
    {
        requestSDF = new PostRequest(this);
        if(searchText.length() > 0)
        {
            String moleculeName = searchText.getText().toString();
            String url = String.format("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/%s/SDF", moleculeName);
            requestSDF.execute(url);
        }
    }

    @Override
    public void onPostExecute(String results)
    {
        //Log.d("SDF results", results);
        if(renderer2D != null)
        {
            InputStream inputStream = new ByteArrayInputStream(results.getBytes());
            try
            {
                sdfReader = new MDLV2000Reader(inputStream);
                IAtomContainer container = new AtomContainer();
                container = sdfReader.read(container);
                if(container != null){
                    Log.d("Atom Count", String.valueOf(container.getAtomCount()));
                    renderer2D.addMolecule(container);
                }
            } catch (CDKException ex) {
                Log.d("Error Occurred", ex.getMessage());
            }
            finally
            {
                try {
                    sdfReader.close();
                }
                catch (IOException ex){
                    Log.d("Error in IO", ex.getMessage());
                }
            }

        }

        dismiss();
    }

    public void setRenderer2D(MoleRenderer2D renderer2D){
        this.renderer2D = renderer2D;
    }
}
