/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.cli;

import jline.ConsoleReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 
 * Small JLine wrapper. Using JLine in scala caused NullPointerExceptions.
 * 
 * @author teamon
 */
public class Prompt {
    final ConsoleReader in = new ConsoleReader();
    
    public Prompt() throws IOException {
        
    }
    
    /** 
     * Show prompt, read line from stdin and return array of non-blank strings
     * 
     * @author teamon
     */
    public String[] readLine() throws IOException {
        String line = in.readLine(">>> ");
        String[] chunks = line.split(" ");
        List<String> args = new ArrayList<String>();
        
        for(String chunk : chunks){
            if(!chunks.equals("")) args.add(chunk);
        }
        
        return args.toArray(new String[0]);
    }
}
