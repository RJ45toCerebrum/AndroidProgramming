package com.example.tylerheers.humananimalyearconverter;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{
    ArrayList<Pair<String, Double>> nameAgePairs;
    TableLayout humanAnimalAgeLayout;
    EditText humanYearsEditText;
    Button convertAgeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        humanAnimalAgeLayout = (TableLayout) findViewById(R.id.conversionTable);
        humanYearsEditText = (EditText) findViewById(R.id.humanYearsEditText);
        convertAgeButton = (Button) findViewById(R.id.convertButton);

        try
        {
            setAgeMapping();
        }
        catch(IOException err){
            Log.i("Error Message", err.getMessage());
        }
        catch(Exception err){
            Log.i("Error Message", err.getMessage());
        }

        convertAgeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateTableAges();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        updateTableNames();
        updateTableAges();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setAgeMapping() throws IOException
    {
        nameAgePairs = new ArrayList<>();

        InputStream is = getResources().openRawResource(R.raw.human_to_animal_ages);
        int size = is.available();
        byte buffer[] = new byte[size];
        is.read(buffer);
        is.close();
        String info = new String(buffer);

        String[] s = info.split("\n");
        for (int i = 0; i < s.length; i++)
        {
            String[] nameAge = s[i].split(" ");

            String newAgeString = nameAge[1].trim();
            nameAgePairs.add(new Pair<String, Double>(nameAge[0].trim(), Double.parseDouble(newAgeString)));
        }

    }

    private void updateTableNames()
    {
        for(int i = 0; i < humanAnimalAgeLayout.getChildCount(); i++){
            TableRow row = (TableRow) humanAnimalAgeLayout.getChildAt(i);
            TextView animalNameView = (TextView)row.getChildAt(0);

            animalNameView.setText(nameAgePairs.get(i).a);
        }
    }

    private void updateTableAges()
    {

        for(int i = 0; i < humanAnimalAgeLayout.getChildCount(); i++){
            TableRow row = (TableRow) humanAnimalAgeLayout.getChildAt(i);
            TextView ageView = (TextView)row.getChildAt(1);

            String ageString = humanYearsEditText.getText().toString();
            Double humanAge = Double.parseDouble(ageString);
            Double animalAge = humanAge * nameAgePairs.get(i).b;

            ageView.setText(animalAge.toString());
        }
    }

}
