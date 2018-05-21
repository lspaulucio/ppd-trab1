package br.inf.ufes.ppd.tester;

import br.inf.ufes.ppd.implementation.Configurations;
import br.inf.ufes.ppd.utils.Decrypt;
import br.inf.ufes.ppd.utils.Encrypt;
import br.inf.ufes.ppd.utils.FileTools;
import br.inf.ufes.ppd.utils.Tupla;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Attacker Centralized.
 *  For automation of tests
 *
 * @author Leonardo Santos Paulucio
 */
public class AttackerCentralizedTester {
    
    private static List<String> keys;
    
    //args[0] random vector lengths or linearly
    //args[1] number of attacks
    //args[2] initial range of random bytes vector
    //args[3] final range of random bytes vector
    //args[4] number of samples
        
    public static void main(String[] args)
    { 
        try {
            
            if(args.length < 4){
                System.err.println("Missing parameters");
                throw new Exception("Attacker Centralized Tester.\nUsage: <s|r> <NumberAttacks> <InitialRange> <FinalRange> [<NumberOfSamples>]");
            }
            
            keys = FileTools.readDictionary(Configurations.DICTIONARY_PATH);

            Random rand = new Random();
            String type = args[0];
            
            int numberAttacks = new Integer(args[1]);
            int initialRange = new Integer(args[2]);
            int finalRange = new Integer(args[3]);
            int samples = (args.length < 5) ? Configurations.NUMBER_SAMPLES : new Integer(args[4]);
            int division = (finalRange - initialRange)/numberAttacks;
            
            byte[] knownText;
            byte[] encryptedText;
            double startTime, endTime;
            
            List<Tupla> dados = new ArrayList<>();
            
            for(int i = 0; i <= numberAttacks; i++){
                
                int key = rand.nextInt(Configurations.DICTIONARY_SIZE);
                int length;
                
                if(type.equals("r")){
                    length = rand.nextInt(finalRange - initialRange + 1) + initialRange;
                }
                else{
                    length = initialRange;
                    initialRange += division;
                }

                System.out.println("Size: " + length);
                
                encryptedText = new byte[length];
                rand.nextBytes(encryptedText);
                knownText = Arrays.copyOf(encryptedText, Configurations.KNOWN_TEXT_SIZE);
                encryptedText = Encrypt.encrypter(keys.get(key).getBytes(), encryptedText);
                
                System.out.println("Key " + i + ": " + keys.get(key));
//                System.out.println(knownText);            
//                System.out.println(encryptedText);
                
                startTime = System.nanoTime();
                
                for(int s = 0; s < samples; s++){
                    
                    for (String actualKey : keys) {
                        try{
                            byte[] decrypted = Decrypt.decrypter(actualKey.getBytes(), encryptedText);
                            String text = new String(decrypted);
                            String KNOWN = new String(knownText);

                            if(text.contains(KNOWN))
                            {
                                System.out.println("Key found: " + actualKey);
                            }
                        }
                        catch (javax.crypto.BadPaddingException e) {
                        // essa excecao e jogada quando a senha esta incorreta
                        // porem nao quer dizer que a senha esta correta se nao jogar essa excecao
                        //System.out.println("Senha " + new String(key) + " invalida.");
                        }
                    }
                }
                
                endTime = System.nanoTime() - startTime;
                
                endTime /= samples*1000000000;
                
                dados.add(new Tupla(endTime, length));
                
            }
            
            Tupla.generateCSV(dados);
            
        }
        catch(Exception p){
            System.err.println("Attacker error:\n" + p.getMessage());
            p.printStackTrace();
        }
    }  
}
