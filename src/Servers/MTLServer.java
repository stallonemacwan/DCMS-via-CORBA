package Servers;
/**
 * Server file to initiate MTLServer
 */
import DCMSApp.DCMS;
import DCMSApp.DCMSHelper;

import ServerImplementation.MTLClass;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class MTLServer {

    public static void main(String args[]) throws Exception {
        ORB orb = ORB.init(args, null);
        POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootpoa.the_POAManager().activate();
        final String serverName = "MTL";
        final int serverNum = 10000;
        final int serverPort = 4567;
        MTLClass mtl = new MTLClass(serverName, serverNum);
        mtl.setORB(orb);
        org.omg.CORBA.Object ref = rootpoa.servant_to_reference(mtl);
        DCMS href = DCMSHelper.narrow(ref);
        org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

        NameComponent path[] = ncRef.to_name("MTL");
        ncRef.rebind(path, href);

        System.out.println("Montreal server is running on port:" + serverPort);
        String managerID = "MTL0000";

        mtl.createSRecord("Jay", "Patel", "English", "active", "12/09/2020", managerID);
        mtl.createSRecord("Stallone", "Mecwan", "Math", "inactive", "01/12/2000", managerID);
        mtl.createTRecord("Harsh", "Agrawal", "1234 avenue" , "234-587-1126", "Geography", "MTL", managerID);
        mtl.createTRecord("Pavit", "Singh", "1564 palm street" , "234-587-1926", "Mythology", "MTL", managerID);

        orb.run();

    }
}

