package enums;

public enum PostTag {
    DURABILITE("#Durabilité"),
    REUTILISATION("#Réutilisation"),
    RECYCLAGE_PLASTIQUE("#RecyclagePlastique"),
    RECYCLAGE_PAPIER("#RecyclagePapier"),
    RECYCLAGE_METAL("#RecyclageMétal"),
    RECYCLAGE_VERRE("#RecyclageVerre"),
    DIY_RECYCLAGE("#DIYRecyclage"),
    QUESTION("#Question"),
    RECLAMATION("#Réclamation"),
    INITIATIVE("#Initiative");

    private final String label;

    PostTag(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PostTag fromLabel(String label) {
        for (PostTag tag : values()) {
            if (tag.label.equalsIgnoreCase(label)) {
                return tag;
            }
        }
        throw new IllegalArgumentException("No matching PostTag for label: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}