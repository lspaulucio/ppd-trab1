package br.inf.ufes.ppd.application;

import br.inf.ufes.ppd.Master;
import br.inf.ufes.ppd.Slave;
import br.inf.ufes.ppd.implementation.Configurations;
import br.inf.ufes.ppd.implementation.SlaveImpl;
import br.inf.ufes.ppd.services.RebindService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.UUID;

/**
 *
 * @author Leonardo Santos Paulucio
 */

public class SlaveServer {
    
    public static void main(String[] args) {
        String SLAVE_NAME = "SlaveLeonardo";
        //Creating a new Slave
        SlaveImpl slave = new SlaveImpl();
        slave.readDictionary(Configurations.DICTIONARY_PATH);
        slave.setUid(UUID.randomUUID());
 
        try {
            Registry registry = LocateRegistry.getRegistry(Configurations.REGISTRY_ADDRESS);
            Master m = (Master) registry.lookup(Configurations.REGISTRY_MASTER_NAME);
            
            Slave slaveRef = (Slave) UnicastRemoteObject.exportObject(slave,0);
//            m.addSlave(slaveRef, SLAVE_NAME, slave.getUid());
//            System.out.println("Slave registered");
            
            //Creating rebind service
            Timer timer = new Timer();   
            RebindService rs = new RebindService(m, slaveRef, SLAVE_NAME, slave.getUid());            
            timer.scheduleAtFixedRate(rs, 0, Configurations.REBIND_TIME);  // 0 = delay, REBIND_TIME = frequence
            
        
        } catch (Exception e) {
            System.err.println("Slave exception: " + e.getMessage());
            e.printStackTrace();
        }        
    }
}
