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

public class Decrypt {
        
    /**
     * Descriptografa uma mensagem com a chave dada.
     * @param key Chave que será utilizada para descriptografar.
     * @param message Mensagem que será descriptografada.
     * @return Mensagem descriptografada.
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
}
