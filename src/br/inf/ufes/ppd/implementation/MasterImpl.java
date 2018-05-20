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
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/** Master Implementation.
 *
 * @author Leonardo Santos Paulucio
 */

class SubAttackControl {

    private long currentIndex, lastIndex;
    private boolean done;
    
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

    private Map<Integer, SubAttackControl> subAttacksMap;
    private byte[] cipherMessage;
    private byte[] knownText;

    public byte[] getCipherMessage() {
        return cipherMessage;
    }

    public void setCipherMessage(byte[] cipherMessage) {
        this.cipherMessage = cipherMessage;
    }

    public byte[] getKnownText() {
        return knownText;
    }

    public void setKnownText(byte[] knownText) {
        this.knownText = knownText;
    }
    private boolean done;
    private double startTime;
    
    public Map<Integer, SubAttackControl> getSubAttacksMap() {
        return subAttacksMap;
    }

    public void setSubAttacksMap(Map<Integer, SubAttackControl> subAttacksMap) {
        this.subAttacksMap = subAttacksMap;
    }
    
    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }
    
    public boolean isDone() {
        boolean finished = true;
        
        synchronized(subAttacksMap){
        
            for (SubAttackControl subAttack : subAttacksMap.values()) {
                finished &= subAttack.isDone();
            }
        }
        
        return finished;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
    
    public AttackControl(byte[] cipher, byte[] known)
    {
        this.cipherMessage = cipher;
        this.knownText = known;
        this.startTime = System.nanoTime();
        this.done = false;
        this.subAttacksMap = new HashMap<>();
    }
}

class SlaveControl {

    private Slave slaveRef;
    private String name;
    private double time;
    private List<Integer> subAttackNumbersList;

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

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
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
    
    public MasterImpl(){
        Timer t = new Timer();
        MonitoringService ms = new MonitoringService(this);
        t.scheduleAtFixedRate(ms, 0, Configurations.TIMEOUT);
    }
    
    public boolean hasAttack(){
        int numberAttacks = 0;
        
        synchronized(attacksList){
            for (AttackControl attack : attacksList.values()) {
                if(!attack.isDone())
                    numberAttacks++;
            }
        }
        return numberAttacks > 0;
    }
    
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
            SlaveControl sc = new SlaveControl(s, slaveName, System.nanoTime());
            
            synchronized (slaves) {
                slaves.put(slavekey, sc);
            }
            
            System.out.println("Slave: " + slaveName + " foi adicionado");
        }
//        else{
//            System.out.println("Client already exists!");
//        }
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
            slaves.get(slaveKey).setTime(System.nanoTime());
        }
        
        System.out.println("Attack Number: " + attackID);
        System.out.println("Slave: " + slaveName + " Found guess: " + currentguess.getKey() + " Current index: " + currentindex);
        System.out.println("Message: " + currentguess.getMessage());
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
        s.setTime(System.nanoTime()); //Registering current time of checkpoint of slave
        //Tomar cuidado com a hora q chega o checkpoint o tempo pode ser menor do q o na variavel
        //Arrumar no setTime;
        
        AttackControl attack = attacksList.get(attackNumber);
                
        SubAttackControl subAttack = attack.getSubAttacksMap().get(subAttackNumber);
        subAttack.setCurrentIndex(currentindex);   //Updating currentIndex
        
        if(currentindex == subAttack.getLastIndex()){
            subAttack.setDone(true);
        }       
        
        double elapsedTime = (System.nanoTime() - attack.getStartTime())/1000000000;
        
        System.out.println("Attack Number: "+ attackNumber + " Elapsed Time: " + elapsedTime);
        System.out.println("Slave: " + s.getName() + " checkpoint.");
        System.out.println("SubAttack " + subAttackNumber + " Status: " + currentindex + "/" + subAttack.getLastIndex());
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
            long startTime = System.nanoTime();
            int subAttackID = 0;
            
            for (UUID slaveID : slavesWorking.keySet()) {
                
                if(initialIndex != finalIndex){
                    
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

                        System.out.println("SubAttack " + subAttackID + " created. Index range: " + initialIndex + "/" + finalIndex);
                        
                        initialIndex = finalIndex;
                        finalIndex += indexDivision;

                        if(finalIndex > dictionarySize)
                            finalIndex = dictionarySize;            

                    }
                    catch(RemoteException e){
                        failedSlaves.put(slaveID, sc);
                        
                        System.err.println("Slave failed: " + slaveID + " Name: " + sc.getName());

                        //Adjusting index for next slave
                        initialIndex = finalIndex;
                        finalIndex += indexDivision;

                        if(finalIndex > dictionarySize)
                            finalIndex = dictionarySize;            
                    }
                }//end of jobs distribuition
            }
            
            ////////////////TERMINAR PARA FAZER REDISTRIBUICAO ///////////////////////////////

//            verificar depois para caso que lista vazia e para redistribuir jobs qdo remover
            if(!failedSlaves.isEmpty()){
                
                for (UUID uid : failedSlaves.keySet()) {
                    try{ 
                        removeSlave(uid); 
                    }
                    catch(RemoteException e){ 
                        System.err.println("Attack Task error:\n" + e.getMessage());
                    }
                }
                
                //Getting the actual working slaves
                synchronized(slaves){
                    slavesWorking = new HashMap<>(slaves);
                }
                
                redistributionJobs(failedSlaves, slavesWorking, smRef);
            }
        }
    }
    
    public void redistributionJobs(Map<UUID, SlaveControl> failedSlaves, Map<UUID, SlaveControl> slavesWorking, SlaveManager smRef){
        
        //Checking if there are slaves registered.
//        while(slavesWorking.isEmpty()){
//            System.out.println("Waiting for slave to get added for redistribution");
//            try {
//                Thread.sleep(10000);
//                synchronized(slaves){
//                    slavesWorking = new HashMap<>(slaves);
//                }
//            } catch (InterruptedException ex) {
//                System.err.println("Attack waiting for slaves error.\n" + ex.getMessage());
//            }
//        }
        System.out.println("Starting redistribution");
        
        for (UUID failedSlaveID : failedSlaves.keySet()) {
            int subAttackID;
            SlaveControl s = failedSlaves.get(failedSlaveID);
            List<Integer> subAttacks = s.getSubAttackNumbersList();

            //Checking if some job of this slave didnt finish
            for (Integer subID : subAttacks) {
                int mainAttackID = attackMap.get(subID);

                AttackControl actualAttack = attacksList.get(mainAttackID);
                SubAttackControl sub = actualAttack.getSubAttacksMap().get(subID);

                //if not do redistribution
                if(!sub.isDone()){

                    long indexSize = sub.getLastIndex() - sub.getCurrentIndex();
                    long division = indexSize / slavesWorking.size();
                    long startIndex = sub.getCurrentIndex(); 
                    long endIndex = sub.getCurrentIndex() + division + (indexSize % slavesWorking.size());

                    for (UUID slaveID : slavesWorking.keySet()) {

                        if(startIndex != endIndex){

                            subAttackID = getSubAttackNumber();
                            SlaveControl sc = slavesWorking.get(slaveID);
                            Slave slRef = sc.getSlaveRef();

                            try{
                                SubAttackControl newSub = new SubAttackControl(startIndex, endIndex);

                                synchronized(slaves)
                                {
                                    slaves.get(slaveID).getSubAttackNumbersList().add(subAttackID);
                                }

                                synchronized(attacksList){
                                    actualAttack.getSubAttacksMap().put(subAttackID, newSub);
                                }

                                synchronized(attackMap){
                                    attackMap.put(subAttackID, mainAttackID);
                                }

                                slRef.startSubAttack(actualAttack.getCipherMessage(), actualAttack.getKnownText(), 
                                                     startIndex, endIndex, subAttackID, smRef);

                                System.out.println("SubAttack " + subAttackID + " created. Index range: " + startIndex + "/" + endIndex);
                                
                                startIndex = endIndex;
                                endIndex += division;

                                if(endIndex > sub.getLastIndex())
                                    endIndex = sub.getLastIndex();            
                            }
                            catch(RemoteException e){
                                System.err.println("Redistribution failed:\n" + e.getMessage());
                            }
                        }   
                    }//end of jobs redistribution
                }

                synchronized(attacksList){
                    sub.setDone(true);
                }
            }
        }
        System.out.println("End redistribution.");
    }
    
    /////////Attacker interfaces
    
    /**
     * Inicia um ataque. Chamado pelo cliente.
     * @param ciphertext Mensagem criptografada.
     * @param knowntext  Trecho conhecido da mensagem.
     * @return Vetor de guess encontrados
     */ 
    @Override
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) throws RemoteException {
        
        int attackID = getAttackNumber();
        
        //Creating a guess list for this attack
        synchronized(guessList){
            guessList.put(attackID, new ArrayList<>());
        }

        //Creating an attackControl for this attack
        synchronized(attacksList){
            attacksList.put(attackID, new AttackControl(ciphertext, knowntext));
        }
        
        //Checking if there are slaves registered.
        while(slaves.isEmpty()){
            System.out.println("Waiting for slave to get added");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                System.err.println("Attack waiting for slaves error.\n" +ex.getMessage());
            }
        }
        
        Thread attack = new AttackTask(this, attackID, ciphertext, knowntext);
        attack.start();
        
        try {
            attack.join();
        } catch (InterruptedException ex) {
            System.err.println("Attack error:\n " + ex.getMessage());
        }
        
        //Waiting end job
        boolean done;
        do{
            synchronized(attacksList){
                done = attacksList.get(attackID).isDone();
            }
            
            try{
                Thread.sleep(1000);
            }
            catch(InterruptedException e){
                System.err.println("Sleep done job error:\n" + e.getMessage());
            }
        }
        while(!done);
        
        synchronized(attacksList){
            attacksList.get(attackID).setDone(true);
        }
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
    
    public class MonitoringService extends TimerTask{
        
        SlaveManager callback;
        
        public MonitoringService(SlaveManager sm){
            this.callback = sm;
        }
        
        @Override
        public void run() {

            if(hasAttack()){
                Map<UUID, SlaveControl> downSlaves = new HashMap<>();
                Map<UUID, SlaveControl> slavesCopy;
            
                long currentTime = System.nanoTime();
            
                synchronized(slaves){
                        slavesCopy = new HashMap<>(slaves);
                    }
                
                for (UUID id : slavesCopy.keySet()) {
                    SlaveControl slave = slavesCopy.get(id);
                    double elapsedTime = (currentTime - slave.getTime())/1000000000;

                    if(elapsedTime > 20.0){
                        downSlaves.put(id, slave);
                        try{
                            removeSlave(id);
                        }
                        catch(RemoteException e){
                            System.err.println("Monitorign error:\n" + e.getMessage());
                        }
                    }
                }
            
                if(!downSlaves.isEmpty()){
                    
                    synchronized(slaves){
                        slavesCopy = new HashMap<>(slaves);
                    }

                    redistributionJobs(downSlaves, slavesCopy, callback);
                }
            }
        }
    }
}