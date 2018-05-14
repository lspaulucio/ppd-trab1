package br.inf.ufes.ppd;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/**
 *
 * @author Leonardo Santos Paulucio
 */

public class Client {
    
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

    private static void saveFile(String filename, byte[] data) throws IOException
    {
        FileOutputStream out = new FileOutputStream(filename);
        out.write(data);
        out.close();
    }

    public static void main(String[] args)
    {       
        final String DICTIONARY_PATH = "dictionary.txt";
        final String REGISTRY_MASTER_NAME = "mestre";
        final String REGISTRY_ADDRESS = "localhost";
        
        try {
            byte[] encryptedText = readFile("texto.txt.cipher");
        
            Registry registry = LocateRegistry.getRegistry(REGISTRY_ADDRESS);
            Master m = (Master) registry.lookup(REGISTRY_MASTER_NAME); 
            
            Guess[] guessVector = m.attack(encryptedText, new String("algum desses").getBytes());
            
            //Ainda tem que terminar
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    
    }  
}
