package com.example.tylerheers.notetoself;

import android.content.Context;
import android.util.Log;
import android.widget.MultiAutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tylerheers on 2/19/17.
 */

// steps to make better
    /*
         1) Make abstract class
         2) save and load abstract methods
         3) maybe make this one save Single objecst
         4) make a JsonSerializerList which will save lists
     */
public class JsonSerializer
{
    private String fileName;
    private Context context;    // need thsi in order to write to files

    // takes file name and the context
    public JsonSerializer(String fileName, Context con){
        this.fileName = fileName;
        context = con;
    }

    public void save(List<Note> notes) throws JSONException, IOException
    {
        JSONArray jsonArray = new JSONArray();

        for(Note n : notes){
            jsonArray.put(n.convertToJSON());
        }

        Writer writer = null;
        try
        {
            OutputStream out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(jsonArray.toString());
        }
        catch (IOException e){
            Log.i("Error Message", e.getMessage());
        }
        finally
        {
            if(writer != null)
                writer.close();
        }
    }

//     this is were it goes wrong because we have a NoteList
//     in a serializer that should be general
//     better way is to use a generic where the generics implement the
//     ISerializable or some interface we make
//     create new object of T and call that method and add to List<T>
    public ArrayList<Note> load() throws JSONException, IOException
    {
        ArrayList<Note> noteList = new ArrayList<Note>();
        BufferedReader reader = null;

        try
        {
            InputStream in = context.openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null){
                jsonString.append(line);
            }

            JSONArray jsonArray = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            for(int i = 0; i < jsonArray.length(); i++){
                noteList.add(new Note(jsonArray.getJSONObject(i)));
            }
        }
        catch (FileNotFoundException e) {
            Log.i("Error Message", e.getMessage());
        }
        finally  {
            if(reader != null)
                reader.close();
        }

        return noteList;
    }

}
