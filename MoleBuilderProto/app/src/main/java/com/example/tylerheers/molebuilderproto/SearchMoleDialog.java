package com.example.tylerheers.molebuilderproto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.tylerheers.molebuilderproto.renderers.MoleRenderer2D;

import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Created by Tyler on 3/11/2017.
 *
 * Class for searching molecules from pubchem
 */

public class SearchMoleDialog extends DialogFragment
       implements IAsyncSignals<Float, String>,
       View.OnClickListener
{
    PostRequest requestSDF;
    EditText searchText;
    RadioGroup radioGroup;
    MoleRenderer2D renderer2D;
    View searchMoleView;
    ProgressBar progressBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        searchMoleView = inflater.inflate(R.layout.search_mole_dialog, null);
        searchMoleView.setVisibility(View.VISIBLE);

        searchText = (EditText) searchMoleView.findViewById(R.id.searchMoleEditText);
        searchText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});

        Button searchButton = (Button) searchMoleView.findViewById(R.id.searchMoleButton);
        Button cancelButton = (Button) searchMoleView.findViewById(R.id.cancelSearchButton);

        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dismiss();
            }
        });
        searchButton.setOnClickListener(this);

        radioGroup = (RadioGroup)searchMoleView.findViewById(R.id.moleSearch_RadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if(checkedId == R.id.cid_moleSearch)
                    searchText.setInputType(InputType.TYPE_CLASS_NUMBER);
                else
                    searchText.setInputType(InputType.TYPE_CLASS_TEXT);
            }
        });
        radioGroup.check(R.id.name_moleSearch);

        builder.setView(searchMoleView);
        return builder.create();
    }

    public void onClick(View v)
    {
        requestSDF = new PostRequest(this);
        if(searchText.length() > 0)
        {
            String moleculeName = searchText.getText().toString();
            int id = radioGroup.getCheckedRadioButtonId();
            String url;
            if(id == R.id.name_moleSearch)
                url = String.format("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/%s/SDF", moleculeName);
            else if(id == R.id.cid_moleSearch)
                url = String.format("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/%s/SDF", moleculeName);
            else
                url = String.format("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/%s/SDF", moleculeName);

            requestSDF.execute(url);
            progressBar = (ProgressBar) searchMoleView.findViewById(R.id.moleSearchProgressBar);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPostExecute(String results)
    {
        if(results == null) {
            Toast.makeText(getActivity().getBaseContext(), "Could not find molecule", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if(renderer2D != null && results.length() > 0)
        {
            IAtomContainer con = SdfConverter.convertSDFString(results);
            if(con != null)
            {
                if(con.getAtomCount() >= SceneContainer.maxAtomsForMolecule)
                {
                    Toast.makeText(getActivity().getBaseContext(), "Unable to build Molecules with over 60 atoms",
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                Molecule mole = Molecule.convertAtomContainer(con);
                SceneContainer.getInstance().putMolecule(mole);
                if(mole == null)
                {
                    Toast.makeText(getActivity().getBaseContext(), "Sorry! Something went wrong molecule building",
                            Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }

                else
                    renderer2D.addMolecule(mole);
            }
        }

        dismiss();
    }


    public void onBackgroundUpdate(Float data)
    {
        if (data != null)
            Log.i("Download Progress", data.toString());
    }


    public void setRenderer2D(MoleRenderer2D renderer2D){
        this.renderer2D = renderer2D;
    }
}
