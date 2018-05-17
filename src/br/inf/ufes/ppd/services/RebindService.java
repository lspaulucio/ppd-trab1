package br.inf.ufes.ppd.services;

import br.inf.ufes.ppd.Master;
import br.inf.ufes.ppd.Slave;
import br.inf.ufes.ppd.implementation.Configurations;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.TimerTask;
import java.util.UUID;

/**
 *
 * @author Leonardo Santos Paulucio
 */
public class RebindService extends TimerTask {
    
    private Master masterRef;
    private Slave slaveRef;
    private String slaveName;
    private UUID slaveUID;
    
    
    public RebindService(Master m, Slave s, String name, UUID uid){
        this.masterRef = m;
        this.slaveRef = s;
        this.slaveName = name;
        this.slaveUID = uid;
    }
    
    public void run(){
                
        try{
            //Trying to rebind on master
            masterRef.addSlave(slaveRef, slaveName, slaveUID);
            System.out.println("Registrado");
        }
        catch (Exception e){
            System.err.println("Master down " + e.getMessage());

            //Master down, so try to find another master on registry
            try {
                Registry registry = LocateRegistry.getRegistry(Configurations.REGISTRY_ADDRESS);
                Master m = (Master) registry.lookup(Configurations.REGISTRY_MASTER_NAME);
                m.addSlave(slaveRef, slaveName, slaveUID);
                masterRef = m; //Save new master reference
            }
            catch (Exception p){
                System.err.println("Master not found or not registered on registry " + p.getMessage());
            }
        }
    }
    
    
}