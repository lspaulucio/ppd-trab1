package br.inf.ufes.ppd.application;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.Master;
import br.inf.ufes.ppd.cripto.Encrypt;
import br.inf.ufes.ppd.implementation.Configurations;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/** Client Application
 *
 * @author Leonardo Santos Paulucio
 */

public class Client {
    
    /**
     * Realiza a leitura de um arquivo.
     * @param filename Nome do arquivo que se deseja ler.
     * @return Vetor de bytes do arquivo lido.
    */
    private static byte[] readFile(String filename)
    {
        byte[] data = null;
        try{
            File file = new File(filename);
            InputStream is = new FileInputStream(file);
            long length = file.length();
            // creates array (assumes file length<Integer.MAX_VALUE)
            data = new byte[(int)length];
            int offset = 0; int count = 0;
            while ((offset < data.length) &&
                            (count=is.read(data, offset, data.length-offset)) >= 0) {
                offset += count;
            }
            is.close();
        }
        catch(IOException e){
            System.err.println("File not found. A random bytes vector will be created");
        }
        return data;
    }

    /**
     * Salva um vetor de bytes em um arquivo.
     * @param filename Nome do arquivo que será gerado.
     * @param data Vetor de bytes a ser gravado.
    */
    private static void saveFile(String filename, byte[] data) throws IOException
    {
        FileOutputStream out = new FileOutputStream(filename);
        out.write(data);
        out.close();
    }
    
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
        //args[0] Cipher file
        //args[1] Known text
        //args[2] Random bytes vector length
        
        try {
            
////          Adicionar terceiro parametro
//            if(args.length < 2){
//                System.err.println("Missing parameters");
//                throw new Exception("Usage: Client <filename> <knowtext> [<randomVectorLength> : optional]");
//            }
//            
//            Random rand = new Random();
//            String filename = args[0];
//            String knowText = args[1];
//            byte[] encryptedText;
//            
//            encryptedText = readFile(filename);
//            
//            if(encryptedText == null){
//                readDictionary(Configurations.DICTIONARY_PATH);
//                int key = rand.nextInt(Configurations.DICTIONARY_SIZE);
//                int length = (args[2] != null) ? new Integer(args[2]) : rand.nextInt(100000 -1000 + 1) + 1000;
//                encryptedText = new byte[length];
//                new Random().nextBytes(encryptedText);
//                encryptedText = Encrypt.encrypter(keys.get(key).getBytes(), encryptedText);
//                knowText = new String(encryptedText, 0, 5);
//                System.out.println("Key: " + keys.get(key));
//            }
//            
//            System.out.println("Client start");
//            
//            System.out.println(encryptedText);
////            Registry registry = LocateRegistry.getRegistry(Configurations.REGISTRY_ADDRESS);
////            Master m = (Master) registry.lookup(Configurations.REGISTRY_MASTER_NAME); 
////            
////            Guess[] guessVector = m.attack(encryptedText, knowText.getBytes());
////            
////            if(guessVector.length != 0){
////
////                for(int i = 0; i < guessVector.length; i++){
////                    String file = "Results/" + guessVector[i].getKey() + ".msg";
////                    saveFile(file, guessVector[i].getMessage());
////                    System.out.println("Key found: " + guessVector[i].getKey());
////                }
////            }
////            else{
////                System.out.println("No keys found");
////            }


            String filename = "TestFiles/desafio.cipher";// args[0];
            String knowText = "JFIF";//args[1];
            
            byte[] encryptedText = readFile(filename);
            System.out.println("Teste");
            Registry registry = LocateRegistry.getRegistry(Configurations.REGISTRY_ADDRESS);
            Master m = (Master) registry.lookup(Configurations.REGISTRY_MASTER_NAME); 
            
            Guess[] guessVector = m.attack(encryptedText, knowText.getBytes());
            
            if(guessVector.length != 0){

                for(int i = 0; i < guessVector.length; i++){
                    String file = guessVector[i].getKey() + ".msg";
                    //saveFile(file, guessVector[i].getMessage());
                    System.out.println("Key found: " + guessVector[i].getKey());
                }
            }
            else{
                System.out.println("No keys found");
            }
            
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
