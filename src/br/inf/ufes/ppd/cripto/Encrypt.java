package br.inf.ufes.ppd.cripto;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Encrypt {

    public static byte[] encrypter(byte[] key, byte[] message) throws BadPaddingException{    
        
        byte[] encrypted = null;   
        
        try{
            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            encrypted = cipher.doFinal(message);
            
            }catch (NoSuchAlgorithmException | NoSuchPaddingException | 
                    IllegalBlockSizeException | InvalidKeyException e) {
                
                System.err.println("Decrypter error: \n " + e.getMessage());                
            }
        
        return encrypted;
    }
}
