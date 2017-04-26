package com.example.tylerheers.molebuilderproto;


import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
import org.openscience.cdk.Atom;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


//TODO: Molecule information card; if available
//TODO: Fix Bond distances and add colors or better text of the atoms in 2D-renderer
//TODO: quick access sliding toolbar in the 2D renderer to quick access to the pan/move and selection tool
//TODO: Undo actions
//TODO: atom charge and name text in the 3D renderer
//TODO: make the selection tool better via; drag box selection
public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener,
                                     IAsyncResult<String>,
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

        canvasLayout = (RelativeLayout) findViewById(R.id.canvasLayout);
        toolbarLayout = (LinearLayout) findViewById(R.id.toolBarLayout);
        toolBarWidth = toolbarLayout.getLayoutParams().width;

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sceneText = (TextView) findViewById(R.id.sceneInfoTextView);
            updateSceneText();
        }

        ImageButton helpDiagButton = (ImageButton) findViewById(R.id.helpButton);
        helpDiagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialog helpDialog = new HelpDialog();
                helpDialog.show(getFragmentManager(), "123");
            }
        });

        initRenderer2D();
        initModeButtons();
        initImmediateActionButtons();
        updateModeButtonColors();

        // Test Code to delete
        TestCode();
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

    private void initModeButtons()
    {
        modeButtons = new ArrayList<>(12);

        LinearLayout atomButtonLayout = (LinearLayout)findViewById(R.id.atomScrollViewLayout);
        String[] elementsArray = getResources().getStringArray(R.array.elements);

        for(String e : elementsArray)
        {
            AtomButton button = null;
            switch (e)
            {
                case "hydrogen":
                    button = new AtomButton(this);
                    button.setElement(Elements.Hydrogen);
                    button.setText(Elements.HYDROGEN.getSymbol());
                    break;
                case "carbon":
                    button = new AtomButton(this);
                    button.setElement(Elements.Carbon);
                    button.setText(Elements.CARBON.getSymbol());
                    break;
                case "nitrogen":
                    button = new AtomButton(this);
                    button.setElement(Elements.Nitrogen);
                    button.setText(Elements.NITROGEN.getSymbol());
                    break;
                case "oxygen":
                    button = new AtomButton(this);
                    button.setElement(Elements.Oxygen);
                    button.setText(Elements.OXYGEN.getSymbol());
                    break;
                case "phosphorus":
                    button = new AtomButton(this);
                    button.setElement(Elements.Phosphorus);
                    button.setText(Elements.PHOSPHORUS.getSymbol());
                    break;
                case "sulfur":
                    button = new AtomButton(this);
                    button.setElement(Elements.Sulfur);
                    button.setText(Elements.SULFUR.getSymbol());
                    break;
            }

            if(button == null)
                continue;

            button.setLayoutParams(atomButtonLayout.getLayoutParams());
            button.getLayoutParams().height = 190;
            button.setClickable(true);
            button.setOnClickListener(this);
            atomButtonLayout.addView(button);

            modeButtons.add(button);
        }

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
    }

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

    private void init3DButton()
    {
        ImageButton to3DButton = (ImageButton) findViewById(R.id.to3DButton);
        to3DButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Animation buttonRotationAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_up_down);
                buttonRotationAnim.setDuration(1000);
                v.startAnimation(buttonRotationAnim);


                if(moleRenderer3D == null)
                {
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

        float centreX = to3DButton.getX() + to3DButton.getWidth()  / 2;
        float centerY = to3DButton.getY() + to3DButton.getHeight() / 2;
        to3DButton.setPivotX(centreX);
        to3DButton.setPivotY(centerY);
    }

    private void initRenderer2D()
    {
        showToolBar(true);
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
        moleRenderer3D= null;
        try {
            moleRenderer3D = new MoleRenderer3D(this, mole);
        }
        catch (Exception e){
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

    public void setSelectedButton(View v){
        updateModeButtonColors();
        selectedButton = v;
    }

    public Mode getCurrentMode() {return currentMode;}

    public Elements getCurrentElement()
    {
        if(currentMode == Mode.AddAtom && selectedButton != null) {
            AtomButton ab = (AtomButton) selectedButton;
            return ab.getElement();
        }

        return null;
    }

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

    @Override
    public void onPostExecute(String results)
    {
        if(results == null) {
            Toast.makeText(this, "Could not find molecule", Toast.LENGTH_LONG).show();
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

    // onClick executed when atom buttons clicked
    @Override
    public void onClick(View v)
    {
        currentMode = Mode.AddAtom;
        selectedButton = v;
        updateModeButtonColors();
    }

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

    public void TestCode()
    {
        // 1) Name, cid, Formula, Description
        // 2) Image
        // 3) smiles

        ImageButton infoCard = (ImageButton) findViewById(R.id.testingInfoCardButton);
        infoCard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new PostRequest(new IAsyncResult<String>()
                {
                    @Override
                    public void onPostExecute(String result)
                    {
                        Log.i("Result", " " + result);
//                        String findStr = "\"DescriptionURL\": ";
//                        int index = result.indexOf(findStr, 120);
//                        if (index != -1) {
//                            int index2 = result.indexOf("\n", index);
//                            if (index2 != -1) {
//                                Log.i("description", result.substring(index + findStr.length(), index2));
//                            }
//                        }
                        if(result != null)
                        {
                            try
                            {
                                JSONObject descriptionJson = new JSONObject(result);
                                JSONArray info = descriptionJson.getJSONObject("InformationList").getJSONArray("Information");

                                JSONObject cid_title = info.getJSONObject(0);
                                Log.i("Cid", String.valueOf(cid_title.getInt("CID")));
                                Log.i("Title", cid_title.getString("Title"));
                                for(int i = 1; i < info.length(); i++)
                                {
                                    Log.i("Record", String.format("Record %d", i));
                                    JSONObject jo = info.getJSONObject(i);
                                    Log.i("Descriptions", jo.getString("Description"));
                                    Log.i("Descriptions", jo.getString("DescriptionSourceName"));
                                    Log.i("Descriptions", jo.getString("DescriptionURL"));
                                }
                            }
                            catch (JSONException ex) {
                                Toast.makeText(MainActivity.this, "Unable to gather information on this Molecule. Check connection or Molecule ID",
                                                Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).execute("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/Serine/description/JSON");

                // replace space with %20
                // https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/dopeamine/description/JSON"     this is for descriptions
            }
        });
    }

}
