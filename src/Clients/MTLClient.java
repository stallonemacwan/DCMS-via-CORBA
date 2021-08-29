package Clients;

import DCMSApp.DCMS;
import DCMSApp.DCMSHelper;
import Log.Log;
import ServerImplementation.MTLClass;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Scanner;

public class MTLClient {

    public boolean hasNumbers(String input) {
        char[] chars = input.toCharArray();
        for (char c : chars) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public boolean validRecordID(String ID) {
        String firstTwoLetters = Character.toString(ID.charAt(0)) + Character.toString(ID.charAt(1));
        for (int i = 2; i < ID.length(); i++) {
            if (hasAlpha(Character.toString(ID.charAt(i)))) {
                return false;
            }
        }
        if ((firstTwoLetters.equals("TR") || firstTwoLetters.equals("SR")) && ID.length() == 7) {
            return true;
        }
        return false;

    }

    public boolean dateFormatChecker(String statusdate) {
        if (statusdate.length()!=10)
            return true;
        String month1 = Character.toString(statusdate.charAt(3)) + Character.toString(statusdate.charAt(4));
        int month = Integer.parseInt(month1);
        if (month > 12) {
            return true;
        }
        return false;
    }

    public boolean hasAlpha(String input) {
        char[] chars = input.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c)) {
                return true;
            }
        }
        return false;

    }

    public void run(String managerID, Log LogObject, String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            DCMS mtl = DCMSHelper.narrow(ncRef.resolve_str("MTL"));
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("----- Select which operation you want to perform -----");
                System.out.println("1. Creating a teacher record (TR)");
                System.out.println("2. Creating a student record (SR)");
                System.out.println("3. Edit record");
                System.out.println("4. Display all records");
                System.out.println("5. Displaying a record");
                System.out.println("6. Display total Record count of all servers.");
                System.out.println("7. Transfer records.");
                System.out.println("8. Exit.");
                int ch;
                ch = sc.nextInt();

                switch (ch) {
                    case 1:
                        LogObject.logger.info("The Manager " + managerID + " attempting to create a new Teacher Record");
                        System.out.println("Enter First name:");
                        String fname = sc.next();
                        while (hasNumbers(fname)) {
                            System.out.println("A name can not contain numbers, please insert valid input.");
                            fname = sc.next();
                        }
                        System.out.println("Enter Last name:");
                        String lname = sc.next();
                        while (hasNumbers(lname)) {
                            System.out.println("A name can not contain numbers, please insert valid input.");
                            lname = sc.next();
                        }
                        System.out.println("Enter Address:");
                        sc.nextLine();
                        String add = sc.nextLine();
                        while (add == null || add.equals("")) {
                            System.out.println("Address can not be null, please insert valid input.");
                            add = sc.next();
                        }
                        System.out.println("Enter Phone number in the format (514-888-9999):");
                        String ph = sc.nextLine();
                        while (ph.length() != 12 || hasAlpha(ph)) {
                            System.out.println("Invalid phone number");
                            System.out.println("Try another");
                            ph = sc.nextLine();
                        }
                        System.out.println("Specialization courses:");
                        String spc = sc.nextLine();
                        mtl.createTRecord(fname, lname, add, ph, spc, "MTL", managerID);
                        LogObject.logger.info("The Manager " + managerID + " successfully created a new Teacher Record");

                        break;
                    case 2:
                        LogObject.logger.info("The Manager " + managerID + " attempting to create a new Student Record");
                        System.out.println("Enter First name:");
                        String firstName = sc.next();
                        while (hasNumbers(firstName)) {
                            System.out.println("A name can not contain numbers, please insert valid input.");
                            firstName = sc.next();
                        }
                        System.out.println("Enter Last name:");
                        String lastName = sc.next();
                        while (hasNumbers(lastName)) {
                            System.out.println("A name can not contain numbers, please insert valid input.");
                            lastName = sc.next();
                        }
                        System.out.println("Name courses the student is registered in:");
                        sc.nextLine();
                        String CourseRegistered = sc.nextLine();
                        System.out.println("Enter Status of the student:");
                        String status = sc.next();
                        while (!(status.equals("active") || status.equals("inactive") || status.equals("Active") || status.equals("Inactive"))) {
                            System.out.println("Status inserted is not accepted, please enter (active or inactive).");
                            status = sc.next();
                        }
                        System.out.println("Enter status date: (format - dd/mm/yyyy)");
                        String statusDate = sc.next();
                        while (hasAlpha(statusDate) || !((Character.toString(statusDate.charAt(2))).equals("/") ||
                                (Character.toString(statusDate.charAt(5))).equals("/")) || dateFormatChecker(statusDate)) {
                            System.out.println("Invalid date format, please insert again in this format dd/mm/yyyy");
                            statusDate = sc.next();
                        }
                        mtl.createSRecord(firstName, lastName, CourseRegistered, status, statusDate, managerID);
                        LogObject.logger.info("The Manager " + managerID + " successfully created a new Student Record");
                        break;
                    case 3:
                        LogObject.logger.info("The Manager " + managerID + " attempting to edit a Record");
                        System.out.println("Enter the recordID of the record you want to edit:");
                        String recID = sc.next();
                        while (!(validRecordID(recID))) {
                            System.out.println("Invalid recordID, insert a valid ID");
                            recID = sc.next();
                        }
                        if (recID.startsWith("SR")) {
                            System.out.println("Enter name of the field you want to change:" + "\n" + "Enter in this format: " +
                                    "courses_registered,status, status_date");
                        }
                        if (recID.startsWith("TR")) {
                            System.out.println("Enter name of the field you want to change:" + "\n" + "Enter in this format: " +
                                    "address, phone, specialization, location");
                        }
                        String field = sc.next();
                        while (!(field.equals("courses_registered") || field.equals("status") || field.equals("status_date")
                                || field.equals("address") || field.equals("phone") || field.equals("specialization")
                                || field.equals("location"))) {
                            System.out.println("Field name not properly mentioned, please write as provided above");
                            field = sc.next();
                        }
                        System.out.println("What value you want to change to:");
                        sc.nextLine();
                        String value = sc.nextLine();
                        System.out.println(value);
                        switch (field) {
                            case ("address"):
                                while (value == null || value.equals("")) {
                                    System.out.println("Address can not be null, please insert valid input.");
                                    value = sc.nextLine();
                                }
                                break;
                            case ("phone"):
                                while (value.length() != 12 || hasAlpha(value)) {
                                    System.out.println("Invalid phone number");
                                    System.out.println("Try another");
                                    value = sc.next();

                                }
                                break;
                            case ("location"):
                                while (!(value.equals("MTL") || value.equals("LVL") || value.equals("DDO"))) {
                                    System.out.println("Invalid location entered");
                                    System.out.println("Try another");
                                    value = sc.next();
                                }
                                break;
                            case ("status"):
                                while (!(value.equals("active") || value.equals("inactive") || value.equals("Active") || value.equals("Inactive"))) {
                                    System.out.println("Status inserted is not accepted, please enter (active or inactive).");
                                    value = sc.next();

                                }
                                break;

                            case ("status_date"):
                                while (hasAlpha(value) || !((Character.toString(value.charAt(2))).equals("/") ||
                                        (Character.toString(value.charAt(5))).equals("/")) || dateFormatChecker(value)) {
                                    System.out.println("Invalid date format, please insert again in this format dd/mm/yyyy");
                                    value = sc.next();

                                }
                                break;
                            default:
                                break;

                        }
                        mtl.editRecord(recID, field, value, managerID);
                        LogObject.logger.info("The Manager " + managerID + " successfully edited the Record " + recID);
                        break;
                    case 4:
                        LogObject.logger.info("The Manager " + managerID + " displayed all Records");
                        System.out.println(mtl.displayAllRecords(managerID));
                        break;
                    case 5:
                        System.out.println("Enter the recordID of the record to display:");
                        String recordID = sc.next();
                        while (!(validRecordID(recordID))) {
                            System.out.println("Invalid recordID, insert a valid ID");
                            recordID = sc.next();
                        }
                        System.out.println(mtl.displayRecord(recordID, managerID));
                        LogObject.logger.info("The Manager " + managerID + " displayed the Record " + recordID);
                        break;
                    case 6:
                        LogObject.logger.info("The Manager " + managerID + " attempting to retrieve Record count over all the servers");
                        System.out.println(mtl.getRecordCounts(managerID));
                        LogObject.logger.info("The Manager " + managerID + " retrieved Record count of all the servers");
                        break;
                    case 7:
                        LogObject.logger.info("The Manager " + managerID + " attempting to transfer a Record");
                        System.out.println("Enter the recordID of the record to transfer:");
                        String recorID = sc.next();
                        while (!(validRecordID(recorID))) {
                            System.out.println("Invalid recordID, insert a valid ID");
                            recorID = sc.next();
                        }

                        System.out.println("Enter the center server name:");
                        String centServer = sc.next();
                        while (!(centServer.equals("MTL")|| centServer.equals("LVL")||centServer.equals("DDO")))
                        {
                            System.out.println("Invalid Server Name mentioned");
                            centServer = sc.next();
                        }
                        System.out.println(mtl.transferRecord(recorID, centServer, managerID));
                        if (centServer.equals("DDO") || centServer.equals("LVL")) {
                            LogObject.logger.info("The Manager " + managerID + " transferred the Record " + recorID + " to server " + centServer);
                        } else if (centServer.equals("MTL")) {
                            System.out.println("Cannot transfer to the same server");
                            LogObject.logger.info("The Manager " + managerID + " attempted to transfer the Record " + recorID + " to itself and it failed");
                        }
                        break;
                    case 8:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Wrong choice");
                        break;
                }

            }

        } catch (Exception e) {
            System.out.println("Exception!");
            e.printStackTrace();
        }

    }
}
