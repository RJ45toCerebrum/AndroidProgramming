package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.openscience.cdk.config.Elements;

import java.util.ArrayList;


class PeriodicTableAdapter extends BaseAdapter
{
    private ArrayList<Elements> es;
    private Context context;
    private MoleRenderer2D moleRenderer;
    private PeriodicTable pt;

    PeriodicTableAdapter(Context c, PeriodicTable pt,  MoleRenderer2D moleRenderer)
    {
        this.pt = pt;
        this.moleRenderer = moleRenderer;
        context = c;
        es = new ArrayList<>(86);
        for(int i = 1; i < 86; i++) {
            es.add(Elements.ofNumber(i));
        }
    }

    @Override
    public int getCount(){
        return es.size();
    }

    @Override
    public long getItemId(int item) {
        return item;
    }

    @Override
    public Elements getItem(int id){
        return es.get(id);
    }

    @Override
    public View getView(int index, View v, ViewGroup g)
    {
        AtomButton elementButton;
        if(v == null)
            elementButton = new AtomButton(context);
        else
            elementButton = (AtomButton)v;

        Elements e = es.get(index);
        char[] symbol = e.symbol().toLowerCase().toCharArray();
        if(symbol.length != 0)
            symbol[0] = Character.toUpperCase(symbol[0]);

        elementButton.setElement(e);
        elementButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                AtomButton atomButton = (AtomButton)v;
                moleRenderer.addAtom(atomButton.getElement());
                pt.dismiss();
            }
        });

        elementButton.setText(new String(symbol));
        elementButton.setTransformationMethod(null);
        return elementButton;
    }

}
