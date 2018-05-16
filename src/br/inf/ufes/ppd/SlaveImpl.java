package br.inf.ufes.ppd;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Leonardo Santos Paulucio
 */
public class SlaveImpl implements Slave {

    private static List<String> keys = new ArrayList<String>();
    private UUID uid;
    private String name;
    private long currentIndex;

    public long getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(long currentIndex) {
        this.currentIndex = currentIndex;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    //Dictionary read function
    public void readDictionary(String filename) {

        try {
            Scanner dic = new Scanner(new File(filename));

            while (dic.hasNext()) {
                keys.add(dic.next());
            }

            dic.close();

        } catch (IOException e) {
            System.err.println("Erro " + e.getMessage());
        }

//       for (String s : keys) {
//           System.out.println(s);
//       }
//
//       System.out.println("Tamanho: " + keys.size());
    }

    @Override
    public void startSubAttack(byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, int attackNumber, SlaveManager callbackinterface) throws RemoteException {

        new Thread() {    
            public void run()
            {
                String KNOWN_TEXT = new String(knowntext);

                final int CHECK_TIMER = 10; //10 seconds
                Timer timer = new Timer();
                System.out.println("New Attack: " + attackNumber);
                
                //Subattack execution
                //Making a timer to notify master about currentIndex
                timer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        try{
                            //Notify master about current index
                            callbackinterface.checkpoint(uid, attackNumber, currentIndex);
                            System.out.println("Checkpoint " + currentIndex);
                        }
                        catch (Exception e){
                            System.err.println("Master down " + e.getMessage());

                        }
                    }
                }, 0, CHECK_TIMER*1000);  // 0 = delay, CHECK_TIMER = frequence in ms
                //End Checkpoint timer
                        
                for (currentIndex = initialwordindex; currentIndex < finalwordindex; currentIndex++) {

                    try {
                        String actualKey = keys.get((int) currentIndex); //Get current key

                        //Blowfish configuration
                        SecretKeySpec keySpec;
                        keySpec = new SecretKeySpec(actualKey.getBytes(), "Blowfish");
                        Cipher cipher = Cipher.getInstance("Blowfish");
                        cipher.init(Cipher.DECRYPT_MODE, keySpec); //Decrypt mode

                        //Try to decrypt the text
                        byte[] decrypted = cipher.doFinal(ciphertext);

                        String decryptedText = new String(decrypted);

                        //Checking if known text exists in decrypted text
                        if (decryptedText.contains(KNOWN_TEXT)) {

                            Guess currentGuess = new Guess();
                            currentGuess.setKey(actualKey);
                            currentGuess.setMessage(decrypted);

                            callbackinterface.foundGuess(uid, attackNumber, currentIndex, currentGuess);

        //                    System.out.println("Key found: " + actualKey);
                        }

                    } catch (javax.crypto.BadPaddingException e) {
                        // essa excecao e jogada quando a senha esta incorreta
                        // porem nao quer dizer que a senha esta correta se nao jogar essa excecao
                        //System.err.println("Senha " + new String(key) + " invalida.");
                    } catch (Exception e) {
                        System.err.println("Error startsubattack: " + e.getMessage());
                    }
                }
                timer.cancel(); //Finish task checkpoint
                try {
                    callbackinterface.checkpoint(uid, attackNumber, currentIndex); //End job sending last checkpoint            
                    System.out.println("Checkpoint " + currentIndex);
                }
                catch (Exception e){
                    System.err.println("Callback fail " + e.getMessage());                
                }
                System.out.println("End subattack: " + attackNumber);
            }
        }.start();   
    } 

    public static void main(String[] args) {

        final String DICTIONARY_PATH = "dictionary.txt";
        final String REGISTRY_MASTER_NAME = "mestre";
        final String REGISTRY_ADDRESS = "192.168.1.124";
        final int REBIND_TIME = 30;
        
        //Creating a new Slave
        SlaveImpl slave = new SlaveImpl();
        slave.readDictionary(DICTIONARY_PATH);
        slave.setName("SlaveLeonardo");
        slave.setUid(UUID.randomUUID());
 
        try {
            Registry registry = LocateRegistry.getRegistry(REGISTRY_ADDRESS);
            Master m = (Master) registry.lookup(REGISTRY_MASTER_NAME);
            
            Slave slaveRef = (Slave) UnicastRemoteObject.exportObject(slave,0);
        
            Timer timer = new Timer();
            
            //Making a timer to reconnect to the master every 30 seconds
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    try{
                        //Trying to rebind on master
                        m.addSlave(slaveRef, slave.getName(), slave.getUid());
                        System.out.println("Registrado");
                    }
                    catch (Exception e){
                        System.err.println("Master down " + e.getMessage());
                        
                        //Master down, so try to find another master on registry
                        try {
                           
                            Registry registry = LocateRegistry.getRegistry(REGISTRY_ADDRESS);
                            Master m = (Master) registry.lookup(REGISTRY_MASTER_NAME);
                            m.addSlave(slaveRef, slave.getName(), slave.getUid());
                        }
                        catch (Exception p)
                        {
                            System.err.println("Master not found " + p.getMessage() );
                        }
                    }
                }
            }, 0, REBIND_TIME*1000);  // 0 = delay, REBIND_TIME = frequence in ms
            
        
        } catch (Exception e) {
            System.err.println("Slave exception: " + e.getMessage());
            e.printStackTrace();
        }        
    }        
}
