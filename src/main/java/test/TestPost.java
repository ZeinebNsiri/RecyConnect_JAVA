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



            int idPostASupprimer = 16; // remplace par l'ID réel
            Post postASupprimer = new Post();
            postASupprimer.setId(idPostASupprimer);

            postService.delete(postASupprimer);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
