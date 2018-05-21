package br.inf.ufes.ppd.implementation;

import br.inf.ufes.ppd.Slave;
import br.inf.ufes.ppd.SlaveManager;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

/** Slave implementation
 *
 * @author Leonardo Santos Paulucio
 */

public class SlaveImpl implements Slave {

    private static List<String> keys = new ArrayList<String>();
    private UUID uid;

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }
    
    /**
     * Realiza a leitura do dicionario.
     * @param filename Nome do arquivo de dicionario.
     */ 
    public void readDictionary(String filename) {

        try {
            Scanner dic = new Scanner(new File(filename));

            while (dic.hasNext()) {
                keys.add(dic.next());
            }

            dic.close();

        } catch (IOException e) {
            System.err.println("ReadDictionary error: \n" + e.getMessage());
        }

//       for (String s : keys) {
//           System.out.println(s);
//       }
//
//       System.out.println("Tamanho: " + keys.size());
    }

    /**
     * Implementação do startSubAttack
     * @param ciphertext Arquivo criptografado.
     * @param knowntext Trecho conhecido do arquivo criptografado.
     * @param initialwordindex Índice inicial do sub ataque.
     * @param finalwordindex Índice final do sub ataque.
     * @param attackNumber Número do sub ataque
     * @param callbackinterface  Interface do mestre para chamada de
     * checkpoint e foundGuess.
     * @see br.inf.ufes.ppd.services.SubAttackService
     */
    @Override
    public void startSubAttack(byte[] ciphertext, 
                               byte[] knowntext, 
                               long initialwordindex, 
                               long finalwordindex, 
                               int attackNumber, 
                               SlaveManager callbackinterface) 
        throws RemoteException {

        Thread subAttack = new SubAttackService(getUid(), ciphertext, knowntext, initialwordindex, 
                                                finalwordindex, attackNumber, callbackinterface, keys);
        
        subAttack.start();
    }         
}
