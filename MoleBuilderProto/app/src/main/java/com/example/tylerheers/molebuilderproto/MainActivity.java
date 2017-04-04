package com.example.tylerheers.molebuilderproto;


import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.openscience.cdk.config.Elements;
import org.openscience.cdk.interfaces.IBond;

import java.util.HashMap;
import java.util.Stack;

//TODO: Add periodic table button and fragment
public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener
{
    private MoleRenderer2D moleRenderer;
    private LockableScrollView lockableScrollView;
    private HashMap<String, ImageButton> actionButtons;
    private SearchMoleDialog diag;

    // TODO: implement undo data structure and find out better way
    private Stack<Action> actions = new Stack<>();
    private ImageButton undoButton;


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

        lockableScrollView = (LockableScrollView)findViewById(R.id.canvasScrollView);
        RelativeLayout canvasLayout = (RelativeLayout) findViewById(R.id.canvasLayout);
        moleRenderer = new MoleRenderer2D(this);
        canvasLayout.addView(moleRenderer);
        moleRenderer.setLayoutParams(new RelativeLayout.LayoutParams(2000, 2000));

        undoButton = (ImageButton)findViewById(R.id.undoActionButton);

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
        undoButton.setOnClickListener(new View.OnClickListener() {
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

        actionButtons.put("singleBondButton", singleBondButton);
        actionButtons.put("doubleBondButton", doubleBondButton);
        actionButtons.put("tripleBondButton", tripleBondButton);
        actionButtons.put("undoButton", undoButton);
    }

    public void lockScrollView(){
        lockableScrollView.setScrollingEnabled(false);
    }
    public void unlockScrollView(){
        lockableScrollView.setScrollingEnabled(true);
    }
    public boolean isScrollable() {return lockableScrollView.isScrollable(); }

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
