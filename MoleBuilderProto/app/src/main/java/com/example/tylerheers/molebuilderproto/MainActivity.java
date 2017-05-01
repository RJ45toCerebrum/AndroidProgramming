package com.example.tylerheers.molebuilderproto;

// After school ends fixes
//TODO: 3. select molecule via select all atoms
//TODO: Fix Bond distances and add colors and better text of the atoms in 2D-renderer; make more visually pleasing when zooming in/ out
//TODO: render atoms in 3D renderer via first selected is 0 point
//TODO: Fix Bond distances and add colors and better text of the atoms in 2D-renderer; make more visually pleasing when zooming in/ out
//TODO: Make tool bar on the toolbar slide-able instead it it always being there
//TODO: Undo actions --> Undoable actions include: 1) adding atoms/molecules, 2) removing atoms/molecules, 3) adding bonds
//TODO: More atom information in the 3D renderer such as: Dipole, Charge or each atoms, atom names, and electron cloud surface

import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

//TODO: make the selection tool better via; drag box selection

public class MainActivity extends AppCompatActivity
                          implements IAsyncResult<String>,
                                     SceneContainer.SceneChangeListener
{
    enum Mode
    {
        AddAtom, Selection, PanZoom
    }

    public static int maxAtoms = 30;
    public static int maxSelectedAtoms = 10;

    Mode currentMode = Mode.AddAtom;
    View selectedButton = null;

    SceneContainer sceneContainer;
    private TextView sceneText;

    private RelativeLayout canvasLayout;
    private LinearLayout toolbarLayout;
    private int toolBarWidth;
    private SurfaceView surfView;
    private MoleRenderer2D moleRenderer;
    private MoleRenderer3D moleRenderer3D;
    private List<View> modeButtons;
    private SearchMoleDialog diag;

    private ProgressBar convertMoleProgress;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sceneContainer = SceneContainer.getInstance();
        sceneContainer.addSceneChangeListener(this);

        canvasLayout = (RelativeLayout) findViewById(R.id.canvasLayout);
        toolbarLayout = (LinearLayout) findViewById(R.id.toolBarLayout);
        toolBarWidth = toolbarLayout.getLayoutParams().width;

        ImageButton searchMoleButton = (ImageButton) findViewById(R.id.startMoleSearch);
        searchMoleButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                currentMode = Mode.Selection;
                updateModeButtonColors();
                diag = new SearchMoleDialog();
                diag.setRenderer2D(moleRenderer);
                diag.show(getFragmentManager(), "123");
            }
        });

        ImageButton helpDiagButton = (ImageButton) findViewById(R.id.helpButton);
        helpDiagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialog helpDialog = new HelpDialog();
                helpDialog.show(getFragmentManager(), "123");
            }
        });

        initRenderer2D();
        initToolbarButtons();
        updateModeButtonColors();
        // Test Code to delete
        initInfoCardButton();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sceneText = (TextView) findViewById(R.id.sceneInfoTextView);
            updateSceneText();
        }
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            sceneText = (TextView)findViewById(R.id.sceneInfoTextView);
        }
    }

    // init. the buttons in the toolbar
    private void initToolbarButtons()
    {
        modeButtons = new ArrayList<>(12);

        LinearLayout atomButtonLayout = (LinearLayout)findViewById(R.id.atomScrollViewLayout);
//        String[] elementsArray = getResources().getStringArray(R.array.elements);
//
//        for(String e : elementsArray)
//        {
//            AtomButton button = null;
//            switch (e)
//            {
//                case "hydrogen":
//                    button = new AtomButton(this);
//                    button.setElement(Elements.Hydrogen);
//                    button.setText(Elements.HYDROGEN.getSymbol());
//                    break;
//                case "carbon":
//                    button = new AtomButton(this);
//                    button.setElement(Elements.Carbon);
//                    button.setText(Elements.CARBON.getSymbol());
//                    break;
//                case "nitrogen":
//                    button = new AtomButton(this);
//                    button.setElement(Elements.Nitrogen);
//                    button.setText(Elements.NITROGEN.getSymbol());
//                    break;
//                case "oxygen":
//                    button = new AtomButton(this);
//                    button.setElement(Elements.Oxygen);
//                    button.setText(Elements.OXYGEN.getSymbol());
//                    break;
//                case "phosphorus":
//                    button = new AtomButton(this);
//                    button.setElement(Elements.Phosphorus);
//                    button.setText(Elements.PHOSPHORUS.getSymbol());
//                    break;
//                case "sulfur":
//                    button = new AtomButton(this);
//                    button.setElement(Elements.Sulfur);
//                    button.setText(Elements.SULFUR.getSymbol());
//                    break;
//            }
//
//            if(button == null)
//                continue;
//
//            button.setLayoutParams(atomButtonLayout.getLayoutParams());
//            button.getLayoutParams().height = 190;
//            button.setClickable(true);
//            button.setOnClickListener(this);
//            atomButtonLayout.addView(button);
//
//            modeButtons.add(button);
//        }
        initPeriodicTableButton();

        ImageButton selectionButton = (ImageButton) findViewById(R.id.selectionButton);
        selectionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentMode = Mode.Selection;
                selectedButton = v;
                updateModeButtonColors();
            }
        });
        selectionButton.setElevation(7);

        // this is to get the view at the bottom of the linear layout
        // even though its defined in the xml
        atomButtonLayout.removeView(selectionButton);
        atomButtonLayout.addView(selectionButton);
        modeButtons.add(selectionButton);

        ImageButton panZoomButton = (ImageButton)findViewById(R.id.panZoomButton);
        panZoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                currentMode = Mode.PanZoom;
                selectedButton = v;
                updateModeButtonColors();
            }
        });
        panZoomButton.setElevation(7);
        atomButtonLayout.removeView(panZoomButton);
        atomButtonLayout.addView(panZoomButton);

        modeButtons.add(panZoomButton);
        initImmediateActionButtons();
    }

    // init more buttons in the toolbar
    private void initImmediateActionButtons()
    {
        ImageButton singleBondButton = (ImageButton) findViewById(R.id.singleBondButton);
        singleBondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moleRenderer.addBond(IBond.Order.SINGLE);
            }
        });


        ImageButton doubleBondButton = (ImageButton) findViewById(R.id.doubleBondButton);
        doubleBondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moleRenderer.addBond(IBond.Order.DOUBLE);
            }
        });

        ImageButton tripleBondButton = (ImageButton) findViewById(R.id.tripleBondButton);
        tripleBondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moleRenderer.addBond(IBond.Order.TRIPLE);
            }
        });

        ImageButton deleteAtom = (ImageButton) findViewById(R.id.deleteButton);
        deleteAtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moleRenderer.deleteSelected();
            }
        });

        ImageButton deleteSweep = (ImageButton) findViewById(R.id.deleteSweepButton);
        deleteSweep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sceneContainer.clearScene();
                moleRenderer.updateBitmap();
            }
        });

        init3DButton();
    }

    // init the periodic table button
    private void initPeriodicTableButton()
    {
        LinearLayout atomButtonLayout = (LinearLayout)findViewById(R.id.atomScrollViewLayout);
        ImageButton periodicTableButton = new ImageButton(this);
        periodicTableButton.setImageResource(R.drawable.periodic_table);
        periodicTableButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        periodicTableButton.setLayoutParams(atomButtonLayout.getLayoutParams());
        periodicTableButton.getLayoutParams().height = 220;

        atomButtonLayout.addView(periodicTableButton);
        periodicTableButton.setClickable(true);
        periodicTableButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentMode = Mode.AddAtom;
                selectedButton = v;
                PeriodicTable pt = PeriodicTable.newInstance(0);
                pt.setMainActivity((MainActivity) v.getContext());
                pt.show(getFragmentManager(), "123");
            }
        });
        periodicTableButton.setElevation(7);

        modeButtons.add(periodicTableButton);
    }

    // init button to convert molecule into 3D
    private void init3DButton()
    {
        ImageButton to3DButton = (ImageButton) findViewById(R.id.to3DButton);
        to3DButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(moleRenderer3D == null)
                {
                    // must have a molecule selected in order to render it
                    if (sceneContainer.selectedMolecule == null) {
                        Toast.makeText(MainActivity.this,
                                "Must have a Molecule selected to Render in 3D",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendTo3DRenderer();
                }
                else
                    initRenderer2D();
            }
        });
    }

    // init the 2D renderer
    private void initRenderer2D()
    {
        // if the 2D renderer is open the show the toolbar
        showToolBar(true);
        // remove the 3D renderer
        canvasLayout.removeAllViewsInLayout();
        moleRenderer3D = null;
        moleRenderer = new MoleRenderer2D(this);
        canvasLayout.addView(moleRenderer);
    }

    private void initRenderer3D(IAtomContainer mole)
    {
        showToolBar(false);
        canvasLayout.removeAllViewsInLayout();
        moleRenderer = null;
        moleRenderer3D = null;
        try {
            moleRenderer3D = new MoleRenderer3D(this, mole);
        }
        catch (Exception e)
        {
            Log.e("Could not construct", e.getMessage());
            initRenderer2D();
            return;
        }

        surfView = new SurfaceView(this);
        surfView.setRenderMode(ISurface.RENDERMODE_WHEN_DIRTY);
        surfView.setSurfaceRenderer(moleRenderer3D);
        surfView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                moleRenderer3D.onTouchEvent(event);
                return true; //processed
            }
        });

        if(surfView != null)
            canvasLayout.addView(surfView);
    }

    // creates new progress bar
    private void initProgressBar()
    {
        convertMoleProgress = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        convertMoleProgress.setLayoutParams(params);
        convertMoleProgress.setIndeterminate(true);
        canvasLayout.addView(convertMoleProgress);
    }

    // send the currently selected molecule to PubChem to get the 3D version
    // if it exists
    private void sendTo3DRenderer()
    {
        try
        {
            IAtomContainer container = sceneContainer.selectedMolecule.clone();
            String smilesStr = Molecule.generateSmilesString(container);

            String url = String.format(
                    "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/%s/SDF?record_type=3d",
                    smilesStr);

            new PostRequest(this).execute(url);
            initProgressBar();
        }
        catch (CloneNotSupportedException ex) {
            Log.e("Clone Exception", ex.getMessage());
        }
        catch (CDKException ex) {
            Log.e("CDK Smiles Exception", ex.getMessage());
        }
        catch (Exception ex){
            Log.e("Exception", ex.getMessage());
        }
    }

    // show that buttons are selected;
    // TODO: currently, this does not effect all buttons because they are not "mode" buttons
    // TODO: this is in the process of being changed
    private void updateModeButtonColors()
    {
        for (View v: modeButtons)
        {
            if(v == selectedButton)
                v.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
            else
                v.setBackgroundTintMode(PorterDuff.Mode.LIGHTEN);
        }
    }

    // set the current button that is selected
    // so that it can be queried later;
    public void setSelectedButton(View v){
        updateModeButtonColors();
        selectedButton = v;
    }

    public Mode getCurrentMode() {return currentMode;}

    // gets the current chosen element by casting to atombutton
    public Elements getCurrentElement()
    {
        if(currentMode == Mode.AddAtom && selectedButton != null) {
            AtomButton ab = (AtomButton) selectedButton;
            return ab.getElement();
        }

        return null;
    }

    // updates text that shows the number of atoms, molecules and bonds
    private void updateSceneText()
    {
        if(sceneText != null)
        {
            String numAtomsText = String.format("Number of Atoms: %d", sceneContainer.getAtomCount());
            String numBondsText = String.format("\tNumber of Bonds: %d\n", sceneContainer.getBondCount());
            String numMoleculesText = String.format("Number of Molecules: %d", sceneContainer.getMoleculeCount());
            numAtomsText += numBondsText + numMoleculesText;
            sceneText.setText(numAtomsText);
        }
    }

    // shows and hides toolbar
    private void showToolBar(boolean show)
    {
        if(show) {
            toolbarLayout.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = toolbarLayout.getLayoutParams();
            layoutParams.width = toolBarWidth;
        }
        else {
            toolbarLayout.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams layoutParams = toolbarLayout.getLayoutParams();
            layoutParams.width = 0;
        }
    }

    // this is run after call to sendTo3DRenderer; Async callback;
    @Override
    public void onPostExecute(String results)
    {
        if(results == null) {
            Toast.makeText(this, "Sorry, could not construct molecule.", Toast.LENGTH_LONG).show();
            initRenderer2D();
            return;
        }

        showToolBar(false);
        IAtomContainer molecule = SdfConverter.convertSDFString(results);
        if(molecule != null)
            initRenderer3D(molecule);
        else
            Toast.makeText(this, "Sorry, Could not construct Molecule", Toast.LENGTH_LONG).show();

        convertMoleProgress = null;
    }

    // implements the scene change listeners
    @Override
    public void atomNumberChanged() {
        updateSceneText();
    }

    @Override
    public void bondNumberChanged() {
        updateSceneText();
    }

    @Override
    public void moleculeNumberChanged() {
        updateSceneText();
    }

    // init the information card on the molecule
    public void initInfoCardButton()
    {
        ImageButton infoCardButton = (ImageButton) findViewById(R.id.testingInfoCardButton);
        infoCardButton.setOnClickListener(new View.OnClickListener()
        {
            String smilesStr = null;
            boolean successful = true;

            @Override
            public void onClick(View v)
            {
                if(sceneContainer.selectedMolecule != null)
                {
                    String urlQueryStr = null;
                    try
                    {
                        smilesStr = Molecule.generateSmilesString(sceneContainer.selectedMolecule);
                        Log.i("Smiles", smilesStr);
                        urlQueryStr = String.format("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/%s/description/JSON", smilesStr);
                    }
                    catch (CDKException ex) {
                        successful = false;
                    }

                    // execute post request and then call onPostExecute
                    new PostRequest(new IAsyncResult<String>()
                    {
                        @Override
                        public void onPostExecute(String result)
                        {
                            PubChemCompoundDescription compoundDescription = null;

                            //Log.i("\nResult", " " + result + "\n\n");
                            if (result != null)
                            {
                                try
                                {
                                    JSONObject pubChemDescriptionJson = new JSONObject(result);
                                    JSONArray info = pubChemDescriptionJson.getJSONObject("InformationList").getJSONArray("Information");
                                    JSONObject cid_title = info.getJSONObject(0);

                                    //Testing
//                                    Log.i("Cid", String.valueOf(cid_title.getInt("CID")));
//                                    Log.i("Title", cid_title.getString("Title"));
                                    // end testing

                                    compoundDescription = new PubChemCompoundDescription();
                                    compoundDescription.cid = cid_title.getInt("CID");
                                    compoundDescription.title = cid_title.getString("Title");
                                    compoundDescription.smiles = smilesStr;

                                    for (int i = 1; i < info.length(); i++)
                                    {
                                        JSONObject jRecords = info.getJSONObject(i);
//                                        Log.i("Record", String.format("Record %d", i));
//                                        Log.i("Descriptions", jRecords.getString("Description"));
//                                        Log.i("Descriptions", jRecords.getString("DescriptionSourceName"));
//                                        Log.i("Descriptions", jRecords.getString("DescriptionURL"));
                                        compoundDescription.addRecord(jRecords.getString("Description"),
                                                                      jRecords.getString("DescriptionURL"),
                                                                      jRecords.getString("DescriptionSourceName"));
                                    }
                                }
                                catch (JSONException ex) {
                                    successful = false;
                                }
                            }
                            else
                                successful = false;


                            if(successful)
                            {
                                // Insert dialog here
                                MoleInfoCard moleInfoCard = new MoleInfoCard();
                                moleInfoCard.setCompoundDescription(compoundDescription);
                                moleInfoCard.show(getFragmentManager(), "123");
                            }
                            else
                                Toast.makeText(MainActivity.this, "Unable to gather information on this Molecule. Check connection or Molecule ID",
                                        Toast.LENGTH_LONG).show();
                        }

                    }).execute(urlQueryStr);
                }
                // Molecule not selected
                else
                {
                    Toast.makeText(MainActivity.this, "Must have molecule selected in order to get its information",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
