/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.inf.ufes.ppds;

import java.io.*;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.spec.*;

/**
 *
 * @author leonardo
 */
public class AttackerSerial {
        
    final static String DICTIONARY_PATH = "dictionary.txt";
    static List<String> keys = new ArrayList<String>();
        
    private static byte[] readFile(String filename) throws IOException {

        File file = new File(filename);
        InputStream is = new FileInputStream(file);
        long length = file.length();

        //creates array (assumes file length<Integer.MAX_VALUE)
        byte[] data = new byte[(int)length];

        int offset = 0;
        int count = 0;

        while((offset < data.length) && (count = is.read(data, offset, data.length-offset)) >= 0 ){
                offset += count;
        }
        is.close();
        return data;
    }

    private static void saveFile(String filename, byte[] data) throws IOException {

        FileOutputStream out = new FileOutputStream(filename);
        out.write(data);
        out.close();

    }
    
    private static void readDictionary(String filename){

        try{
            Scanner dic = new Scanner(new File(filename));
       
            while(dic.hasNext())
            {
                keys.add(dic.next());            
            }

            dic.close();
        
        }catch(IOException e){
            e.getMessage();
        }
        for(String s : keys);
//            System.out.println(s);
        System.out.println("Tamanho: " + keys.size());
    }
            
    

    public static void main(String[] args) {
        // args[0] e o nome do arquivo de entrada
        // args[1] e a frase conhecida
        
//        try {
//            //Abre o dicionario
//            Scanner file = new Scanner(new FileReader(DICTIONARY_PATH));
//            
//            String KNOWN_TEXT = args[1];
//            byte[] message = readFile(args[0]);
//            byte[] key = null;
//            
//            while(file.hasNext())
//            {
//                try{
//                    key = file.next().getBytes();
//                    SecretKeySpec keySpec;
//                    keySpec = new SecretKeySpec(key, "Blowfish");
//                    Cipher cipher = Cipher.getInstance("Blowfish");
//                    cipher.init(Cipher.DECRYPT_MODE, keySpec);
//
//                    byte[] decrypted = cipher.doFinal(message);
//
//                    String text = new String(decrypted);
//                    if(text.contains(KNOWN_TEXT))
//                    {
//                        System.out.println("Chave encontrada: " + new String(key));
//                        //saveFile(new String(key) + ".msg", decrypted);
//                    }
//                
//                } catch (javax.crypto.BadPaddingException e) {
//                    // essa excecao e jogada quando a senha esta incorreta
//                    // porem nao quer dizer que a senha esta correta se nao jogar essa excecao
//                    //System.out.println("Senha " + new String(key) + " invalida.");
//                }
//            }
//        
//            file.close();
//            
//        }catch (FileNotFoundException e) {
//            System.out.println(e.getMessage());
//            
//        }catch (Exception e){
//            //dont try this at home
//                e.printStackTrace();
//        }
        
        readDictionary(DICTIONARY_PATH);
    }

}
