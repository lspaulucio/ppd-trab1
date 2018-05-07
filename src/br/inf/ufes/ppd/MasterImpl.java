package br.inf.ufes.ppd;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Leonardo Santos Paulucio
 */
public class MasterImpl implements Master 
{
    private List<Slave> slaves = new ArrayList<Slave>();
    
    //SlaveManager interfaces
    @Override
    public synchronized void addSlave(Slave s, String slaveName, UUID slavekey) throws RemoteException {
        slaves.add(s);
    }

    @Override
    public synchronized void removeSlave(UUID slaveKey) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void foundGuess(UUID slaveKey, int attackNumber, long currentindex, Guess currentguess) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkpoint(UUID slaveKey, int attackNumber, long currentindex) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //Attacker interfaces
    @Override
    public Guess[] attack(byte[] ciphertext, byte[] knowntext) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
