package br.inf.ufes.ppd.implementation;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.Master;
import br.inf.ufes.ppd.Slave;
import br.inf.ufes.ppd.SlaveManager;
import java.rmi.RemoteException;
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

    long currentIndex, lastIndex;
    boolean done;
    long startTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(long currentCheck) {
        this.currentIndex = currentCheck;
    }

    public long getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(long lastCheck) {
        this.lastIndex = lastCheck;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
    
    AttackControl(long curr, long lc, long stTime)
    {
        this.currentIndex = curr;
        this.lastIndex = lc;
        this.startTime = stTime;
        this.done = false;     
    }
}

class SlaveControl {

    Slave slaveRef;
    String name;
    long time;
    Map<Integer, AttackControl> attackList;

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
        this.attackList = new HashMap<>();
    }
}

public class MasterImpl implements Master {

    private Map<UUID, SlaveControl> slaves = new HashMap<>();
    private Map<Integer, List<Guess>> guessList = new HashMap<>();
    private int attackNumber = 0;
    
    public int getAttackNumber(){
        return attackNumber++;
    }
        
    //SlaveManager interfaces
    @Override
    public void addSlave(Slave s, String slaveName, UUID slavekey) throws RemoteException {
        
        //Checking if slave is already registered
        if (!slaves.containsKey(slavekey)) {
            SlaveControl sc = new SlaveControl(s, slaveName, System.currentTimeMillis());
            
            synchronized (slaves) {
                slaves.put(slavekey, sc);
            }
            
            System.out.println("Slave: " + slaveName + " foi adicionado");
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
        System.out.println(" Message: " + currentguess.getMessage());
    }

    @Override
    public void checkpoint(UUID slaveKey, int attackNumber, long currentindex) throws RemoteException {
        
        SlaveControl s = slaves.get(slaveKey);
        //Tomar cuidado com a hora q chega o checkpoint o tempo pode ser menor do q o na variavel
        s.setTime(System.currentTimeMillis()); //Registering current time of checkpoint of slave
        
        AttackControl attack = s.getAttackList().get(attackNumber);
        attack.setCurrentIndex(currentindex);   //Updating currentIndex
        
        if(attack.getCurrentIndex() == attack.getLastIndex()){
            attack.setDone(true);
        }       
        
        long elapsedTime = System.currentTimeMillis() - attack.getStartTime();
        
        System.out.println("Slave: " + s.getName() + " checkpoint, current index: " + currentindex);
        System.out.println("Elapsed Time: " + elapsedTime);
    }
    
    private class AttackTask extends Thread{

        SlaveManager smRef;
        int attackID;
        Map<UUID, SlaveControl> slavesCopy;
        byte[] encriptedText;
        byte[] knownText;
        
        public AttackTask(SlaveManager callback, int attackNumber, byte[] ciphertext, byte[] knowntext){
            
            this.smRef = callback;
            this.attackID = attackNumber;
            this.encriptedText = ciphertext;
            this.knownText = knowntext;
        }
        
        @Override
        public void run() {
            
            synchronized(slaves){
                slavesCopy = new HashMap<>(slaves);
            }
            
            List<UUID> failedSlaves = new ArrayList<>();
            int dictionarySize = Configurations.DICTIONARY_SIZE; 
            int indexDivision = dictionarySize / slavesCopy.size();
            int initialIndex = 0; 
            int finalIndex = indexDivision;
            long startTime = System.currentTimeMillis();
            //Creating a guess list for this attack
            synchronized(guessList){
                guessList.put(attackID, new ArrayList<>());
            }

            
            for (UUID slaveID : slavesCopy.keySet()) {
                
                SlaveControl sc = slavesCopy.get(slaveID);
                Slave slRef = sc.getSlaveRef();

                try{
                    AttackControl currentAttack = new AttackControl(initialIndex, finalIndex, startTime);
                    
                    synchronized(slaves)
                    {
                        slaves.get(slaveID).getAttackList().put(attackID, currentAttack);
                    }
                    
                    slRef.startSubAttack(encriptedText, knownText, initialIndex, finalIndex, attackID, smRef);

                    initialIndex = finalIndex;
                    finalIndex += indexDivision;

                    if(finalIndex > dictionarySize)
                        finalIndex = dictionarySize;            
                
                }
                catch(RemoteException e){
                    failedSlaves.add(slaveID);
                    System.err.println("Slave failed: " + slaveID + "Name: " + sc.getName());
                }
            
            }//end of jobs distribuition

            
            ////////////////TERMINAR PARA FAZER REDISTRIBUICAO ///////////////////////////////

            //verificar depois para caso que lista vazia e para redistribuir jobs qdo remover
//            if(failedSlaves.size() != 0){
//
//                synchronized(slaves){
//                    for (UUID uid : failedSlaves) {
//                        slaves.remove(uid);
//                    }
//
//                    //Getting the working slaves
//                    slavesCopy = new HashMap<>(slaves);
//                }
//
//                for (UUID failedSlaveID : failedSlaves) {
//
//                    SlaveControl s = slavesCopy.get(failedSlaveID);
//                    Map<Integer, AttackControl> attackList = s.getAttackList();
//
//                    for (Integer attackID : attackList.keySet()) {
//                        AttackControl ac = attackList.get(attackID);
//
//                        if(!ac.isDone()){
//                        //tem q resolver o problema da criação do ataque
//                        //slRef.startSubAttack(ciphertext, knowntext, initialIndex, finalIndex, attackID, callback);
//                        }
//
//                    }
//
//                }
//
//
//            }
        }
    }
    
    
    //Attacker interfaces
    @Override
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) throws RemoteException {
        int attackID = getAttackNumber();
        Map<UUID, SlaveControl> slavesCopy;
               
        Thread attack = new AttackTask(this, attackID, ciphertext, knowntext);
        attack.start();
        try {
            attack.join();
        } catch (InterruptedException ex) {
            System.out.println("Aki");
        }
        
//        new Thread(){        
//            public void run(){
//                
//                List<UUID> failedSlaves = new ArrayList<>();
//                int indexDivision = dictionarySize / slaves.size();
//                int initialIndex = 0; 
//                int finalIndex = initialIndex + indexDivision;
//
//                synchronized(guessList){
//                    guessList.put(attackID, new ArrayList<Guess>());
//                }
//                
//                for (UUID slaveID : slavesCopy.keySet()) {
//                    SlaveControl sc = slavesCopy.get(slaveID);
//                    Slave slRef = sc.getSlaveRef();
//                    
//                    try{
//                        slRef.startSubAttack(ciphertext, knowntext, initialIndex, finalIndex, attackID, callback);
//                    }
//                    catch(Exception e){
//                        failedSlaves.add(slaveID);
//                        System.err.println("Slave failed: " + slaveID);
//                    }
//                    
//                    initialIndex = initialIndex + indexDivision;
//                    finalIndex = initialIndex + indexDivision;
//
//                    if(finalIndex > dictionarySize)
//                        finalIndex = dictionarySize;            
//                }
//                
//                ////////////////TERMINAR PARA FAZER REDISTRIBUICAO ///////////////////////////////
//                
//                
//                //verificar depois para caso que lista vazia e para redistribuir jobs qdo remover
//                if(failedSlaves.size() != 0){
//                    
//                    Map<UUID, SlaveControl> slavesWorking;
//                    
//                    synchronized(slaves){
//                        for (UUID uid : failedSlaves) {
//                            slaves.remove(uid);
//                        }
//                        
//                        slavesWorking = new HashMap<>(slaves);
//                    }
//                    
//                    for (UUID failedSlaveID : failedSlaves) {
//                        
//                        SlaveControl s = slavesCopy.get(failedSlaveID);
//                        Map<Integer, AttackControl> attackList = s.getAttackList();
//                        
//                        for (Integer attackID : attackList.keySet()) {
//                            AttackControl ac = attackList.get(attackID);
//                            
//                            if(!ac.isDone()){
//                            //tem q resolver o problema da criação do ataque
//                            //slRef.startSubAttack(ciphertext, knowntext, initialIndex, finalIndex, attackID, callback);
//                            }
//                                
//                        }
//                        
//                    }
//                    
//
//                }
//            }
//        }.start();
        
        //Waiting end job
        
        boolean done;
        
        do{
            done = true;
            for (SlaveControl sc : slaves.values()) {
                
                AttackControl ac = sc.getAttackList().get(attackID);
                
                if(ac != null)
                    done = done && ac.isDone();
            }
            
        }while(!done);
                        
        //Return guess vector
        Guess[] guess = getGuessVector(guessList.get(attackID));
                               
        return guess;
    }

    public Guess[] getGuessVector(List<Guess> g){
        
        Guess[] guessVector = new Guess[g.size()];
        
        for(int i = 0; i < g.size(); i++){
            guessVector[i] = g.get(i);
        }
        
        return guessVector;
    }
}
