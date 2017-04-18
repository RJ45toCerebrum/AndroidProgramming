package com.example.tylerheers.molebuilderproto;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;

import javax.vecmath.Point2d;

//TODO: Add periodic table button and fragment
public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener,
                                     IAsyncResult<String>,
                                     SceneContainer.SceneChangeListener
{
    public static int maxAtoms = 30;
    public static int maxSelectedAtoms = 10;

    SceneContainer sceneContainer;
    private TextView sceneText;

    private RelativeLayout canvasLayout;
    SurfaceView surfView;
    private MoleRenderer2D moleRenderer;
    private MoleRenderer3D moleRenderer3D;
    private HashMap<Integer, ImageButton> actionButtons;
    private SearchMoleDialog diag;


    // TODO: implement undo data structure and find out better way
    private Stack<Action> actions = new Stack<>();
    //private ImageButton undoButton;


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
                diag = new SearchMoleDialog();
                diag.setRenderer2D(moleRenderer);
                diag.show(getFragmentManager(), "123");
            }
        });

        canvasLayout = (RelativeLayout) findViewById(R.id.canvasLayout);
        sceneText = (TextView)findViewById(R.id.sceneInfoTextView);

        initRenderer2D();
        initAtomButtonList();
        initModeButtons();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume()
    {
        super.onResume();
    }


    private void initAtomButtonList()
    {
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
            }

            if(button == null)
                continue;

            button.setLayoutParams(atomButtonLayout.getLayoutParams());
            button.getLayoutParams().height = 230;

            atomButtonLayout.addView(button);
            button.setClickable(true);
            button.setOnClickListener(this);
        }
    }

    private void initModeButtons()
    {
        actionButtons = new HashMap<>(6);
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

        ImageButton undoButton = (ImageButton)findViewById(R.id.undoActionButton);
        undoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Action action = actions.pop();
                Toast.makeText(MainActivity.this, action.getActionType().toString(), Toast.LENGTH_LONG).show();
                switch (action.getActionType())
                {
                    case Add:
                        moleRenderer.undoAdd(action.getObjIDList(), action.getClassType());
                        break;
                    case Delete:
                        break;
                    case Manipulation:
                        break;

                }
            }
        });

        init3DButton();

        actionButtons.put(R.id.singleBondButton, singleBondButton);
        actionButtons.put(R.id.doubleBondButton, doubleBondButton);
        actionButtons.put(R.id.tripleBondButton, tripleBondButton);
        actionButtons.put(R.id.undoActionButton, undoButton);
    }

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

        actionButtons.put(R.id.to3DButton, to3DButton);
    }

    private void initRenderer2D()
    {
        canvasLayout.removeAllViewsInLayout();
        moleRenderer3D = null;

        moleRenderer = new MoleRenderer2D(this);
        canvasLayout.addView(moleRenderer);
    }

    private void initRenderer3D(IAtomContainer mole)
    {
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

    //TODO: Add support for triple bonds by replacing # with %23
    //TODO: add Auto-completion for smiles
    private void sendTo3DRenderer()
    {
        try
        {
            IAtomContainer container = sceneContainer.selectedMolecule.clone();
            String smilesStr = Molecule.generateSmilesString(container);
            Log.i("Smiles", smilesStr);

            String url = String.format(
                    "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/%s/SDF?record_type=3d",
                    smilesStr);

            new PostRequest(this).execute(url);
        }
        catch (CloneNotSupportedException ex) {
            Log.e("Clone Exception", ex.getMessage());
        }
        catch (CDKException ex) {
            Log.e("CDK Smiles Exception", ex.getMessage());
        }
    }

    public void addAction(Action action) {
        actions.push(action);
    }

    @Override
    public void onPostExecute(String results)
    {
        if(results == null) {
            Toast.makeText(this, "Could not find molecule", Toast.LENGTH_LONG).show();
            initRenderer2D();
            return;
        }

        IAtomContainer molecule = SdfConverter.convertSDFString(results);
        if(molecule != null)
            initRenderer3D(molecule);
        else
            Toast.makeText(this, "Sorry, Could not construct Molecule", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v)
    {
        AtomButton atomButton = (AtomButton)v;
        moleRenderer.addAtom(atomButton.getElement());
    }

    private void updateSceneText()
    {
        String numAtomsText = String.format("Number of Atoms: %d", sceneContainer.getAtomCount());
        String numBondsText = String.format("\tNumber of Bonds: %d\n", sceneContainer.getBondCount());
        String numMoleculesText = String.format("Number of Molecules: %d", sceneContainer.getMoleculeCount());
        numAtomsText += numBondsText + numMoleculesText;
        sceneText.setText(numAtomsText);
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
}
