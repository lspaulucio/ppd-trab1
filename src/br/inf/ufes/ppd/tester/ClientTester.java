package br.inf.ufes.ppd.tester;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.Master;
import br.inf.ufes.ppd.cripto.Encrypt;
import br.inf.ufes.ppd.implementation.Configurations;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

    public static class Tupla{
        long length;
        double time;
        
        public Tupla(double t, long l){
            this.time = t;
            this.length = l;
        }
    }
    
    public static void main(String[] args)
    {              
        //args[0] number of attacks
        //args[1] initial range of random bytes vector
        //args[2] final range of random bytes vector
        
        try {
            
            if(args.length < 3){
                System.err.println("Missing parameters");
                throw new Exception("Usage: Client Tester <NumberAttacks> <InitialRange> <FinalRange>");
            }
            
            readDictionary(Configurations.DICTIONARY_PATH);
            
            System.out.println("Client start");
            Registry registry = LocateRegistry.getRegistry(Configurations.REGISTRY_ADDRESS);
            Master m = (Master) registry.lookup(Configurations.REGISTRY_MASTER_NAME); 
            
            Random rand = new Random();
            
            int numberAttacks = new Integer(args[0]);
            int initialRange = new Integer(args[1]);
            int finalRange = new Integer(args[2]);
            byte[] knownText;
            byte[] encryptedText;
            double startTime, endTime;
            List<Tupla> dados = new ArrayList<>();
            
            for(int i = 0; i < numberAttacks; i++){
                
                int key = rand.nextInt(Configurations.DICTIONARY_SIZE);
                int length = rand.nextInt(finalRange - initialRange + 1) + initialRange;
                encryptedText = new byte[length];
                rand.nextBytes(encryptedText);
                knownText = Arrays.copyOf(encryptedText, 20);
                encryptedText = Encrypt.encrypter(keys.get(key).getBytes(), encryptedText);
                
                
                System.out.println("Key " + i + ": " + keys.get(key));
//                System.out.println(knownText);            
//                System.out.println(encryptedText);
                startTime = System.nanoTime();
                
                Guess[] guessVector = m.attack(encryptedText, knownText);
                
                endTime = System.nanoTime() - startTime;
                
                endTime /= 1000000000;
                
                dados.add(new Tupla(endTime, length));
                
                if(guessVector.length != 0){

                    for(int j = 0; j < guessVector.length; j++){
                        String file = "Results/" + guessVector[j].getKey() + ".msg";
                        //saveFile(file, guessVector[j].getMessage());
                        System.out.println("Key found: " + guessVector[j].getKey());
                    }
                }
                else{
                    System.out.println("No keys found");
                }
            }
            
            generateCSV(dados);
            
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
    
    public static void generateCSV(List<Tupla> t){
        
        String filename = "Results/dados.csv";
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(filename)));
            out.write("Tamanho da Mensagem;Tempo de Resposta\n");
            
            for (Tupla tupla : t) {
                out.write(Long.toString(tupla.length) + ";" + Double.toString(tupla.time).replace(".", ",") + "\n");                
            }
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    
}

