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

class SubAttackControl {

    long currentIndex, lastIndex;
    boolean done;
    
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
    
    SubAttackControl(long curr, long lc)
    {
        this.currentIndex = curr;
        this.lastIndex = lc;
        this.done = false;     
    }
}

class AttackControl {

    Map<Integer, SubAttackControl> subAttacksMap;
    boolean done;
    long startTime;
    
    public Map<Integer, SubAttackControl> getSubAttacksMap() {
        return subAttacksMap;
    }

    public void setSubAttacksMap(Map<Integer, SubAttackControl> subAttacksMap) {
        this.subAttacksMap = subAttacksMap;
    }
    
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public boolean isDone() {
        boolean finished = true;
        
        for (SubAttackControl subAttack : subAttacksMap.values()) {
            finished &= subAttack.isDone();
        }
        
        setDone(finished);
        
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
    
    AttackControl()
    {
        this.startTime = System.currentTimeMillis();
        this.done = false;
        this.subAttacksMap = new HashMap<>();
    }
}

class SlaveControl {

    Slave slaveRef;
    String name;
    long time;
    List<Integer> subAttackNumbersList;

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

    public List<Integer> getSubAttackNumbersList() {
        return subAttackNumbersList;
    }

    public void setSubAttackNumbersList(List<Integer> attackList) {
        this.subAttackNumbersList = attackList;
    }
        
    SlaveControl(Slave s, String n, long t) {
        this.slaveRef = s;
        this.name = n;
        this.time = t;
        this.subAttackNumbersList = new ArrayList<>();
    }
}

public class MasterImpl implements Master {

    private Map<UUID, SlaveControl> slaves = new HashMap<>();
    private Map<Integer, List<Guess>> guessList = new HashMap<>();
    
    private Map<Integer, Integer> attackMap = new HashMap<>();
    
    private Map<Integer, AttackControl> attacksList = new HashMap<>();
    
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
    public void foundGuess(UUID slaveKey, int subAttackNumber, long currentindex, Guess currentguess) throws RemoteException {
        
        String slaveName = slaves.get(slaveKey).getName();
        
        int attackID = attackMap.get(subAttackNumber);
        
        synchronized(guessList){
            guessList.get(attackID).add(currentguess);
        }
        synchronized(slaves){
            slaves.get(slaveKey).setTime(System.currentTimeMillis());
        }
        
        System.out.println("Slave: " + slaveName + " Found guess: " + currentguess.getKey() + " Current index: " + currentindex);
        System.out.println(" Message: " + currentguess.getMessage());
        System.out.println("Attack Number: " + attackID);
    }

    @Override
    public void checkpoint(UUID slaveKey, int subAttackNumber, long currentindex) throws RemoteException {
        
        int attackNumber = attackMap.get(subAttackNumber);
        
        SlaveControl s = slaves.get(slaveKey);
        s.setTime(System.currentTimeMillis()); //Registering current time of checkpoint of slave
        //Tomar cuidado com a hora q chega o checkpoint o tempo pode ser menor do q o na variavel
        //Arrumar no setTime;
        
        AttackControl attack = attacksList.get(attackNumber);
                
        SubAttackControl subAttack = attack.getSubAttacksMap().get(subAttackNumber);
        subAttack.setCurrentIndex(currentindex);   //Updating currentIndex
        
        if(currentindex == subAttack.getLastIndex()){
            subAttack.setDone(true);
        }       
        
        long elapsedTime = System.currentTimeMillis() - attack.getStartTime();
        
        System.out.println("Slave: " + s.getName() + " checkpoint, current index: " + currentindex);
        System.out.println("Attack Number: " + attackNumber);
        System.out.println("Elapsed Time: " + elapsedTime);
    }
    
    private class AttackTask extends Thread{

        SlaveManager smRef;
        int attackID;
        Map<UUID, SlaveControl> slavesWorking;
        byte[] encriptedText;
        byte[] knownText;
        int subAttackNumber;
        
        public AttackTask(SlaveManager callback, int attackNumber, byte[] ciphertext, byte[] knowntext){
            
            this.smRef = callback;
            this.attackID = attackNumber;
            this.encriptedText = ciphertext;
            this.knownText = knowntext;
            this.subAttackNumber = 0;
            
            synchronized(slaves){
                this.slavesWorking = new HashMap<>(slaves);
            }
        }
        
        public int getSubAttackID() {
            return subAttackNumber++;
        }
        
        @Override
        public void run() {
            
            Map<UUID, SlaveControl> failedSlaves = new HashMap<>();
            long dictionarySize = Configurations.DICTIONARY_SIZE; 
            long indexDivision = dictionarySize / slavesWorking.size();
            long initialIndex = 0; 
            long finalIndex = indexDivision + (dictionarySize % slavesWorking.size());
            long startTime = System.currentTimeMillis();
            
            //Creating a guess list for this attack
            synchronized(guessList){
                guessList.put(attackID, new ArrayList<>());
            }
            
            synchronized(attacksList){
                attacksList.put(attackID, new AttackControl());
            }
            
            for (UUID slaveID : slavesWorking.keySet()) {
                
                SlaveControl sc = slavesWorking.get(slaveID);
                Slave slRef = sc.getSlaveRef();
                
                int subAttackID = getSubAttackID();
                
                synchronized(attackMap){
                    attackMap.put(subAttackID, attackID);
                }
                
                try{
                    SubAttackControl currentSubAttack = new SubAttackControl(initialIndex, finalIndex);
                    
                    synchronized(attacksList){
                        attacksList.get(attackID).getSubAttacksMap().put(subAttackID, currentSubAttack);
                    }
                    
                    synchronized(slaves)
                    {
                        slaves.get(slaveID).getSubAttackNumbersList().add(subAttackID);
                    }
                    
                    slRef.startSubAttack(encriptedText, knownText, initialIndex, finalIndex, subAttackID, smRef);

                    initialIndex = finalIndex;
                    finalIndex += indexDivision;

                    if(finalIndex > dictionarySize)
                        finalIndex = dictionarySize;            
                
                }
                catch(RemoteException e){
                    failedSlaves.put(slaveID, sc);
                    System.err.println("Slave failed: " + slaveID + "Name: " + sc.getName());
                }
            
            }//end of jobs distribuition

            
            ////////////////TERMINAR PARA FAZER REDISTRIBUICAO ///////////////////////////////

//            verificar depois para caso que lista vazia e para redistribuir jobs qdo remover
//            if(!failedSlaves.isEmpty()){
//
//                synchronized(slaves){
//                    for (UUID uid : failedSlaves.keySet()) {
//                        slaves.remove(uid);
//                    }
//
//                    //Getting the actual working slaves
//                    slavesWorking = new HashMap<>(slaves);
//                }
//                
//                List<AttackControl> stopedJobs = new ArrayList<>();
//                
//                for (UUID failedSlaveID : failedSlaves.keySet()) {
//
//                    SlaveControl s = failedSlaves.get(failedSlaveID);
//                    Map<Integer, AttackControl> attackList = s.getAttackList();
//                    
//                    //Checking if some job of this slave didnt finish
//                    for (Integer attackID : attackList.keySet()) {
//                        
//                        AttackControl ac = attackList.get(attackID);
//
//                        //if not, adding to stopedJobs list
//                        if(!ac.isDone()){
//                            stopedJobs.add(ac);
//                        }
//                    }
//
//                    for (AttackControl stopedJob : stopedJobs) {
//                        long indexSize = stopedJob.getLastIndex() - stopedJob.getCurrentIndex();
//                        long division = indexSize / slavesWorking.size();
//                        long startIndex = 0; 
//                        long endIndex = division + (indexSize % slavesWorking.size());
//                        
//                        for (UUID slaveID : slavesWorking.keySet()) {
//                
//                            SlaveControl sc = slavesWorking.get(slaveID);
//                            Slave slRef = sc.getSlaveRef();
//
//                            try{
//                                AttackControl currentAttack = new AttackControl(startIndex, endIndex, startTime);
//
//                                synchronized(slaves)
//                                {
//                                    slaves.get(slaveID).getAttackList().put(attackID, currentAttack);
//                                    
//                                }
//
//                                slRef.startSubAttack(encriptedText, knownText, startIndex, endIndex, attackID, smRef);
//
//                                startIndex = endIndex;
//                                endIndex += indexDivision;
//
//                                if(endIndex > indexSize)
//                                    endIndex = indexSize;            
//
//                            }
//                            catch(RemoteException e){
//                                failedSlaves.put(slaveID, sc);
//                                System.err.println("Slave failed: " + slaveID + "Name: " + sc.getName());
//                            }
//
//                        }//end of jobs distribuition
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
            System.err.println("Attack error:\n " + ex.getMessage());
        }
        
        //Waiting end job
                
        while(!attacksList.get(attackID).isDone());
                
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
