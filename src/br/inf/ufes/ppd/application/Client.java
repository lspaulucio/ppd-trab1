package br.inf.ufes.ppd.application;

import br.inf.ufes.ppd.Guess;
import br.inf.ufes.ppd.Master;
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
    private static byte[] readFile(String filename) throws IOException
    {
        File file = new File(filename);
        InputStream is = new FileInputStream(file);
        long length = file.length();
        // creates array (assumes file length<Integer.MAX_VALUE)
        byte[] data = new byte[(int)length];
        int offset = 0; int count = 0;
        while ((offset < data.length) &&
                        (count=is.read(data, offset, data.length-offset)) >= 0) {
            offset += count;
        }
        is.close();
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

    public static void main(String[] args)
    {              
        try {
            
            //Adicionar terceiro parametro
//            if(args[0] == null || args[1] == null){
//                System.err.println("Missing parameters");
//            }
            
            String filename = "desafio.cipher";// args[0];
            String knowText = "JFIF";//args[1];
            
            byte[] encryptedText = readFile(filename);
            System.out.println("Teste");
            Registry registry = LocateRegistry.getRegistry(Configurations.REGISTRY_ADDRESS);
            Master m = (Master) registry.lookup(Configurations.REGISTRY_MASTER_NAME); 
            
            Guess[] guessVector = m.attack(encryptedText, knowText.getBytes());
            
            if(guessVector.length != 0){

                for(int i = 0; i < guessVector.length; i++){
                    String file = guessVector[i].getKey() + ".msg";
                    saveFile(file, guessVector[i].getMessage());
                    System.out.println("Key found: " + guessVector[i].getKey());
                }
            }
            else{
                System.out.println("No keys found");
            }
            
        }
        catch(RemoteException e)
        {
            System.err.println("Client remote error:\n" + e.getMessage());
        }
        catch(Exception p){
            System.err.println("Client error:\n" + p.getMessage());
            p.printStackTrace();
        }
    
    }  
}
