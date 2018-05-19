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
    private int subAttackNumber = 0;
    
    public int getAttackNumber(){
        return attackNumber++;
    }
    
    public int getSubAttackNumber(){
        return subAttackNumber++;
    }
        
    //SlaveManager interfaces
    
    /**
     * Adiciona um escravo na lista.
     * @param s Referência para o escravo.
     * @param slaveName Nome do escravo.
     * @param slavekey  Identificador único do escravo.
     */
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
        else{
            synchronized (slaves) {
                slaves.get(slavekey).setTime(System.currentTimeMillis());
            }
            
            System.out.println("Client already exists!");
        }
    }

    /**
     * Remove um escravo da lista.
     * @param slavekey  Identificador único do escravo que sera removido.
     */
    @Override
    public void removeSlave(UUID slaveKey) throws RemoteException {
        synchronized (slaves) {
            slaves.remove(slaveKey);
        }
    }
    
    /**
     * Guess encontrado. Chamado pelo escravo ao encontrar um guess.
     * @param slaveKey  Identificador único do escravo.
     * @param subAttackNumber Número do sub ataque.
     * @param currentindex Índice atual do ataque.
     * @param currentguess Guess encontrado.
     */ 
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

    /**
     * Checkpoint. Chamado frequentemente pelo escravo
     * informando o andamento do ataque
     * @param slaveKey  Identificador único do escravo.
     * @param subAttackNumber Número do sub ataque.
     * @param currentindex Índice atual do ataque.
     */ 
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
 
        
        public AttackTask(SlaveManager callback, int attackNumber, byte[] ciphertext, byte[] knowntext){
            
            this.smRef = callback;
            this.attackID = attackNumber;
            this.encriptedText = ciphertext;
            this.knownText = knowntext;
            
            synchronized(slaves){
                this.slavesWorking = new HashMap<>(slaves);
            }
        }
        
        @Override
        public void run() {
            
            Map<UUID, SlaveControl> failedSlaves = new HashMap<>();
            long dictionarySize = Configurations.DICTIONARY_SIZE; 
            long indexDivision = dictionarySize / slavesWorking.size();
            long initialIndex = 0; 
            long finalIndex = indexDivision + (dictionarySize % slavesWorking.size());
            long startTime = System.currentTimeMillis();
            int subAttackID = 0;
            
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
                
                subAttackID = getSubAttackNumber();
                
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
            if(!failedSlaves.isEmpty()){
                System.out.println("Akiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
                
                synchronized(slaves){
                    for (UUID uid : failedSlaves.keySet()) {
                        slaves.remove(uid);
                    }

                    //Getting the actual working slaves
                    slavesWorking = new HashMap<>(slaves);
                }
                
                for (UUID failedSlaveID : failedSlaves.keySet()) {

                    SlaveControl s = failedSlaves.get(failedSlaveID);
                    List<Integer> subAttacks = s.getSubAttackNumbersList();
                    
                    //Checking if some job of this slave didnt finish
                    for (Integer subID : subAttacks) {
                        int id = attackMap.get(subID);
                        
                        AttackControl ac = attacksList.get(id);
                        SubAttackControl sub = ac.getSubAttacksMap().get(subID);
                        
                        //if not, adding to stopedJobs list
                        if(!sub.isDone()){
                            
                            long indexSize = sub.getLastIndex() - sub.getCurrentIndex();
                            long division = indexSize / slavesWorking.size();
                            long startIndex = sub.getCurrentIndex(); 
                            long endIndex = sub.getCurrentIndex() + division + (indexSize % slavesWorking.size());

                            for (UUID slaveID : slavesWorking.keySet()) {

                                SlaveControl sc = slavesWorking.get(slaveID);
                                Slave slRef = sc.getSlaveRef();

                                try{
                                    synchronized(slaves)
                                    {
                                        slaves.get(slaveID).getSubAttackNumbersList().add(subID);
                                    }

                                    slRef.startSubAttack(encriptedText, knownText, startIndex, endIndex, subID, smRef);

                                    startIndex = endIndex;
                                    endIndex += division;

                                    if(endIndex > sub.getLastIndex())
                                        endIndex = sub.getLastIndex();            

                                }
                                catch(RemoteException e){
                                    System.err.println("Redistribution failed:\n" + e.getMessage());
                                }
                            }//end of jobs distribuition
                        }
                        
                        System.out.println("End redistribution");
                    }
                }
            }
            
            
            
        }
    }
    
    
    //Attacker interfaces
    
    /**
     * Inicia um ataque. Chamado pelo cliente.
     * @param ciphertext Mensagem criptografada.
     * @param knowntext  Trecho conhecido da mensagem.
     * @return Vetor de guess encontrados
     */ 
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
        
        System.out.println("End attack: " + attackID);
        //Return guess vector
        Guess[] guess = getGuessVector(guessList.get(attackID));
                               
        return guess;
    }

    /**
     * Gera um vetor de Guess a partir de uma lista de Guess.
     * @param g  Lista de guess.
     * @return Vetor contendo os guess da lista.
     */ 
    public Guess[] getGuessVector(List<Guess> g){
        
        Guess[] guessVector = new Guess[g.size()];
        
        for(int i = 0; i < g.size(); i++){
            guessVector[i] = g.get(i);
        }
        
        return guessVector;
    }
}
