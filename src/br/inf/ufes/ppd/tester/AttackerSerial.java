package br.inf.ufes.ppd.tester;

import br.inf.ufes.ppd.cripto.Decrypt;
import java.io.*;
import java.util.*;

/** Attacker Serial Application
 *
 * @author Leonardo Santos Paulucio
 */
public class AttackerSerial {
        
    final static String DICTIONARY_PATH = "dictionary.txt";
    static List<String> keys = new ArrayList<String>();
    
    long currentIndex;
    
    /**
     * Realiza a leitura de um arquivo.
     * @param filename Nome do arquivo que se deseja ler.
     * @return Vetor de bytes do arquivo lido.
    */
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

    /**
     * Realiza o salvamento de um vetor de bytes.
     * @param filename Nome do arquivo que será gerado.
     * @param data Vetor de bytes que se deseja salvar.
    */
    private static void saveFile(String filename, byte[] data) throws IOException {

        FileOutputStream out = new FileOutputStream(filename);
        out.write(data);
        out.close();

    }
    
    /**
     * Realiza a leitura do dicionário.
     * @param filename Nome do arquivo de dicionario.
    */
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
//        for(String s : keys);
//            System.out.println(s);
//        System.out.println("Tamanho: " + keys.size());
    }
            
    public static void main(String[] args) {
            // args[0] e o nome do arquivo de entrada
            // args[1] e a frase conhecida
            
        try {
            //Abre o dicionario
            Scanner file = new Scanner(new FileReader(DICTIONARY_PATH));
            
            String KNOWN_TEXT = "JFIF";//args[1];
            byte[] message = readFile("desafio.cipher");////args[0]);
            byte[] key = null;
            
            while(file.hasNext())
            {
                try{
                    key = file.next().getBytes();
                    
                    byte[] decrypted = Decrypt.decrypter(key, message);

                    String text = new String(decrypted);
                    if(text.contains(KNOWN_TEXT))
                    {
                        System.out.println("Chave encontrada: " + new String(key));
                        saveFile(new String(key) + ".msg", decrypted);
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
        
//        readDictionary(DICTIONARY_PATH);
    }

}
