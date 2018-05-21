package br.inf.ufes.ppd.implementation;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.SlaveManager;
import br.inf.ufes.ppd.cripto.Decrypt;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/** Classe responsável por executar o subAttack.
 *
 * @author Leonardo Santos Paulucio
 */

public class SubAttackService extends Thread {
    
    private UUID uid;
    private byte[] encryptedText;
    private byte[] knownText;
    private long currentIndex;
    private long finalIndex;
    private int subAttackID;
    private SlaveManager smRef;
    private List<String> keys;
        
    /**
     * Construtor do serviço de sub ataque.
     * @param id identificador único do escravo.
     * @param ciphertext Arquivo criptografado.
     * @param knowntext Trecho conhecido do arquivo criptografado.
     * @param initialwordindex Índice inicial do sub ataque.
     * @param finalwordindex Índice final do sub ataque.
     * @param attackNumber Número do sub ataque
     * @param callbackinterface  Interface do mestre para chamada de
     * checkpoint e foundGuess.
     * @param keysList Lista com chaves do dicionário.
     * @see CheckPointTask
     * @see br.inf.ufes.ppd.implementation.RebindService
     */
    
    public SubAttackService(UUID id, 
                            byte[] ciphertext, 
                            byte[] knowntext, 
                            long initialwordindex,
                            long finalwordindex, 
                            int attackNumber, 
                            SlaveManager callbackinterface, 
                            List<String> keysList){
        this.uid = id;
        this.encryptedText = ciphertext;
        this.knownText = knowntext;
        this.currentIndex = initialwordindex;
        this.finalIndex = finalwordindex;
        this.subAttackID = attackNumber;
        this.smRef = callbackinterface;
        this.keys = keysList;
    }
    
    /**
     * Classe interna responsável por executar o serviço de checkpoint.
     * @author Leonardo Santos Paulucio
    */
    private class CheckPointTask extends TimerTask{

        @Override
        public void run() {
            try{
                //Notify master about current index
                smRef.checkpoint(uid, subAttackID, currentIndex);
                System.out.println("Checkpoint " + currentIndex);
            }
            catch (RemoteException e){
                System.err.println("Master down:\n" + e.getMessage());
            }
        }
    }
    
    public void run()
    {
        String KNOWN_TEXT = new String(knownText);

        System.out.println("New SubAttack: " + subAttackID);
        //Making a timer to notify master about currentIndex
        Timer timer = new Timer();

        CheckPointTask checkTask = new CheckPointTask();
        timer.scheduleAtFixedRate(checkTask, 0, Configurations.CHECKPOINT_TIME);  // 0 = delay, CHECKPOINT_TIME = frequence

        //Subattack execution
        for (; currentIndex < finalIndex; currentIndex++) {

            try {
                String actualKey = keys.get((int) currentIndex); //Get current key

                byte[] decrypted = Decrypt.decrypter(actualKey.getBytes(), encryptedText);

                String decryptedText = new String(decrypted);

                //Checking if known text exists in decrypted text
                if (decryptedText.contains(KNOWN_TEXT)) {

                    Guess currentGuess = new Guess();
                    currentGuess.setKey(actualKey);
                    currentGuess.setMessage(decrypted);

                    smRef.foundGuess(uid, subAttackID, currentIndex, currentGuess);

//                    System.out.println("Key found: " + actualKey);
                }

            } catch (javax.crypto.BadPaddingException e) {
                // essa excecao e jogada quando a senha esta incorreta
                // porem nao quer dizer que a senha esta correta se nao jogar essa excecao
                //System.err.println("Senha " + new String(key) + " invalida.");
            } catch (RemoteException e) {
                System.err.println("Error subattack service:\n" + e.getMessage());
            }
        }

        timer.cancel(); //Closing task checkpoint

        try {
            smRef.checkpoint(uid, subAttackID, currentIndex); //End job sending last checkpoint            
            System.out.println("Final checkpoint " + currentIndex);
        }
        catch (RemoteException e){
            System.err.println("Subattack callback fail:\n" + e.getMessage());                
        }
        System.out.println("End subattack: " + subAttackID);
    }
    
}
