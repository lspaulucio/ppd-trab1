package br.inf.ufes.ppd.utils;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
     * Criptografador.
     * @author Leonardo Santos Paulucio
    */

public class Encrypt {

    /**
     * Criptografa uma cadeia de bytes com a chave dada.
     * @param key Chave que será utilizada para criptografar.
     * @param message Mensagem que se deseja criptografar.   
     * @return Mensagem criptografado.
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
}
