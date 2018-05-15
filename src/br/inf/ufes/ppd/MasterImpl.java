package br.inf.ufes.ppd;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Leonardo Santos Paulucio
 */
class AttackControl {

    long currentCheck, lastCheck;
    boolean done = false;

    public long getCurrentCheck() {
        return currentCheck;
    }

    public void setCurrentCheck(long currentCheck) {
        this.currentCheck = currentCheck;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(long lastCheck) {
        this.lastCheck = lastCheck;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
    
    AttackControl(long curr, long lc)
    {
        this.currentCheck = curr;
        this.lastCheck = lc;
    }
}

class SlaveControl {

    Slave slaveRef;
    String name;
    long time;
    boolean busy;
    Map<Integer, AttackControl> attackList = new HashMap<>();

    public Slave getSlaveRef() {
        return slaveRef;
    }

    public void setSlaveRef(Slave slaveRef) {
        this.slaveRef = slaveRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public Map<Integer, AttackControl> getAttackList() {
        return attackList;
    }

    public void setAttackList(Map<Integer, AttackControl> attackList) {
        this.attackList = attackList;
    }
        
    SlaveControl(Slave s, String n, long t) {
        this.slaveRef = s;
        this.name = n;
        this.time = t;
        this.busy = false;
    }
}

public class MasterImpl implements Master {

    private Map<UUID, SlaveControl> slaves = new HashMap<>();
    private Map<Integer, List<Guess>> guessList = new HashMap<>();
    private int attackId = 0;
    
    public int getAttackId(){
        return attackId++;
    }
        
    //SlaveManager interfaces
    @Override
    public void addSlave(Slave s, String slaveName, UUID slavekey) throws RemoteException {
        SlaveControl sc = new SlaveControl(s, slaveName, System.currentTimeMillis());

        if (!slaves.containsKey(slavekey)) {
            synchronized (slaves) {
                slaves.put(slavekey, sc);
            }
        }
//        else{
//            System.out.println("Client already exists!");
//        }
    }

    @Override
    public void removeSlave(UUID slaveKey) throws RemoteException {
        synchronized (slaves) {
            slaves.remove(slaveKey);
        }
    }

    @Override
    public void foundGuess(UUID slaveKey, int attackNumber, long currentindex, Guess currentguess) throws RemoteException {
        String slaveName = slaves.get(slaveKey).getName();
        
        synchronized(guessList){
            guessList.get(attackNumber).add(currentguess);
        }
        synchronized(slaves){
            slaves.get(slaveKey).setTime(System.currentTimeMillis());
        }
        
        System.out.println("Slave: " + slaveName + " Found guess: " + currentguess.getKey() + " Current index: " + currentindex);
    }

    @Override
    public void checkpoint(UUID slaveKey, int attackNumber, long currentindex) throws RemoteException {
        SlaveControl s = slaves.get(slaveKey);
        AttackControl attack = s.getAttackList().get(attackNumber);
        
        s.setTime(System.currentTimeMillis()); //Registering time
        attack.setCurrentCheck(currentindex);   //Updating currentIndex
        
        if(attack.getCurrentCheck() == attack.getLastCheck()){
            attack.setDone(true);
            
            boolean done = false;
            
            for (AttackControl at : s.getAttackList().values()) {
                done = done && at.isDone();
            }
            
            if(done == true)
                s.setBusy(false);
        }       
        
        System.out.println("Slave: " + s.getName() + " checkpoint, current index: " + currentindex);
    }
    
    //Attacker interfaces
    @Override
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) throws RemoteException {
        int dictionarySize = 80368;
        int ID = getAttackId();
        
        int indexDivision = dictionarySize / slaves.size();
        int initialIndex = 0; 
        int finalIndex = initialIndex + indexDivision;
        
        guessList.put(ID, new ArrayList<Guess>());
        
        for (SlaveControl s : slaves.values()) {
            Slave sl = s.getSlaveRef();
            sl.startSubAttack(ciphertext, knowntext, initialIndex, finalIndex, ID, this);
            
            initialIndex = initialIndex + indexDivision;
            finalIndex = initialIndex + indexDivision;
            
            if(finalIndex > dictionarySize)
                finalIndex = dictionarySize;            
        }
        //criar thread para verificar e retornar guess
        return null;
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
            System.out.println("Master ready!");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

}
