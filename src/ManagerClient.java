/**
 * Starting point of the application
 */

import Clients.DDOClient;
import Clients.LVLClient;
import Clients.MTLClient;
import Log.Log;

import java.util.Scanner;

public class ManagerClient {
    private static Log LogObject;

    public static boolean check_valid_managerID(String input, String prefix) {
        if ((!(prefix.equals("MTL") || prefix.equals("LVL") || prefix.equals("DDO"))) || input.length() != 7) {
            return false;
        }
        boolean check = hasAlpha(input.substring(3, input.length()));
        return !check;
    }

    public static boolean hasAlpha(String input) {
        char[] chars = input.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c)) {
                return true;
            }
        }
        return false;

    }

    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter manager ID");
        String input_string = in.next();  //input string
        String prefix = "";
        if (input_string.length() > 3) {
            prefix = input_string.substring(0, 3);  //substring containing first 3 characters
        }

        while (!check_valid_managerID(input_string, prefix)) {
            System.out.println("Manager ID not valid." + " Insert a valid ManagerID");
            input_string = in.next();
            if (input_string.length() > 3) {
                prefix = input_string.substring(0, 3);
            }
        }
        LogObject = new Log(
                "Logs/"
                        + input_string + ".txt");
        LogObject.logger.info(input_string + " has logged in the " + prefix + " Server");

        if (prefix.equals("MTL")) {
            MTLClient mtlc = new MTLClient();
            mtlc.run(input_string, LogObject, args);
        } else if (prefix.equals("LVL")) {
            LVLClient lvlc = new LVLClient();
            lvlc.run(input_string, LogObject, args);
        } else if (prefix.equals("DDO")) {
            DDOClient ddoc = new DDOClient();
            ddoc.run(input_string, LogObject, args);
        } else {
            System.out.println("Wrong ID");
        }
    }
}
