package config;

public class Config {
	public static boolean DEBUG = true;
	public static String PATH_UPLOAD = "_UPLOAD_";
	public static String HOST_PI = "localhost";
	public static int PORT_PI = 2121;
	public static int PORT_DTP = 2020;

	public static String RETRIEVING_DATA = "Retrieving data...";

	public static String DATABASE_HOST = "localhost";
	public static int DATABASE_PORT = 3306;
	public static String DATABASE_USER = "root";
	public static String DATABASE_PASS = "";
	public static String DATABASE_DB = "dut__dacsnm";

	public static void print(String str) {
		if (DEBUG == true) {
			System.out.println(">> DEBUG: " + str);
		}
	}
}
