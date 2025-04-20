package entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import java.io.File;

public class Cours {

    private int id;
    private CategorieCours categorieCours;
    private String titreCours;
    private String descriptionCours;
    private String video;
    private String imageCours;

    public Cours() {
    }

    // Constructeur complet :
    // ordre : id, imageCours, titreCours, categorieCours, descriptionCours, video
    public Cours(int id, String imageCours, String titreCours, CategorieCours categorieCours, String descriptionCours, String video) {
        this.id = id;
        this.imageCours = imageCours;
        this.titreCours = titreCours;
        this.categorieCours = categorieCours;
        this.descriptionCours = descriptionCours;
        this.video = video;
    }

    // Getters et Setters

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

    // Retourne un aperçu de l'image sous forme d'ImageView
    public ImageView getImageView() {
        String path;
        if (imageCours == null || imageCours.isEmpty()) {
            path = "uploadsworkshop/default.png";
        } else {
            path = "uploadsworkshop/" + imageCours;
        }
        File file = new File(path);
        Image img = new Image(file.toURI().toString());
        ImageView iv = new ImageView(img);
        iv.setFitWidth(100);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    // Retourne un aperçu de la vidéo sous forme de MediaView
    public MediaView getVideoView() {
        if (video == null || video.trim().isEmpty()) {
            return new MediaView();
        }
        String path = "uploadsworkshop/" + video;
        File file = new File(path);
        if (!file.exists()) {
            return new MediaView();
        }
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setMute(true);
            mediaPlayer.setAutoPlay(true);
            MediaView mv = new MediaView(mediaPlayer);
            mv.setFitWidth(100);
            mv.setPreserveRatio(true);
            return mv;
        } catch (MediaException e) {
            System.out.println("Erreur lors de la lecture de la vidéo : " + e.getMessage());
            return new MediaView();
        }
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
