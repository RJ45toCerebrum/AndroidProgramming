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
import android.widget.RadioGroup;
import android.widget.Toast;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Element;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.Bond;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by Tyler on 3/11/2017.
 */

public class SearchMoleDialog extends DialogFragment
       implements IAsyncResult<String>,
       View.OnClickListener
{
    PostRequest requestSDF;
    EditText searchText;
    RadioGroup radioGroup;
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
        }
    }

    @Override
    public void onPostExecute(String results)
    {
        if(results == null) {
            Toast.makeText(getActivity().getBaseContext(), "Could not find molecule", Toast.LENGTH_LONG).show();
            return;
        }

        if(renderer2D != null && results.length() > 0)
        {
            InputStream inputStream = new ByteArrayInputStream(results.getBytes());
            try
            {
                sdfReader = new MDLV2000Reader(inputStream);
                IAtomContainer container = new AtomContainer();
                container = sdfReader.read(container);
                if(container != null)
                {
                    Molecule mole = convertAtomContainer(container);
                    renderer2D.addMolecule(mole);
                }
            } catch (CDKException ex) {
                Log.d("Error Occurred", ex.getMessage());
            }
            catch (Exception ex){
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

    private Molecule convertAtomContainer(IAtomContainer container)
    {
        HashMap<IBond, MoleculeAtom[]> bondAtoms = new HashMap<>();
        Molecule mole = new Molecule();

        /*
            they built it using IAtoms instead of MoleculeAtom
            which is what is needed;
            In order to perform the conversion:
            1) for each of the atoms, we make a new MoleculeAtom which is passed into constructor
            2) for each of the bonds connect to that atom:
                a) place it into a hashmap with key as bond. Why? because when i go to the next atom
                    then i can look up whether its already been added. I need to do this because
                    a bond is always connected to more than one atom.
                b) if no entry for the bond, then the algo has not seen it yet --> add it and put the
                    current MoleculeAtom into the first place of the MoleAtom[] of hashmap
                c) if there is an entry then this means we are at a bond but from the perspective of
                        a diff atom. Add atom into the second place of the MoleculeAtom[]

         */
        int numCreatedAtoms = renderer2D.getNumCreatedAtoms() + 1;
        int numCreatedBonds = renderer2D.getNumCreatedBonds() + 1;
        for (int i = 0; i < container.getAtomCount(); i++)
        {
            IAtom a = container.getAtom(i);
            Elements e = MoleculeAtom.getElement(a);
            MoleculeAtom ma = new MoleculeAtom(e, a);
            ma.setID("atom"+ String.valueOf(numCreatedAtoms + i));
            mole.addAtom(ma);
            ma.setMolecule(mole);

            for (IBond b: container.getConnectedBondsList(a))
            {
                ma.addBond(b.getOrder());
                if(bondAtoms.containsKey(b)) {
                    bondAtoms.get(b)[1] = ma;
                }
                else {
                    MoleculeAtom[] conAtoms = new MoleculeAtom[2];
                    conAtoms[0] = ma;
                    bondAtoms.put(b, conAtoms);
                }
            }
        }

        for (IBond b: bondAtoms.keySet()) {
            b.setAtoms(bondAtoms.get(b));
            mole.addBond(b);
            b.setID("bond"+String.valueOf(numCreatedBonds));
            numCreatedBonds++;
        }

        return  mole;
    }

    public void setRenderer2D(MoleRenderer2D renderer2D){
        this.renderer2D = renderer2D;
    }
}
