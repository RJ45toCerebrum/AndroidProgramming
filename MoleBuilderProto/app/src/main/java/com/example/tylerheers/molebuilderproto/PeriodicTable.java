package com.example.tylerheers.molebuilderproto;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.openscience.cdk.Element;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.interfaces.IElement;

import java.util.ArrayList;


public class PeriodicTable extends DialogFragment
{
    int theme;


    static PeriodicTable newInstance(int themeNumber)
    {
        PeriodicTable pt = new PeriodicTable();
        Bundle args = new Bundle();
        args.putInt("theme", themeNumber);
        pt.setArguments(args);
        return pt;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        theme = getArguments().getInt("theme");

        // Pick a style based on the num.
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.periodic_table_dialog, container, false);

        GridView gv = (GridView) v.findViewById(R.id.periodicTableView);
        gv.setAdapter(new PeriodicTableAdapter(getActivity()));
        return v;
    }

}
