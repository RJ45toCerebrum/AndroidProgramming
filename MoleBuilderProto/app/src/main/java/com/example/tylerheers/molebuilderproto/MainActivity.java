package com.example.tylerheers.molebuilderproto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.openscience.cdk.Atom;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IElement;


public class MainActivity extends AppCompatActivity
                            implements View.OnClickListener
{
    private MoleRenderer2D moleRenderer;
    ImageButton searchMoleButton;
    ImageButton[] bondButtons;
    SearchMoleDialog diag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchMoleButton = (ImageButton) findViewById(R.id.startMoleSearch);

        searchMoleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diag = new SearchMoleDialog();
                diag.setRenderer2D(moleRenderer);
                diag.show(getFragmentManager(), "123");
            }
        });

        moleRenderer = (MoleRenderer2D) findViewById(R.id.moleRenderer2D);

        initAtomButtonList();
        initBondButtons();
    }

    private void initAtomButtonList()
    {
        LinearLayout atomButtonLayout = (LinearLayout)findViewById(R.id.atomScrollViewLayout);

        for(Elements e : Elements.values())
        {
            AtomImageButton button = new AtomImageButton(this);
            IElement element = e.toIElement();
            button.setAtom(element);

            switch (element.getSymbol())
            {
                case "H":
                    button.setImageResource(R.drawable.atoms_hydrogen);
                    break;
                case "C":
                    button.setImageResource(R.drawable.atoms_carbon);
                    break;
                default:
                    button.setImageResource(R.drawable.atoms_hydrogen);
                    break;
            }

            button.setLayoutParams(atomButtonLayout.getLayoutParams());
            button.setScaleType(ImageView.ScaleType.FIT_CENTER);
            button.getLayoutParams().height = 400;

            atomButtonLayout.addView(button);
            button.setClickable(true);
            button.setOnClickListener(this);
        }
    }

    private void initBondButtons()
    {
        bondButtons = new ImageButton[3];
        bondButtons[0] = (ImageButton) findViewById(R.id.singleBondImageButton);
        bondButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moleRenderer.addBond(IBond.Order.SINGLE);
            }
        });

        bondButtons[1] = (ImageButton) findViewById(R.id.doubleBondImageButton);
        bondButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moleRenderer.addBond(IBond.Order.DOUBLE);
            }
        });

        bondButtons[2] = (ImageButton) findViewById(R.id.tripleBondImageButton);
        bondButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moleRenderer.addBond(IBond.Order.TRIPLE);
            }
        });

    }

    @Override
    public void onClick(View v){
        AtomImageButton atomButton = (AtomImageButton)v;
        moleRenderer.addAtom(new Atom(atomButton.getAtom()));
    }
}
