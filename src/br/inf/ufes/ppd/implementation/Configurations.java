package br.inf.ufes.ppd.implementation;

/** Classe com as configurações padrões.
 *
 * @author Leonardo Santos Paulucio
 */

public class Configurations {
    
    public static final String REGISTRY_ADDRESS = "192.168.1.4";
    public static final String REGISTRY_MASTER_NAME = "mestre";
    
    public static final String DICTIONARY_PATH = "dictionary.txt";
    
    public static final int DICTIONARY_SIZE = 80368;
    
    public static final int REBIND_TIME = 30000; //30 seconds
    public static final int CHECKPOINT_TIME = 10000; //10 seconds
}
