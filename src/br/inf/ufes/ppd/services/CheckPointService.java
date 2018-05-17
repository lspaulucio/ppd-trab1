package br.inf.ufes.ppd.services;

import br.inf.ufes.ppd.SlaveManager;
import java.util.TimerTask;
import java.util.UUID;

/**
 *
 * @author Leonardo Santos Paulucio
 */

public class CheckPointService extends TimerTask {

    private UUID uid;
    private int attackNumber;
    private long currentIndex;
    private SlaveManager smRef;
    
    public CheckPointService(UUID slaveID, int attackID, long index, SlaveManager sm){
        this.uid = slaveID;
        this.attackNumber = attackID;
        this.currentIndex = index;
        this.smRef = smRef;        
    }
    
    @Override
    public void run() {
        try{
                //Notify master about current index
                smRef.checkpoint(uid, attackNumber, currentIndex);
//                System.out.println("Checkpoint " + currentIndex);
            }
            catch (Exception e){
                System.err.println("Master error " + e.getMessage());
            }
        }   
}
