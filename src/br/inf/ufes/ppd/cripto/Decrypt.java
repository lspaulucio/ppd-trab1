package br.inf.ufes.ppd.cripto;

import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.*;

public class Decrypt {
        
    public static byte[] decrypter(List<String> keys, byte[] key, byte[] message){
        byte[] decrypted = null;
        
        try{
            
            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");

            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            System.out.println("message size (bytes) = "+ message.length);

            decrypted = cipher.doFinal(message);
        }
        catch (javax.crypto.BadPaddingException e) {
                // essa excecao e jogada quando a senha esta incorreta
                // porem nao quer dizer que a senha esta correta se nao jogar essa excecao
                //System.err.println("Senha " + new String(key) + " invalida.");
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        return decrypted;
    }

}
