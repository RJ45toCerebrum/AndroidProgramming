package com.example.tylerheers.molebuilderproto;


import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.openscience.cdk.Atom;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.interfaces.IBond;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener
{
    //TODO: implement each mode such that you first select the mode
    //TODO: before performing actions; Atom mode allows you to add
    //TODO: atoms for example; and you only add in that mode
    public enum Mode
    {
        Creation, Manipulation
    }

    public enum Action
    {
        CreateAtom, CreateBond
    }

    private MoleRenderer2D moleRenderer;
    private HashMap<String, ImageButton> creationModeButtons;
    private SearchMoleDialog diag;

    // Every mode has its corresponding button
    private ImageButton addAtomButton;

    private LinearLayout toolbarLayout;
    private ScrollView atomScrollView;
    private ViewGroup.LayoutParams atomSVLayoutParams;
    private int atomSVVisibleHeight;
    private ScrollView modeScrollView;
    private ViewGroup.LayoutParams modeSVLayoutParams;
    private LinearLayout.LayoutParams filledParam;

    public Mode currentMode = Mode.Creation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();

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

        LinearLayout lo = (LinearLayout)findViewById(R.id.buildAtomLayout);
        moleRenderer = new MoleRenderer2D(this);
        lo.addView(moleRenderer);

        initAtomButtonList();
        initModeButtons();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume()
    {
        super.onResume();

        if(currentMode == Mode.Creation)
            setAtomButtonListVisibility(true);
        else
            setAtomButtonListVisibility(false);
    }

    private void setAtomButtonListVisibility(boolean makeVisible)
    {
        if(makeVisible)
        {
            modeScrollView.setLayoutParams(modeSVLayoutParams);
            atomScrollView.setVisibility(View.VISIBLE);
            atomSVLayoutParams.height = atomSVVisibleHeight;
        }
        else
        {
            atomScrollView.setVisibility(View.INVISIBLE);
            atomSVLayoutParams.height = 0;
            modeScrollView.setLayoutParams(filledParam);
        }
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
        creationModeButtons = new HashMap<>(6);
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

        creationModeButtons.put("singleBondButton", singleBondButton);
        creationModeButtons.put("doubleBondButton", doubleBondButton);
        creationModeButtons.put("tripleBondButton", tripleBondButton);
    }

    private void initToolbar()
    {
        toolbarLayout = (LinearLayout)findViewById(R.id.toolBarLayout);
        atomScrollView = (ScrollView)findViewById(R.id.atomScrollView);
        atomSVLayoutParams = atomScrollView.getLayoutParams();
        atomSVVisibleHeight = atomSVLayoutParams.height;

        modeScrollView = (ScrollView)findViewById(R.id.modeScrollView);
        modeSVLayoutParams = modeScrollView.getLayoutParams();

        filledParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        );
    }

    private void setButtonColors(ImageButton selectedButton)
    {
        for (String b: creationModeButtons.keySet())
        {
            ImageButton ib = creationModeButtons.get(b);
            if(ib == selectedButton)
                ib.setColorFilter(Color.RED);
            else
                ib.setColorFilter(Color.GRAY);
        }
    }

    @Override
    public void onClick(View v)
    {
        AtomButton atomButton = (AtomButton)v;
        moleRenderer.addAtom(atomButton.getElement());
    }
}
