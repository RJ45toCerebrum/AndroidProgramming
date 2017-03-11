package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
{
    // used to fill the list view with buttons
    public class AtomAdapter extends BaseAdapter
    {
        ArrayList<String> buttons = new ArrayList<>();

        @Override
        public int getCount(){
            return buttons.size();
        }

        @Override
        public View getView(int item, View v, ViewGroup group)
        {
            if(v == null){
                LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = in.inflate(R.layout.atom_button_layout, group, false);
            }

            ImageButton b = (ImageButton) v.findViewById(R.id.atomButton);
            Bitmap icon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.atoms_carbon);
            if(icon != null){
                b.setImageBitmap(icon);
            }

            return v;
        }

        @Override
        public String getItem(int id){
            return buttons.get(id);
        }

        @Override
        public long getItemId(int id){
            return id;
        }

        public void addAtom(String atomType) {
            if (atomType != null) {
                buttons.add(atomType);
            }
        }

    }

    private ListView atomListView;
    private AtomAdapter atomAdapter;

    private LinearLayout layout;
    private MoleRenderer2D moleRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = (LinearLayout) findViewById(R.id.buildAtomLayout);
        try {
            //moleRenderer = new MoleRenderer2D(this);
            layout.addView(moleRenderer);
        }
        catch (Exception err){
            Log.i("Error message", err.getMessage());
        }


        atomAdapter = new AtomAdapter();
        atomAdapter.addAtom("Carbon");
        atomAdapter.addAtom("Hydrogen");

        atomListView = (ListView) findViewById(R.id.atomListView);

        atomListView.setAdapter(atomAdapter);

    }

    public void setDownloadImage(Bitmap bitmap){
        //downloadImage.setImageBitmap(bitmap);
    }
}
