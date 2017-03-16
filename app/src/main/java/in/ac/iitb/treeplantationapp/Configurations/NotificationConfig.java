package in.ac.iitb.treeplantationapp.Configurations;

public class NotificationConfig {
    private static String URL_MAIN = LoginConfig.URL_MAIN + "notificationHandler/";
    public static String URL_REGISTER_DEVICE = URL_MAIN + "RegisterDevice.php";
    public static String URL_SEND_SINGLE_PUSH = URL_MAIN + "sendSinglePush.php";
    public static String URL_SEND_MULTIPLE_PUSH = URL_MAIN + "sendMultiplePush.php";
    public static String URL_FETCH_DEVICES = URL_MAIN + "GetRegisteredDevices.php";


    public static void setUrlMain(String url) {
        URL_MAIN = url + "notificationHandler/" ;
        URL_REGISTER_DEVICE = URL_MAIN + "RegisterDevice.php";
        URL_SEND_SINGLE_PUSH = URL_MAIN + "sendSinglePush.php";
        URL_SEND_MULTIPLE_PUSH = URL_MAIN + "sendMultiplePush.php";
        URL_FETCH_DEVICES = URL_MAIN + "GetRegisteredDevices.php";
    }
}