package entities;

import java.time.LocalDate;

public class Rating {
    private int id;
    private int user;
    private Cours cours;
    private int note;
    private LocalDate dateRate;

    public Rating() {}

    public Rating(int user, Cours cours, int note, LocalDate dateRate) {
        this.user = user;
        this.cours = cours;
        this.note = note;
        this.dateRate = dateRate;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUser() { return user; }
    public void setUser(int user) { this.user = user; }

    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public LocalDate getDateRate() { return dateRate; }
    public void setDateRate(LocalDate dateRate) { this.dateRate = dateRate; }
}
