package br.inf.ufes.ppd.application;

import br.inf.ufes.ppd.Master;
import br.inf.ufes.ppd.implementation.Configurations;
import br.inf.ufes.ppd.implementation.MasterImpl;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author Leonardo Santos Paulucio
 */

public class MasterServer {
    
    public static void main(String[] args) {
        try {
            MasterImpl masterObj = new MasterImpl();
            Master masterRef = (Master) UnicastRemoteObject.exportObject(masterObj, 0);
            // Bind the remote object in the registry
            Registry registry = LocateRegistry.getRegistry(Configurations.REGISTRY_ADDRESS); // opcional: host
            registry.rebind(Configurations.REGISTRY_MASTER_NAME, masterRef);

            System.out.println("Master ready!");
            
        } catch (Exception e) {
            System.err.println("Master exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
