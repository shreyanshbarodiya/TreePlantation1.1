package in.ac.iitb.treeplantationapp.Models;


public class ReviewModel {
    String tree_id, review_text, reviewed_by, reviewed_on, title;
    int review_no;
    Double review_stars;

    public Double getReview_stars() {
        return review_stars;
    }

    public void setReview_stars(Double review_stars) {
        this.review_stars = review_stars;
    }

    public ReviewModel(String tree_id, String review_text, String reviewed_by, String reviewed_on, String title, int review_no, Double review_stars ) {
        this.tree_id = tree_id;
        this.review_text = review_text;
        this.reviewed_by = reviewed_by;
        this.reviewed_on = reviewed_on;
        this.title = title;
        this.review_no = review_no;
        this.review_stars = review_stars;
    }

    public String getTree_id() {
        return tree_id;
    }

    public void setTree_id(String tree_id) {
        this.tree_id = tree_id;
    }

    public String getReview_text() {
        return review_text;
    }

    public void setReview_text(String review_text) {
        this.review_text = review_text;
    }

    public String getReviewed_by() {
        return reviewed_by;
    }

    public void setReviewed_by(String reviewed_by) {
        this.reviewed_by = reviewed_by;
    }

    public String getReviewed_on() {
        return reviewed_on;
    }

    public void setReviewed_on(String reviewed_on) {
        this.reviewed_on = reviewed_on;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReview_no() {
        return review_no;
    }

    public void setReview_no(int review_no) {
        this.review_no = review_no;
    }
}
