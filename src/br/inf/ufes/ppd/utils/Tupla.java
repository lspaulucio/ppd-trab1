/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.inf.ufes.ppd.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Leonardo Santos Paulucio
 */
public class Tupla {
    private long length;
    private double time;

    public Tupla(double t, long l){
        this.time = t;
        this.length = l;
    }
    
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
    
    public static void generateCSV(List<Tupla> t){
        
        String filename = "Results/dados.csv";
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(filename)));
            out.write("Tamanho da Mensagem;Tempo de Resposta\n");
            
            for (Tupla tupla : t) {
                out.write(Long.toString(tupla.getLength()) + ";" + Double.toString(tupla.getTime()).replace(".", ",") + "\n");                
            }
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
}
