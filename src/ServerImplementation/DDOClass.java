package ServerImplementation;
/**
 * A class that implements the CenterServer
 * All the funcions/operations available to clients are created here.
 */
import DCMSApp.DCMSPOA;
import Log.Log;
import Record.Record;
import Record.StudentRecord;
import Record.TeacherRecord;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import org.omg.CORBA.ORB;


public class DDOClass extends DCMSPOA {

    private ORB orb = null;

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    Log serverLog = null; //object for logging events of a given server
    private String serverName;  //server name MTL, LVL or DDO
    private int studentRecNum;            //number used to generate record IDs
    private int teacherRecNum;            //number used to generate record IDs
    private int recordCount = 0; //total count of the records created on the server
    private HashMap<String, ArrayList<Record>> recordHashMap;  //HashMap which stores records
    Semaphore semaphore = new Semaphore(1);            //ensure atomic access to the HashMap


    /**
     * @param centName name of the center LVL DDO or MTL
     * @param centNum  number used for creating record IDs
     * @throws SecurityException
     * @throws IOException
     */
    public DDOClass(String centName, int centNum) throws SecurityException, IOException {

        super();
        recordHashMap = new HashMap<String, ArrayList<Record>>();
        studentRecNum = teacherRecNum = centNum;  //setting up the numbers used to create record IDs
        serverName = centName;                //setting server name
        setUpHashMap();                        //Initializing HashMap
        serverLog = new Log(serverName + ".txt");  //Initializing log object

        Thread t = new Thread(new Runnable() {    //running thread which will publish record counts using UDP/sockets
            public void run() {
                sendRecordCount();
            }
        });
        Thread t2 = new Thread(new Runnable(){	//running thread which will receive records from other servers
            public void run(){
                recieveRecord();
            }
        });
        t.start();
        t2.start();
        serverLog.logger.info(centName + " server in running"); //log of server running
    }

    /**
     * method to validate record ID
     * @param ID id to the record
     * @return validation result
     */
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

    /**
     * method to check numeric value
     * @param input value of input
     * @return result of validation
     */
    public boolean hasNumbers(String input) {
        char[] chars = input.toCharArray();
        for (char c : chars) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * method to validate date
     * @param statusdate date input
     * @return result of validation
     */
    public boolean dateFormatChecker(String statusdate) {
        String month1 = Character.toString(statusdate.charAt(3)) + Character.toString(statusdate.charAt(4));
        int month = Integer.parseInt(month1);
        if (month > 12) {
            return true;
        }
        return false;
    }

    /**
     * method to check alphabetical input
     * @param input string input
     * @return result of validation
     */
    public boolean hasAlpha(String input) {
        char[] chars = input.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c)) {
                return true;
            }
        }
        return false;

    }


    /**
     * methods used to initialize HashMap
     */
    private void setUpHashMap(){		//sets up hash map in which the keys are the letters of the alphabet (capital)
        Character a = new Character('A');
        for(int i = 0; i <= 25; i++){  // A --> Z each getting an ArrayList
            recordHashMap.put(Character.toString(a), new ArrayList<Record>());  //adding ArrayLists to HashMap (values)
            a++;
        }
    }


    /**
     * Method used to create Teacher record
     * @param firstName first name of teacher
     * @param lastName last name of teacher
     * @param address address of teacher
     * @param phone phone number of teacher
     * @param specialization specialization fields of teacher
     * @param location location of teacher
     * @param manID managerID
     * @return result of creating record
     * @throws RemoteException
     */
    public synchronized String createTRecord(String firstName, String lastName, String address, String phone, String specialization,
                                             String location, String manID) {

        if (!(location.equals("LVL") || location.equals("MTL") || location.equals("DDO"))) {     //ensuring that location is valid
            serverLog.logger.info(manID + " has failed in creating a teacher record" + " invalid location inserted");        //log invalid record creation
            return "Teacher record creation failed!";
        }  //return message for client
        if (firstName == null || firstName.equals("") || hasNumbers(firstName)) {
            serverLog.logger.info(manID + " has failed in creating a teacher record" + " invalid first name");        //log invalid record creation
            return "Teacher record creation failed!";
        }
        if (lastName == null || lastName.equals("") || hasNumbers(lastName)) {
            serverLog.logger.info(manID + " has failed in creating a teacher record" + " invalid last name");        //log invalid record creation
            return "Teacher record creation failed!";
        }
        if (address == null || address.equals("")) {
            serverLog.logger.info(manID + " has failed in creating a teacher record" + " invalid address");        //log invalid record creation
            return "Teacher record creation failed!";
        }
        if (phone == null || phone.equals(" ") || hasAlpha(phone) || phone.length()!=12) {
            serverLog.logger.info(manID + " has failed in creating a teacher record" + " invalid phone number");        //log invalid record creation
            return "Teacher record creation failed!";
        }

        String recID = "TR" + teacherRecNum;   //creating record ID
        TeacherRecord t = new TeacherRecord(recID, firstName, lastName, address, phone, specialization, location);  //Teacher record constructor
        String surname = Character.toString(lastName.charAt(0)).toUpperCase();
        ArrayList<Record> temp = recordHashMap.get(surname);//get first character of last name for placing record in HashMap
        temp.add(t);        //add record into ArrayList from HashMap
        teacherRecNum++;        //increment number
        recordCount++;        //increment record count
        semaphore.release();    //release mutex
        serverLog.logger.info(manID + " created a teacher record ID:" + recID);    //log record creation on server
        return recID; //if success --> return user the new record ID
    }


    /**
     * Method used to create student record
     * @param firstName first name of student
     * @param lastName last name of student
     * @param courseRegistered courses in which student is registered
     * @param status status of student
     * @param statusDate status date of student
     * @param manID managerID
     * @return result of creating record
     * @throws RemoteException
     */
    public synchronized String createSRecord(String firstName, String lastName, String courseRegistered, String status, String statusDate,
                                             String manID) {

        if (!(status.equals("active") || status.equals("inactive") || status.equals("Active") || status.equals("Inactive"))) {
            serverLog.logger.info(manID + " has failed in creating a student record" + " invalid status inserted");
            return "Student record creation failed!";
        }
        if (firstName == null || firstName.equals("") || hasNumbers(firstName)) {
            serverLog.logger.info(manID + " has failed in creating a student record" + " invalid first name");        //log invalid record creation
            return "Student record creation failed!";
        }
        if (lastName == null || lastName.equals("") || hasNumbers(lastName)) {
            serverLog.logger.info(manID + " has failed in creating a student record" + " invalid last name");        //log invalid record creation
            return "Student record creation failed!";
        }
        if (hasAlpha(statusDate) || !((Character.toString(statusDate.charAt(2))).equals("/") ||
                (Character.toString(statusDate.charAt(5))).equals("/")) || dateFormatChecker(statusDate)) {
            serverLog.logger.info(manID + " has failed in creating a student record" + " invalid status date format");        //log invalid record creation
            return "Student record creation failed!";
        }

        String recID = "SR" + studentRecNum;
        StudentRecord s = new StudentRecord(recID, firstName, lastName, courseRegistered, status, statusDate);
        String surname = Character.toString(lastName.charAt(0)).toUpperCase();
        ArrayList<Record> temp = recordHashMap.get(surname);
        temp.add(s);
        studentRecNum++;
        recordCount++;
        semaphore.release();


        serverLog.logger.info(manID + " created a student record ID:" + recID);
        return recID;  //return "Operation succeeded
    }


    /**
     * method which gets record counts from other servers (using udp)
     * @param manID managerID
     */
    public String getRecordCounts(String manID) {

        int recieve1address = 0; //variables to be used for receiving packets from other servers
        int recieve2address = 0;
        String response = null;
        DatagramSocket aSocket1 = null;            //sockets to be used later
        DatagramSocket aSocket2 = null;
        byte[] buffer1 = new byte[200];            //byte arrays for DatagramPackets
        byte[] buffer2 = new byte[200];

        switch (serverName) {
            case ("MTL"):
                recieve1address = 7003;
                recieve2address = 7006;
                break;
            case ("LVL"):
                recieve1address = 7001;
                recieve2address = 7005;
                break;
            case ("DDO"):
                recieve1address = 7002;
                recieve2address = 7004;
                break;
            default:
                System.out.println("No server specifications available for " + serverName);
                System.exit(1);
        }

        try {

            aSocket2 = new DatagramSocket(recieve2address);
            aSocket1 = new DatagramSocket(recieve1address);
            DatagramPacket packet1 = new DatagramPacket(buffer1, buffer1.length);
            DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length);
            aSocket1.receive(packet1);
            aSocket2.receive(packet2);
            semaphore.acquire();        //semaphore for ensuring that local records will not be created before client receives record count
            String reply1 = new String(packet1.getData()).trim();
            String reply2 = new String(packet2.getData()).trim();  //getting replies from packets
            response = serverName + " " + this.recordCount + ", ";
            response += reply1 + ", ";
            response += reply2 + ".";

        } catch (Exception e) {
            e.printStackTrace();
        }

        serverLog.logger.info(manID + " has retrieved the total record count");
        semaphore.release();
        return response;
    }


    /**
     * method used to edit records
     * @param recID id of record to edit
     * @param fieldName field name to change
     * @param newValue value to be replaced with
     * @param manID manager ID
     */
    public String editRecord(String recID, String fieldName, String newValue, String manID) {

        try {
            semaphore.acquire();        //semaphore to prevent creation/editing of records while this function is active
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!(validRecordID(recID))) {
            serverLog.logger.info(manID + " has failed at editing a record");
            return "Record editing failed" +" (invalid record ID)";
        }
        Record temp = findRecord(recID);
        if (temp == null) {
            serverLog.logger.info(manID + " has failed at editing a record");
            semaphore.release();
            return "Student record editing failed! (record not found)";
        }

        if (recID.startsWith("TR")) {

            TeacherRecord tempT = (TeacherRecord) temp;


            switch (fieldName) {

                case ("address"):
                    tempT.setAddress(newValue);
                    serverLog.logger.info(manID + " edited a Teacher record ID:" + recID);
                    semaphore.release();
                    return "Teacher record has been edited ID" + recID;
                case ("phone"):
                    tempT.setPhone(newValue);
                    serverLog.logger.info(manID + " edited a Teacher record ID:" + recID);
                    semaphore.release();
                    return "Teacher record has been edited ID" + recID;
                case ("specialization"):
                    tempT.setSpecialization(newValue);
                    serverLog.logger.info(manID + " edited a Teacher record ID:" + recID);
                    semaphore.release();
                    return "Teacher record has been edited ID" + recID;
                case ("location"):
                    if (!(newValue.equals("MTL") || newValue.equals("LVL") || newValue.equals("DDO"))) {
                        serverLog.logger.info(manID + " has failed at editing a Teacher record");
                        semaphore.release();
                        return "Teacher record editing failed! (invalid new value)";
                    } else {
                        tempT.setLocation(newValue);
                        serverLog.logger.info(manID + " edited a Teacher record ID:" + recID);
                        semaphore.release();
                        return null;
                    }
                default:
                    serverLog.logger.info(manID + " has failed at editing a Teacher record ");
                    semaphore.release();
                    return "Teacher record editing failed! (invalid field name)";
            }

        } else if (recID.startsWith("SR")) {
            StudentRecord tempS = (StudentRecord) temp;

            switch (fieldName) {

                case ("courses_registered"):
                    tempS.setCoursesRegistered(newValue);
                    serverLog.logger.info(manID + " edited a student record ID:" + recID);
                    semaphore.release();
                    return "Student record has been edited ID" + recID;
                case ("status"):
                    if (!(newValue.equals("active") || newValue.equals("inactive") || newValue.equals("Active") || newValue.equals("Inactive"))) {
                        serverLog.logger.info(manID + " has failed at editing a student record (invalid new value)");
                        semaphore.release();
                        return "student record editing failed! (invalid new value)";
                    } else {
                        tempS.setStatus(newValue);
                        serverLog.logger.info(manID + " edited a student record ID:" + recID);
                        semaphore.release();
                        return "student record has been edited ID" + recID;
                    }
                case ("status_date"):
                    tempS.setStatusDate(newValue);
                    serverLog.logger.info(manID + " edited a student record ID:" + recID);
                    semaphore.release();
                    return "Student record has been edited ID" + recID;
                default:
                    serverLog.logger.info(manID + " has failed at editing a student record (invalid field name)");
                    semaphore.release();
                    return "Student record editing failed! (invalid field name)";
            }
        } else {
            serverLog.logger.info(manID + " has failed at editing a record (invalid record ID)");
            semaphore.release();
            return "Record editing failed! (invalid record ID)";
        }

    }


    /**
     * method used for displaying all of the records on a given server
     * @param manID manager ID
     */
    public synchronized String displayAllRecords(String manID) {

        String output = "";
        for (Entry<String, ArrayList<Record>> a : recordHashMap.entrySet()) {

            if (a.getValue().isEmpty()) continue;
            ArrayList<Record> b = a.getValue();
            for (Record r : b) {
                output += r.toString();
                output += "\n";
            }
        }

        serverLog.logger.info(manID + " has displayed all records");  //log of activity
        semaphore.release();
        return output;
    }


    /**
     * method used to display specific record with details
     * @param recID ID of record to be displayed
     * @param manID manager ID
     */
    public String displayRecord(String recID,String manID) {

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        if (!(validRecordID(recID))) {
            serverLog.logger.info(manID + " has failed at displaying a record");
            return "Record displaying failed" +" (invalid record ID)";
        }

        Record temp = findRecord(recID);
        if (temp == null) {
            serverLog.logger.info(manID + " has failed at displaying record (record not found)");
            semaphore.release();
            return "Record not found";
        }

        if (recID.startsWith("TR")) {
            TeacherRecord tempT = (TeacherRecord) temp;
            serverLog.logger.info(manID + " has displayed record " + recID);
            semaphore.release();
            return tempT.toStringT();
        } else if (recID.startsWith("SR")) {
            StudentRecord tempS = (StudentRecord) temp;
            serverLog.logger.info(manID + " has displayed record " + recID);
            semaphore.release();
            return tempS.toStringS();
        }
        return null;
    }


    /**
     * method used to send record count in server
     */
    private void sendRecordCount() {

        int reply1address = 0;        //addresses for sending out info
        int reply2address = 0;
        int socketNum = 0;    //socket for sending

        switch (serverName) {
            case ("MTL"):
                socketNum = 6789;
                reply1address = 7001;
                reply2address = 7002;
                break;
            case ("LVL"):
                socketNum = 6790;
                reply1address = 7003;
                reply2address = 7004;
                break;
            case ("DDO"):
                socketNum = 6791;
                reply1address = 7005;
                reply2address = 7006;
                break;
            default:
                System.out.println("No server specifications available for " + serverName);
                System.exit(1);

        }

        DatagramSocket aSocket = null;    //socket for sending

        try {

            aSocket = new DatagramSocket(socketNum);

            while (true) {            //infinite loop (on thread) for publishing data
                semaphore.acquire();    //no new records will be added while data is being prepared
                String replyString = serverName + " " + recordCount;        //string which is to be sent
                byte[] replyByte = replyString.getBytes();                    //convert to byte array
                DatagramPacket reply1 = new DatagramPacket(replyByte, replyByte.length, InetAddress.getLocalHost(), reply1address);    //packet for sending
                DatagramPacket reply2 = new DatagramPacket(replyByte, replyByte.length, InetAddress.getLocalHost(), reply2address);
                aSocket.send(reply1);
                aSocket.send(reply2);        //sending packets
                semaphore.release();            //release semaphore
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
        }

    }


    /**
     * function used to extract a record from the HashMap using record ID
     */
    private Record findRecord(String recID) {

        char a = 'A';
        for (int i = 0; i <= 24; i++) {
            String temp1 = Character.toString(a);
            ArrayList<Record> temp = recordHashMap.get(temp1);
            if (temp!=null){
            for (i = 0; i < temp.size(); i++) {
                if (temp.get(i).getRecordID().equals(recID)) {
                    return temp.get(i);
                }
            }}
            else
            {
                return null;
            }
            a++;
        }
        return null;
    }

    /**
     * function which transfers Record from one remote obj to another
     */
    @Override
    public String transferRecord(String recID, String toServer, String manID) {

        int sendAddress = 0;
        int recieveaddress = 0;
        DatagramSocket socket = null;
        byte[]buffer = new byte[1024];
        ByteArrayOutputStream byArrOStream = new ByteArrayOutputStream(); //stream to output object as byte array
        ObjectOutputStream objOStream = null;

        try {
            objOStream = new ObjectOutputStream(byArrOStream);  //connect object stream to byte array
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if(toServer.equals(this.serverName)){
            serverLog.logger.info(manID + " failed to transfer " + recID);
            return "Transfer failed";
        }

        switch(serverName){
            case("DDO"):
                sendAddress = 9005;
                break;
            default:
                System.out.println("Invalid server");
                serverLog.logger.info(manID + " failed to transfer " + recID);
                return "Transfer failed";
        }

        switch(toServer){
            case("MTL"):
                recieveaddress = 9003;
                break;
            case("LVL"):
                recieveaddress = 9001;
                break;
            case("DDO"):
                System.out.println("Cannot transfer to the same server");
                break;
            default:
                System.out.println("Invalid server mentioned");
                serverLog.logger.info(manID + " has failed to transfer " + recID);
                return "transfer failed";
        }

        Record temp = findRecord(recID);

        if(temp == null){
            serverLog.logger.info(manID + " has failed to transfer " + recID);
            return "Record not found";
        }

        try {

            semaphore.acquire();
            objOStream.writeObject(temp);
            buffer = byArrOStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), recieveaddress);
            socket = new DatagramSocket(sendAddress);
            socket.send(sendPacket);
            removeRecord(recID);
            recordCount--;

        } catch (Exception e) {
            e.printStackTrace();
        }

        serverLog.logger.info(recID + "has been transferred to" + toServer + "by " + manID);
        semaphore.release();
        return "Record transferred";
    }

    @Override
    public void shutdown() {
        orb.shutdown(false);
    }

    /**
     * function to receive the record and add to HashMap
     */
    @SuppressWarnings("resource")
    private void recieveRecord(){

        int recieveaddress = 0;
        DatagramSocket aSocket = null;
        byte[]buffer = new byte[1024];

        switch(serverName){
            case("DDO"):
                recieveaddress = 9002;
                break;
            case("LVL"):
                recieveaddress = 9001;
                break;
            case("MTL"):
                recieveaddress = 9003;
                break;
            default:
                System.out.println("No server specifications available for " + serverName);
        }

        try {
            aSocket = new DatagramSocket(recieveaddress);
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

        String recID = null;

        while(true){

            try {

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(packet);

                semaphore.acquire();
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                ObjectInputStream is = new ObjectInputStream(in);
                Record temp = (Record)is.readObject();

                ArrayList<Record> arr = recordHashMap.get(Character.toString(temp.getLastName().charAt(0)));
                arr.add(temp);
                recordCount++;
                recID = temp.getRecordID();

            }catch (Exception e) {
                e.printStackTrace();
            }

            serverLog.logger.info(recID + " has been added to the mentioned server");
            semaphore.release();
        }
    }


    /**
     * function to remove the record from HashMap
     * @param recID record ID of record to be removed
     */
    private void removeRecord(String recID){

        Character a = new Character('A');

        for(int i = 0; i <= 25; i++){
            ArrayList<Record> dummy = recordHashMap.get(Character.toString(a));
            for(int x = 0; x < dummy.size(); x++){
                if(dummy.get(x).getRecordID().equals(recID)){dummy.remove(x);}
            }
            a++;
        }
    }
}

