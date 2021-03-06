package in.ac.iitb.treeplantationapp.Configurations;


public class NearbyTreeConfig {
    public static String URL_MAIN = LoginConfig.URL_MAIN;

    public static String NEARBY_TREES_URL = URL_MAIN + "getNearbyTrees.php";
    public static String TREE_DETAILS_URL = URL_MAIN + "getTreeDetails.php";
    public static String WRITE_REVIEW_URL = URL_MAIN + "writeReview.php";
    public static String SEE_REVIEWS_URL =  URL_MAIN + "seeReviews.php";
    public static String UPLOAD_URL =  URL_MAIN + "uploadImages.php";
    public static String DIRECTORY_URL = URL_MAIN + "treeDirectory.php";
    public static String IMAGES_URL = URL_MAIN + "getImagesUrl.php";
    public static String ADOPT_TREE_URL = URL_MAIN + "adoptTree.php";


    public static final String KEY_URL_MAIN = "server";

    public static final String KEY_TREE_ID = "tree_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_DATE = "planted_on";
    public static final String KEY_SPECIES = "species";
    public static final String KEY_TITLE = "title";
    public static final String KEY_REVIEW_TEXT = "review_text";
    public static final String KEY_REVIEW_DATE = "reviewed_on";
    public static final String KEY_REVIEW_NO = "review_no";
    public static final String KEY_REVIEW_RATINGS = "review_stars";


    public static final String KEY_ADOPTED_ON = "adopted_on";
    //KEYS FOR UPLOADING IMAGES
    public static final String KEY_NO_OF_IMAGES = "no_images";
    public static final String KEY_IMAGE_NUMBER = "image_number";

    public static final String KEY_CLICKED_ON = "clicked_on";
    //KEYS FOR GALLERY
    public static final String KEY_DIRECTORY_ID = "directory";
    public static final String KEY_IMAGE_URL = "image_url";



    public static final String IMAGES_FAILURE = "failure";
    public static final String KEY_UPLOADED_BY = "uploaded_by";
    public static final String PLANTED_FAILURE = "failure";
    public static final String PLANTED_SUCCESS = "Success";
    public static final String REVIEWED_SUCCESS = "Success";
    public static final String ADOPTED_SUCCESS = "Success";

    public static final String REVIEW_FAILURE = "failure";

    public static final String JSON_ARRAY = "result";

    public static void setUrlMain(String url){
        URL_MAIN = url;
        NEARBY_TREES_URL = url + "getNearbyTrees.php";
        TREE_DETAILS_URL = url + "getTreeDetails.php";
        WRITE_REVIEW_URL = url + "writeReview.php";
        SEE_REVIEWS_URL =  url + "seeReviews.php";
        UPLOAD_URL =  url + "uploadImages.php";
        DIRECTORY_URL = url + "treeDirectory.php";
        IMAGES_URL = url + "getImagesUrl.php";
        ADOPT_TREE_URL = url + "adoptTree.php";
    }
}
