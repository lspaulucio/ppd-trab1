package br.inf.ufes.ppd;

import java.io.*;
import java.util.*;

/**
 *
 * @author Leonardo Santos Paulucio
 */
public class Client {

    public static void main(String[] args)
    {
        final String DICTIONARY = "dictionary.txt";
        List<String> chaves = new ArrayList<String>();
        
        try {
            Scanner file = new Scanner(new FileReader(DICTIONARY));
        
            while (file.hasNext()) 
            {
                String word = file.next();
                chaves.add(word);
//                System.out.println(word);
            }
            System.out.println("Tamanho: " + chaves.size());
            System.out.println("Existe a palavra pink " + chaves.contains("pink"));
            
            
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    
    }  
}
