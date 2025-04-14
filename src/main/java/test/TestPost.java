package test;

import entities.Post;
import services.PostService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TestPost {
    public static void main(String[] args) {
        try {
            PostService postService = new PostService();


            System.out.println("\n--- Liste des posts ---");
            List<Post> posts = postService.displayList();

            for (Post p : posts) {
                System.out.println("ID: " + p.getId());
                System.out.println("Contenu: " + p.getContenu());
                System.out.println("Date: " + p.getDate_publication());
                System.out.println("Nombre de j'aime: " + p.getNbr_jaime());
                System.out.println("Status: " + p.isStatus_post());


                List<String> images = postService.getMediaForPost(p.getId());
                if (images.isEmpty()) {
                    System.out.println("Aucune image.");
                } else {
                    System.out.println("Images associées :");
                    for (String img : images) {
                        System.out.println("  - " + img);
                    }
                }

                System.out.println("------------------------------------");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
