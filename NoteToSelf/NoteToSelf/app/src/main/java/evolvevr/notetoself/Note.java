package evolvevr.notetoself;

/**
 * Created by Tyler on 2/14/2017.
 */

public class Note
{
    public enum ImportanceLevel {
        NotImportant, SomewhatImportant, Important, VeryImportant
    }

    public final Integer MaxTitleLength = 100;
    public final Integer MaxDescriptionLength = 400;

    private String title;
    private String description;
    private boolean idea;
    private boolean todo;
    private boolean important;
    private ImportanceLevel level = ImportanceLevel.Important;

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


}
