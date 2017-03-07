package in.ac.iitb.treeplantationapp.Models;

public class PlantedTreeModel {
    private String tree_id;
    private String username;
    private double latitude;
    private double longitude;
    private String planted_on;
    private String species;

    public PlantedTreeModel(String tree_id, String username, double latitude, double longitude, String planted_on, String species) {
        this.tree_id = tree_id;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.planted_on = planted_on;
        this.species = species;
    }

    public String getTree_id() {
        return tree_id;
    }

    public void setTree_id(String tree_id) {
        this.tree_id = tree_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlanted_on() {
        return planted_on;
    }

    public void setPlanted_on(String planted_on) {
        this.planted_on = planted_on;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }
}
