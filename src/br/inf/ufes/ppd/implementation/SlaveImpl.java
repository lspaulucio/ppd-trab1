package br.inf.ufes.ppd.implementation;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.Slave;
import br.inf.ufes.ppd.SlaveManager;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
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
                
                System.out.println("New Attack: " + attackNumber);
                
                //Making a timer to notify master about currentIndex
                Timer timer = new Timer();

//                CheckPointService checkServ = new CheckPointService(uid, attackNumber, currentIndex, callbackinterface);
//                timer.scheduleAtFixedRate(checkServ, 0, Configurations.CHECKPOINT_TIME);  // 0 = delay, CHECK_TIMER = frequence

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
                }, 0, Configurations.CHECKPOINT_TIME);  // 0 = delay, CHECK_TIMER = frequence
                //End Checkpoint timer
                
                //Subattack execution
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
                
                timer.cancel(); //Closing task checkpoint
                
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
}
