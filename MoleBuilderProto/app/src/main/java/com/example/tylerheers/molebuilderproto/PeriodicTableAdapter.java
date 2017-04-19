package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import org.openscience.cdk.config.Elements;

import java.util.ArrayList;


class PeriodicTableAdapter extends BaseAdapter
{
    ArrayList<Elements> es;
    private Context context;

    PeriodicTableAdapter(Context c)
    {
        context = c;
        es = new ArrayList<>(86);
        for(int i = 0; i < 86; i++) {
            Log.i("Elements", Elements.ofNumber(i).symbol());
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
        Button elementButton;
        if(v == null){
            elementButton = new Button(context);
            elementButton.setText(es.get(index).symbol());
        }
        else
            elementButton = (Button)v;

        return elementButton;
    }

}
