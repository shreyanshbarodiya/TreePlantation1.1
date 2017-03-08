package in.ac.iitb.treeplantationapp.Configurations;


public class PlantTreeConfig {
    private static String URL_MAIN = LoginConfig.URL_MAIN;

    public static String PLANT_TREE_URL = URL_MAIN + "plantTree.php";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_DATE = "planted_on";
    public static final String KEY_SPECIES = "species";
    public static final String KEY_TREE_ID = "tree_id";

    public static final String PLANTED_FAILURE = "failure";

    public static final String PLANTED_SUCCESS = "Success";

}
