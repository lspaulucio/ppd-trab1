package br.inf.ufes.ppd;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Leonardo Santos Paulucio
 */
public class MasterImpl implements Master 
{
    private List<Slave> slaves = new ArrayList<>();
    
    //SlaveManager interfaces
    @Override
    public void addSlave(Slave s, String slaveName, UUID slavekey) throws RemoteException {
        synchronized(slaves){
            slaves.add(s);
        }
    }

    @Override
    public void removeSlave(UUID slaveKey) throws RemoteException {
        synchronized(slaves){
            //fazer hashMap
        }
    }

    @Override
    public void foundGuess(UUID slaveKey, int attackNumber, long currentindex, Guess currentguess) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkpoint(UUID slaveKey, int attackNumber, long currentindex) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //Attacker interfaces
    @Override
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 
    
    public static void main(String[] args) {
    
        final String REGISTRY_NAME = "mestre";
        final String REGISTRY_ADDRESS = "localhost";
        try {
            MasterImpl masterObject = new MasterImpl();
            Master masterReference = (Master) UnicastRemoteObject.exportObject(masterObject, 0);
            // Bind the remote object in the registry
            Registry registry = LocateRegistry.getRegistry(REGISTRY_ADDRESS); // opcional: host
            registry.rebind(REGISTRY_NAME, masterReference);
            
            //Criar HASHMAP para adicionar slaves
            System.out.println("REGISTRADO!!!");
        }
        catch (Exception e) 
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
}
