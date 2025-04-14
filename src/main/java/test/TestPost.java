package test;

import entities.Post;
import services.PostService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TestPost {
    public static void main(String[] args) {
        try {
            // 1. Créer un objet PostService
            PostService postService = new PostService();

            // 2. Créer un objet Post à insérer
            Post post = new Post();
            post.setUser_p_id(1); // Remplace par un ID utilisateur valide
            post.setContenu("Ceci est un test de post depuis la console");
            post.setDate_publication(LocalDateTime.now());
            post.setNbr_jaime(0);
            post.setStatus_post(true);



            // 3. Option B : Ajouter avec des chemins d'images
            List<String> mediaPaths = Arrays.asList("chemin/image1.jpg", "chemin/image2.png");
            postService.addWithMedia(post, mediaPaths);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
