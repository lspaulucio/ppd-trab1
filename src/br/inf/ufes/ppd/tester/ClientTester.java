package br.inf.ufes.ppd.tester;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.Master;
import br.inf.ufes.ppd.implementation.Configurations;
import br.inf.ufes.ppd.utils.Encrypt;
import br.inf.ufes.ppd.utils.FileTools;
import br.inf.ufes.ppd.utils.Tupla;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/** Client Application Tester.
 *  For automation of the tests. 
 * @author Leonardo Santos Paulucio
 */

public class ClientTester {
    
    /**
     * Realiza a leitura do dicionario.
     * @param filename Nome do arquivo de dicionario.
     */ 
    static List<String> keys;
    
    public static void readDictionary(String filename) {
        keys = new ArrayList<>();
        try {
            Scanner dic = new Scanner(new File(filename));

            while (dic.hasNext()) {
                keys.add(dic.next());
            }

            dic.close();

        } catch (IOException e) {
            System.err.println("Client ReadDictionary error: \n" + e.getMessage());
        }
    }

    public static void main(String[] args)
    {              
        //args[0] number of attacks
        //args[1] initial range of random bytes vector
        //args[2] final range of random bytes vector
        
        try {
            
            if(args.length < 4){
                System.err.println("Missing parameters");
                throw new Exception("Usage: Client Tester <s|r> <NumberAttacks> <InitialRange> <FinalRange> [<NumberOfSamples>]");
            }
            
            readDictionary(Configurations.DICTIONARY_PATH);

            System.out.println("Client start");
            Registry registry = LocateRegistry.getRegistry(Configurations.REGISTRY_ADDRESS);
            Master m = (Master) registry.lookup(Configurations.REGISTRY_MASTER_NAME); 
            
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
                
                Guess[] guessVector = null;
                
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
                
                for(int s = 0; s < samples; s++)
                    guessVector = m.attack(encryptedText, knownText);
                
                endTime = System.nanoTime() - startTime;
                
                endTime /= samples*1000000000;
                
                dados.add(new Tupla(endTime, length));
                
                if(guessVector.length != 0){

                    for(int j = 0; j < guessVector.length; j++){
                        String file = "Results/" + guessVector[j].getKey() + ".msg";
//                        FileTools.saveResults(file, guessVector[j].getMessage());
                        System.out.println("Key found: " + guessVector[j].getKey());
                    }
                }
                else{
                    System.out.println("No keys found");
                }
            }
            
            Tupla.generateCSV(dados);
            
        }
        catch(RemoteException e)
        {
            System.err.println("Client - Master remote error:\n" + e.getMessage());
        }
        catch(Exception p){
            System.err.println("Client error:\n" + p.getMessage());
            p.printStackTrace();
        }
    }  
}

