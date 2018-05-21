package br.inf.ufes.ppd.application;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.Master;
import br.inf.ufes.ppd.implementation.Configurations;
import br.inf.ufes.ppd.utils.Encrypt;
import br.inf.ufes.ppd.utils.FileTools;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/** Client Application
 *
 * @author Leonardo Santos Paulucio
 */

public class Client {
    
    static List<String> keys;
    
    public static void main(String[] args)
    {              
        //args[0] Cipher file
        //args[1] Known text
        //args[2] Random bytes vector length
        
        try {
            
//          Adicionar terceiro parametro
            if(args.length < 2){
                System.err.println("Missing parameters");
                throw new Exception("Usage: Client <filename> <knowtext> [<randomVectorLength> : optional]");
            }
            
            Random rand = new Random();
            String filename = args[0];
            String knownText = args[1];
            byte[] encryptedText;
            
            encryptedText = FileTools.readFile(filename);
            
            //If file doesn't exist, generate a random bytes vector
            if(encryptedText == null){
                keys = FileTools.readDictionary(Configurations.DICTIONARY_PATH);
                int key = rand.nextInt(Configurations.DICTIONARY_SIZE);
                int length = (args.length < 3) ? (rand.nextInt(100000 -1000 + 1) + 1000) : new Integer(args[2]);
                
                System.out.println("A random bytes vector will be generated. Random vector size: " + length);
                
                encryptedText = new byte[length];
                new Random().nextBytes(encryptedText);
                knownText = new String(encryptedText, 0, 10);
                encryptedText = Encrypt.encrypter(keys.get(key).getBytes(), encryptedText);
                System.out.println("Key: " + keys.get(key));
            }
            
            System.out.println("Client start");
            
//            System.out.println(encryptedText);
            Registry registry = LocateRegistry.getRegistry(Configurations.REGISTRY_ADDRESS);
            Master m = (Master) registry.lookup(Configurations.REGISTRY_MASTER_NAME); 
            
            Guess[] guessVector = m.attack(encryptedText, knownText.getBytes());
            
            if(guessVector.length != 0){

                for(int i = 0; i < guessVector.length; i++){
                    String file = guessVector[i].getKey() + ".msg";
                    FileTools.saveResults(file, guessVector[i].getMessage());
                    System.out.println("Key found: " + guessVector[i].getKey());
                }
            }
            else{
                System.out.println("No keys found");
            }
        }
        catch(RemoteException e){
            System.err.println("Client - Master remote error:\n" + e.getMessage());
        }
        catch(Exception p){
            System.err.println("Client error:\n" + p.getMessage());
            p.printStackTrace();
        }
    }  
}
