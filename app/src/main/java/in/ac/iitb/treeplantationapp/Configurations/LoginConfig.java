package in.ac.iitb.treeplantationapp.Configurations;

public class LoginConfig {

    public static String URL_SERVER = "192.168.0.103";
    public static String URL_MAIN = "http://" + URL_SERVER + "/treePlantation/";

    public static String LOGIN_URL =  URL_MAIN + "userLogin.php";
    public static String REGISTER_URL = URL_MAIN +  "registerUser.php";

    //Keys for email and password as defined in our $_POST['key'] in login.php

    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PH_NO = "ph_no";
    public static final String KEY_NAME = "name";

    //If server response is equal to this that means login/register is unsuccessful
    public static final String LOGIN_FAILURE = "failure";

    //If server response is equal to this that means login/register is successful
    public static final String LOGIN_SUCCESS = "Success";
    public static final String REGISTER_SUCCESS = "Success";

    //Keys for Sharedpreferences
    //This would be the name of our shared preferences
    public static final String SHARED_PREF_NAME = "myloginapp";

    //This would be used to store the email of current logged in user
    public static final String USERNAME_SHARED_PREF = "username";
    public static final String NAME_SHARED_PREF = "name";

    //This would be used to store the email of current logged in user
    //public static final String USER_ID_SHARED_PREF = "id";

    //We will use this to store the boolean in sharedpreference to track user is loggedin or not
    public static final String LOGGEDIN_SHARED_PREF = "loggedin";

    //Name of the JSON array created by server in the response
    public static final String JSON_ARRAY = "result";

    public static void setUrlServer(String url){
        URL_SERVER = url;
        URL_MAIN = "http://" + url + "/treePlantation/";
        LOGIN_URL =  URL_MAIN + "userLogin.php";
        REGISTER_URL = URL_MAIN+  "registerUser.php";
        NearbyTreeConfig.setUrlMain(URL_MAIN);
        PlantTreeConfig.setUrlMain(URL_MAIN);
    }

}
