package br.inf.ufes.ppd.implementation;

import br.inf.ufes.ppd.Slave;
import br.inf.ufes.ppd.SlaveManager;
import br.inf.ufes.ppd.services.SubAttackService;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

/**
 *
 * @author Leonardo Santos Paulucio
 */

public class SlaveImpl implements Slave {

    private static List<String> keys = new ArrayList<String>();
    private UUID uid;
    private long currentIndex;

    public long getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(long currentIndex) {
        this.currentIndex = currentIndex;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }
    
    //Dictionary read function
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

    @Override
    public void startSubAttack(byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, int attackNumber, SlaveManager callbackinterface) throws RemoteException {

        Thread subAttack = new SubAttackService(uid, ciphertext, knowntext, initialwordindex, 
                                                finalwordindex, attackNumber, callbackinterface, keys);
        
        subAttack.start();
    }         
}
