package Servers;
/**
 * Server file to initiate DDOServer
 */
import DCMSApp.DCMS;
import DCMSApp.DCMSHelper;

import ServerImplementation.DDOClass;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class DDOServer {

    public static void main(String args[]) throws Exception {

        ORB orb = ORB.init(args, null);
        POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootpoa.the_POAManager().activate();
        final String serverName = "DDO";
        final int serverNum = 30000;
        final int serverPort = 4569;
        DDOClass ddo = new DDOClass(serverName, serverNum);
        ddo.setORB(orb);
        org.omg.CORBA.Object ref = rootpoa.servant_to_reference(ddo);
        DCMS href = DCMSHelper.narrow(ref);

        org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

        NameComponent path[] = ncRef.to_name("DDO");
        ncRef.rebind(path, href);

        System.out.println("Dollard des server is running on port:" + serverPort);
        String managerID = "DDO0000";

        ddo.createSRecord("Ashraf", "Amirali", "AI", "inactive", "12/09/2019", managerID);
        ddo.createSRecord("Azim", "Surani", "React", "active", "01/12/2018", managerID);
        ddo.createTRecord("Meet", "Patel", "1567 township" , "514-587-8787", "Ruby", "DDO", managerID);
        ddo.createTRecord("Akshita", "Patel", "1564 newstreet" , "876-999-1926", "History", "DDO", managerID);
        orb.run();

    }

}
