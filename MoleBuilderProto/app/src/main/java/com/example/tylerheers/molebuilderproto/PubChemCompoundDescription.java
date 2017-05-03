package com.example.tylerheers.molebuilderproto;

import android.support.annotation.Nullable;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tylerheers on 4/26/17.
 *
 * Contains all the information gained from the molecule information request
 */

class PubChemCompoundDescription
{
    public class Record
    {
        String description;
        String url;
        String source;

        public Record(){}
        public Record(String description, String url, String source) {
            this.description = description;
            this.url = url;
            this.source = source;
        }
    }

    public Integer cid = null;
    public String title;
    public String smiles;
    List<Record> records;


    public PubChemCompoundDescription()
    {
        records = new ArrayList<>(3);
    }
    public PubChemCompoundDescription(int cid, String title, String smilesStr)
    {
        this.cid = cid;
        this.title = title;
        this.smiles = smilesStr;
        records = new ArrayList<>(3);
    }

    public void addRecord(String description, String url, String source)
    {
        description = description.replaceAll("<\\/?a[^>]*>", "");
        if(description.length() > 200)
            description = description.substring(0, 114) + "...";

        Record newRec = new Record(description, url, source);
        records.add(newRec);
    }
    public boolean removeRecord(Record record) {
        return record != null && records.remove(record);
    }

    @Nullable
    public Record get(int i) {
        if(i >= records.size())
            return null;
        return records.get(i);
    }
    public List<Record> getRecords() {
        return records;
    }
    public int recordSize(){return records.size();}
}
