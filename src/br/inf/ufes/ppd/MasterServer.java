/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.inf.ufes.ppd;

import java.rmi.registry.*;
import java.rmi.server.*;

/**
 *
 * @author leonardo
 */
public class MasterServer {
        
    public static void main(String[] args) {
    
        final String REGISTRY_NAME = "mestre";
        final String REGISTRY_ADDRESS = "localhost";
        try {
            MasterImpl masterObject = new MasterImpl();
            Master masterReference = (Master) UnicastRemoteObject.exportObject(masterObject, 0);
            // Bind the remote object in the registry
            Registry registry = LocateRegistry.getRegistry(REGISTRY_ADDRESS); // opcional: host
            registry.rebind(REGISTRY_NAME, masterReference);
            
            System.out.println("REGISTRADO!!!");
        }
        catch (Exception e) 
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
