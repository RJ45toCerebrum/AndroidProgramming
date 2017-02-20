package com.example.tylerheers.notetoself;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tyler on 2/14/2017.
 */

public class Note
{
    public enum ImportanceLevel {
        NotImportant,
        SomewhatImportant,
        Important,
        VeryImportant
    }

    public final Integer MaxTitleLength = 100;
    public final Integer MaxDescriptionLength = 400;

    // implementing SERIALIZATION
    private static final String JSON_TITLE = "title";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_IDEA = "idea" ;
    private static final String JSON_TODO = "todo";
    private static final String JSON_IMPORTANT = "important";
    private static final String JSON_LEVEL_ORDER = "levelOrder";


    private String title;
    private String description;
    private boolean idea;
    private boolean todo;
    private boolean important;
    private ImportanceLevel level = ImportanceLevel.Important;
    private int levelOrder = level.ordinal();


    public Note(JSONObject jsonObject) throws JSONException
    {
        title = jsonObject.getString(JSON_TITLE);
        description = jsonObject.getString(JSON_DESCRIPTION);
        idea = jsonObject.getBoolean(JSON_IDEA);
        todo = jsonObject.getBoolean(JSON_TODO);
        important = jsonObject.getBoolean(JSON_IMPORTANT);
        levelOrder = jsonObject.getInt(JSON_LEVEL_ORDER);

        switch (levelOrder)
        {
            case 0:
                level = ImportanceLevel.NotImportant;
                break;
            case 1:
                level = ImportanceLevel.SomewhatImportant;
                break;
            case 2:
                level = ImportanceLevel.Important;
                break;
            default:
                level = ImportanceLevel.VeryImportant;
        }
    }

    public Note(){}

    // Bunch of getter and setters bellow

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if(title.length() <= MaxTitleLength)
            this.title = title;
        else
            throw new IllegalArgumentException(
                    String.format("The length of the title is longer than {0}", this.MaxTitleLength));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description.length() <= this.MaxDescriptionLength)
            this.description = description;
        else
            throw  new IllegalArgumentException(
                    String.format("The length of the description is longer than {0}", this.MaxDescriptionLength));
    }

    public boolean isIdea() {
        return idea;
    }

    public void setIdea(boolean idea) {
        this.idea = idea;
    }

    public boolean isTodo() {
        return todo;
    }

    public void setTodo(boolean todo) {
        this.todo = todo;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public ImportanceLevel getLevel() {
        return level;
    }

    public void setLevel(ImportanceLevel level) {
            this.level = level;
    }

    public JSONObject convertToJSON() throws JSONException{
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JSON_TITLE, title);
        jsonObject.put(JSON_DESCRIPTION, description);
        jsonObject.put(JSON_IDEA, idea);
        jsonObject.put(JSON_TODO, todo);
        jsonObject.put(JSON_IMPORTANT, important);
        jsonObject.put(JSON_LEVEL_ORDER, levelOrder);

        return jsonObject;
    }

}
