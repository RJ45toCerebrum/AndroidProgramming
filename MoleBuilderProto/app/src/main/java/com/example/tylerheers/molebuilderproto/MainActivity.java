package com.example.tylerheers.molebuilderproto;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.openscience.cdk.config.Elements;
import org.openscience.cdk.interfaces.IBond;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

//TODO: Add periodic table button and fragment
public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener
{
    public static int maxAtoms = 30;
    public static int maxSelectedAtoms = 10;

    private static HashMap<String, MoleculeAtom> atoms = new HashMap<>();
    private static HashMap<String, Molecule> molecules = new HashMap<>();
    public static Molecule selectedMolecule = null;    // mole that is rendered for 3D rendering
    private static int numCreatedAtoms = 0;
    private static int numCreatedBonds = 0;
    private static int numCreatedMolecules = 0;

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
                if(MainActivity.selectedMolecule == null) {
                    Toast.makeText(MainActivity.this,
                            "Must have a Molecule selected to Render in 3D",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                canvasLayout.removeAllViewsInLayout();
                moleRenderer = null;
                initRenderer3D();
                if(surfView != null)
                    canvasLayout.addView(surfView);
            }
        });

        actionButtons.put(R.id.to3DButton, to3DButton);
    }

    private void initRenderer2D()
    {
        canvasLayout = (RelativeLayout) findViewById(R.id.canvasLayout);
        moleRenderer = new MoleRenderer2D(this);
        canvasLayout.addView(moleRenderer);
    }

    private void initRenderer3D()
    {
        moleRenderer3D = new MoleRenderer3D(this);
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
    }

    // Statics
    public static boolean putAtom(MoleculeAtom ma)
    {
        if(ma != null) {
            numCreatedAtoms++;
            atoms.put(ma.getID(), ma);
            return true;
        }

        return false;
    }
    public static Collection<MoleculeAtom> getAtoms(){
        return atoms.values();
    }
    public static boolean delAtom(String key)
    {
        if(atoms.remove(key) != null) {
            numCreatedAtoms--;
            return true;
        }

        return false;
    }

    public static boolean putMolecule(Molecule mole)
    {
        if(mole != null) {
            numCreatedMolecules++;
            molecules.put(mole.getID(), mole);
            return true;
        }

        return false;
    }
    public static Collection<Molecule> getMolecules() {
        return molecules.values();
    }
    public static boolean delMolecule(String key)
    {
        if(molecules.remove(key) != null)
            return true;

        return false;
    }

    public static void addBondCount(int count) {
        numCreatedBonds += count;
    }
    public static int getBondCount() {return numCreatedBonds;}

    public static void addAtomCount(int count){
        numCreatedAtoms += count;
    }
    public static int getAtomCount() {return numCreatedAtoms;}

    public static void addMoleculeCount(int count){
        numCreatedMolecules += count;
    }
    public static Molecule getMolecule(String key){return  molecules.get(key); }
    public static int getMoleculeCount() {return numCreatedMolecules; }
    // end statics

    public void addAction(Action action) {
        actions.push(action);
    }

    @Override
    public void onClick(View v)
    {
        AtomButton atomButton = (AtomButton)v;
        moleRenderer.addAtom(atomButton.getElement());
    }
}
