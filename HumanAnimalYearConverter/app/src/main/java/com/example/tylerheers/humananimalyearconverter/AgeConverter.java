package com.example.tylerheers.humananimalyearconverter;

import java.util.HashMap;

/**
 * Created by tylerheers on 2/12/17.
 */

public class AgeConverter
{
    private HashMap<String, Ratio> animalToAge;

    public AgeConverter() {
        animalToAge = new HashMap();
    }

    // Insert into the Map; Warning: this overrides
    // previous value associated to key of key was already in the map
    public void insert(String key, Ratio ageRatio){
        animalToAge.put(key, ageRatio);
    }

    public void setMapping(HashMap<String, Ratio> map){
        animalToAge = map;
    }

    public void remove(String key){
        animalToAge.remove(key);
    }

    public Ratio get(String key){
        return animalToAge.get(key);
    }

    public boolean isEmpty(){
        return animalToAge.isEmpty();
    }

    public  boolean contains(String key){
        return animalToAge.containsKey(key);
    }
}
