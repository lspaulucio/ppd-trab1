package br.inf.ufes.ppd.application;

import br.inf.ufes.ppd.implementation.Configurations;
import br.inf.ufes.ppd.utils.Decrypt;
import br.inf.ufes.ppd.utils.FileTools;
import java.io.*;
import java.util.*;

/** Attacker Serial Application
 *
 * @author Leonardo Santos Paulucio
 */
public class AttackerCentralized {
        
    public static void main(String[] args) {
            // args[0] e o nome do arquivo de entrada
            // args[1] e a frase conhecida
            
        try {
            //Abre o dicionario
            Scanner file = new Scanner(new FileReader(Configurations.DICTIONARY_PATH));
            
            String KNOWN_TEXT = args[1];//"JFIF";
            byte[] message = FileTools.readFile(args[0]);//"TestFiles/desafio.cipher"
            
            while(file.hasNext())
            {
                try{
                    byte[] key = file.next().getBytes();
                    byte[] decrypted = Decrypt.decrypter(key, message);

                    String text = new String(decrypted);
                    
                    if(text.contains(KNOWN_TEXT))
                    {
                        System.out.println("Key found: " + new String(key));
                        FileTools.saveResult(new String(key) + ".msg", decrypted);
                    }
                
                } catch (javax.crypto.BadPaddingException e) {
                    // essa excecao e jogada quando a senha esta incorreta
                    // porem nao quer dizer que a senha esta correta se nao jogar essa excecao
                    //System.out.println("Senha " + new String(key) + " invalida.");
                }
            }
        
            file.close();
            
        }catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
