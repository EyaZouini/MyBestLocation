package com.example.mybestlocation;

public class Position {
    int idposition;
    String longitude, latitude, pseudo, type;

    // Constructeur avec tous les champs
    public Position(int idposition, String pseudo, String longitude, String latitude, String type) {
        this.idposition = idposition;
        this.pseudo = pseudo;
        this.longitude = longitude;
        this.latitude = latitude;
        this.type = type;
    }

    // Constructeur sans l'ID (par exemple pour une insertion)
    public Position(String longitude, String latitude, String pseudo, String type) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.pseudo = pseudo;
        this.type = type;
    }

    // Getters et Setters
    public int getIdposition() {
        return idposition;
    }

    public void setIdposition(int idposition) {
        this.idposition = idposition;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getType() {
        return type; // Getter pour 'type'
    }

    public void setType(String type) {
        this.type = type; // Setter pour 'type'
    }

    @Override
    public String toString() {
        return pseudo + " (" + type + ")"; // Inclure 'type' dans la représentation textuelle
    }
}
