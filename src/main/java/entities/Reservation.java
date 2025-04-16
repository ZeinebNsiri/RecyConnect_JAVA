package entities;

public class Reservation {
    private int id;
    private int eventId;
    private String nom;
    private String email;
    private String numTel;
    private int nbPlaces;
    private String demandes_speciales;
    private String status;

    public Reservation() {}

    public Reservation(int id, int eventId, String nom, String email, String numTel, int nbPlaces, String demandes_speciales, String status) {
        this.id = id;
        this.eventId = eventId;
        this.nom = nom;
        this.email = email;
        this.numTel = numTel;
        this.nbPlaces = nbPlaces;
        this.demandes_speciales = demandes_speciales;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNumTel() { return numTel; }
    public void setNumTel(String numTel) { this.numTel = numTel; }

    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }

    public String getdemandes_speciales() { return demandes_speciales; }
    public void setdemandes_speciales(String demandes_speciales) { this.demandes_speciales = demandes_speciales; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
