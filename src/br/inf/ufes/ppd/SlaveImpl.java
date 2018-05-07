package br.inf.ufes.ppd;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Leonardo Santos Paulucio
 */
public class SlaveImpl implements Slave {

    private List<String> keys = new ArrayList<String>();

    public void readDictionary(String filename) {

        try {
            Scanner dic = new Scanner(new File(filename));

            while (dic.hasNext()) {
                keys.add(dic.next());
            }

            dic.close();

        } catch (IOException e) {
            e.getMessage();
        }

//       for (String s : keys) {
//           System.out.println(s);
//       }
//
//       System.out.println("Tamanho: " + keys.size());
    }

    @Override
    public void startSubAttack(byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, int attackNumber, SlaveManager callbackinterface) throws RemoteException {

        String KNOWN_TEXT = new String(knowntext);

        for (long i = initialwordindex; i <= finalwordindex; i++) {

            try {
                String actualKey = keys.get((int) i);
                SecretKeySpec keySpec;
                keySpec = new SecretKeySpec(actualKey.getBytes(), "Blowfish");
                Cipher cipher = Cipher.getInstance("Blowfish");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);

                byte[] decrypted = cipher.doFinal(ciphertext);

                String decryptedText = new String(decrypted);

                if (decryptedText.contains(KNOWN_TEXT)) {
                    System.out.println("Chave encontrada: " + actualKey);
                    //CRIAR GUESS E ENVIAR AO MESTRE
                    //saveFile(new String(key) + ".msg", decrypted);
                }

            } catch (javax.crypto.BadPaddingException e) {
                // essa excecao e jogada quando a senha esta incorreta
                // porem nao quer dizer que a senha esta correta se nao jogar essa excecao
                //System.out.println("Senha " + new String(key) + " invalida.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        final String DICTIONARY_PATH = "dictionary.txt";
        final String REGISTRY_NAME = "mestre";
        final String REGISTRY_ADDRESS = "localhost";

        //Creating a new Slave
        SlaveImpl s = new SlaveImpl();
        s.readDictionary(DICTIONARY_PATH);
        UUID uuid = UUID.randomUUID();
        
        try {
            Registry registry = LocateRegistry.getRegistry(REGISTRY_ADDRESS);
            Master m = (Master) registry.lookup(REGISTRY_NAME);
            m.addSlave((Slave) s, "Slave1", uuid);

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();

        }
    }
}
