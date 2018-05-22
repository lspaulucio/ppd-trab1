package br.inf.ufes.ppd.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.*;

/**
     * Descriptografador.
     * @author Leonardo Santos Paulucio
    */

public class Crypto {
        
    /**
     * Descriptografa uma mensagem com a chave dada.
     * @param key Chave que será utilizada para descriptografar.
     * @param message Mensagem que será descriptografada.
     * @return Mensagem descriptografada.
     * @throws javax.crypto.BadPaddingException
    */
    public static byte[] decrypter(byte[] key, byte[] message) throws BadPaddingException{
        
        byte[] decrypted = null;   
        
        try{
            
            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            decrypted = cipher.doFinal(message);
            
            }catch (NoSuchAlgorithmException | NoSuchPaddingException | 
                    IllegalBlockSizeException | InvalidKeyException e) {
                
                System.err.println("Decrypter error: \n " + e.getMessage());                
            }
        
        return decrypted;
    }
    
    /**
     * Criptografa uma cadeia de bytes com a chave dada.
     * @param key Chave que será utilizada para criptografar.
     * @param message Mensagem que se deseja criptografar.   
     * @return Mensagem criptografado.
     * @throws javax.crypto.BadPaddingException
    */
    public static byte[] encrypter(byte[] key, byte[] message) throws BadPaddingException{    
        
        byte[] encrypted = null;   
        
        try{
            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            encrypted = cipher.doFinal(message);
            
            }catch (NoSuchAlgorithmException | NoSuchPaddingException | 
                    IllegalBlockSizeException | InvalidKeyException e) {
                
                System.err.println("Encrypter error: \n " + e.getMessage());                
            }
        
        return encrypted;
    }
    
    public static boolean contains(byte[] array, byte[] sequence){
        
        boolean exists = true;
        
        for(int i = 0; i < array.length; i++){
            
            if(array[i] == sequence[0]){
                
                for(int j = 1; j < sequence.length; j++){
                    
                    if( i + j >= array.length){
                        break;
                    }    
                        if(array[i+j] != sequence[j]){
                            exists = false;
                            break;
                        }
                    
                }
                
                if(exists){
                    return true;
                }
            }
        }
        
        return true;
    }
}
