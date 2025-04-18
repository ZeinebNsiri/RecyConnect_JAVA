package entities;

public class Cours {

    private int id;
    private CategorieCours categorieCours;
    private String titreCours;
    private String descriptionCours;
    private String video;
    private String imageCours;

    public Cours() {
    }

    public Cours(int id, CategorieCours categorieCours, String titreCours, String descriptionCours, String video, String imageCours) {
        this.id = id;
        this.categorieCours = categorieCours;
        this.titreCours = titreCours;
        this.descriptionCours = descriptionCours;
        this.video = video;
        this.imageCours = imageCours;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CategorieCours getCategorieCours() {
        return categorieCours;
    }

    public void setCategorieCours(CategorieCours categorieCours) {
        this.categorieCours = categorieCours;
    }

    public String getTitreCours() {
        return titreCours;
    }

    public void setTitreCours(String titreCours) {
        this.titreCours = titreCours;
    }

    public String getDescriptionCours() {
        return descriptionCours;
    }

    public void setDescriptionCours(String descriptionCours) {
        this.descriptionCours = descriptionCours;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getImageCours() {
        return imageCours;
    }

    public void setImageCours(String imageCours) {
        this.imageCours = imageCours;
    }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", categorieCours=" + categorieCours +
                ", titreCours='" + titreCours + '\'' +
                ", descriptionCours='" + descriptionCours + '\'' +
                ", video='" + video + '\'' +
                ", imageCours='" + imageCours + '\'' +
                '}';
    }
}
