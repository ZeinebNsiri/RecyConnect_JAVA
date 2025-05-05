package entities;

public class CustomTag {
    private int id;
    private String label;

    public CustomTag() {}

    public CustomTag(String label) {
        this.label = label;
    }

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}

