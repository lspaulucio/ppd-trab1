package br.inf.ufes.ppd.services;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.SlaveManager;
import br.inf.ufes.ppd.implementation.Configurations;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Leonardo Santos Paulucio
 */
public class SubAttackService extends Thread {
    
    UUID uid;
    byte[] encryptedText;
    byte[] knownText;
    long currentIndex;
    long finalIndex;
    int attackID;
    SlaveManager smRef;
    private List<String> keys;
        
    public SubAttackService(UUID id, byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, int attackNumber,
        SlaveManager callbackinterface, List<String> keysList){
        this.uid = id;
        this.encryptedText = ciphertext;
        this.knownText = knowntext;
        this.currentIndex = initialwordindex;
        this.finalIndex = finalwordindex;
        this.attackID = attackNumber;
        this.smRef = callbackinterface;
        this.keys = keysList;
    }
    
    public void run()
    {
        
        String KNOWN_TEXT = new String(knownText);

        System.out.println("New Attack: " + attackID);

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
                    smRef.checkpoint(uid, attackID, currentIndex);
                    System.out.println("Checkpoint " + currentIndex);
                }
                catch (Exception e){
                    System.err.println("Master down " + e.getMessage());

                }
            }
        }, 0, Configurations.CHECKPOINT_TIME);  // 0 = delay, CHECK_TIMER = frequence
        //End Checkpoint timer

        //Subattack execution
        for (; currentIndex < finalIndex; currentIndex++) {

            try {
                String actualKey = keys.get((int) currentIndex); //Get current key

                //Blowfish configuration
                SecretKeySpec keySpec;
                keySpec = new SecretKeySpec(actualKey.getBytes(), "Blowfish");
                Cipher cipher = Cipher.getInstance("Blowfish");
                cipher.init(Cipher.DECRYPT_MODE, keySpec); //Decrypt mode

                //Try to decrypt the text
                byte[] decrypted = cipher.doFinal(encryptedText);

                String decryptedText = new String(decrypted);

                //Checking if known text exists in decrypted text
                if (decryptedText.contains(KNOWN_TEXT)) {

                    Guess currentGuess = new Guess();
                    currentGuess.setKey(actualKey);
                    currentGuess.setMessage(decrypted);

                    smRef.foundGuess(uid, attackID, currentIndex, currentGuess);

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
            smRef.checkpoint(uid, attackID, currentIndex); //End job sending last checkpoint            
            System.out.println("Checkpoint " + currentIndex);
        }
        catch (Exception e){
            System.err.println("Callback fail " + e.getMessage());                
        }
        System.out.println("End subattack: " + attackID);
    }
    
}