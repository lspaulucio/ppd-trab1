package br.inf.ufes.ppd.implementation;

/** Classe com as configurações padrões.
 *
 * @author Leonardo Santos Paulucio
 */

public class Configurations {
    
    public static final String REGISTRY_ADDRESS = "192.168.1.4";
    public static final String REGISTRY_MASTER_NAME = "mestre";
    
    public static final String DICTIONARY_PATH = "dictionary.txt";
    public static final String RESULTS_PATH = "Results/";
    
    public static final int NUMBER_SAMPLES = 5;
    public static final int KNOWN_TEXT_SIZE = 10;
    
    public static final int DICTIONARY_SIZE = 80368;
    
    public static final int REBIND_TIME = 30000; //30 seconds
    public static final int CHECKPOINT_TIME = 10000; //10 seconds
    public static final int TIMEOUT = 20000; //20 seconds
}
