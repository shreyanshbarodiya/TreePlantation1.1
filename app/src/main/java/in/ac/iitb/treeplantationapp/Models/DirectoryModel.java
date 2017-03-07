package in.ac.iitb.treeplantationapp.Models;


public class DirectoryModel {
    private String directory_id, tree_id, uploaded_by, clicked_on;

    public DirectoryModel(String directory_id, String tree_id, String uploaded_by, String clicked_on) {
        this.directory_id = directory_id;
        this.tree_id = tree_id;
        this.uploaded_by = uploaded_by;
        this.clicked_on = clicked_on;
    }

    public String getDirectory_id() {
        return directory_id;
    }

    public void setDirectory_id(String directory_id) {
        this.directory_id = directory_id;
    }

    public String getTree_id() {
        return tree_id;
    }

    public void setTree_id(String tree_id) {
        this.tree_id = tree_id;
    }

    public String getUploaded_by() {
        return uploaded_by;
    }

    public void setUploaded_by(String uploaded_by) {
        this.uploaded_by = uploaded_by;
    }

    public String getClicked_on() {
        return clicked_on;
    }

    public void setClicked_on(String clicked_on) {
        this.clicked_on = clicked_on;
    }
}
