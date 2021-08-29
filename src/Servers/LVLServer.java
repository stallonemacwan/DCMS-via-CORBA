package Servers;
/**
 * Server file to initiate LVLServer
 */
import DCMSApp.DCMS;
import DCMSApp.DCMSHelper;

import ServerImplementation.LVLClass;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class LVLServer {

    public static void main(String args[]) throws Exception {
        ORB orb = ORB.init(args, null);
        POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootpoa.the_POAManager().activate();
        final String serverName = "LVL";
        final int serverNum = 20000;
        final int serverPort = 4568;
        LVLClass lvl = new LVLClass(serverName, serverNum);
        lvl.setORB(orb);
        org.omg.CORBA.Object ref = rootpoa.servant_to_reference(lvl);
        DCMS href = DCMSHelper.narrow(ref);

        org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

        NameComponent path[] = ncRef.to_name("LVL");
        ncRef.rebind(path, href);

        System.out.println("Laval server is running on port:" + serverPort);
        String managerID = "LVL0000";

        lvl.createSRecord("Shubham", "Patel", "French", "active", "12/09/2021", managerID);
        lvl.createSRecord("Vandit", "Thakkar", "Spanish", "inactive", "01/12/2020", managerID);
        lvl.createTRecord("Bhoomi", "Shah", "1567 township" , "514-587-1126", "Java", "LVL", managerID);
        lvl.createTRecord("Nilesh", "Agrawal", "1564 newstreet" , "234-999-1926", "Python", "LVL", managerID);

        orb.run();

    }

}


