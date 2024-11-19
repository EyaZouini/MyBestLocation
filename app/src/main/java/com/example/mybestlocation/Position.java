package com.example.mybestlocation;

public class Position {
    int idposition;
    String longitude,latitude,pseudo;

    public Position(int idposition, String pseudo, String longitude, String latitude) {
        this.idposition = idposition;
        this.pseudo = pseudo;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Position(String longitude, String latitude, String pseudo) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.pseudo = pseudo;
    }

    @Override
    public String toString() {
        return "Position{" +
                "idposition=" + idposition +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", pseudo='" + pseudo + '\'' +
                '}';
    }
}
