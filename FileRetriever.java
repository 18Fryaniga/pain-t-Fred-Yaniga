/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pain.t;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 18fry
 */
public class FileRetriever {
    private String text = "";
    private String path = Paths.get("").toAbsolutePath().toString();
    
    public String getText() throws FileNotFoundException{
        read();
        return text;
    }
    private void read(){
      File file=new File(path+ "\\src\\pain\\t\\Resources\\ReleaseNotes.txt");
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }
            while (sc.hasNextLine()) { 
                text = text.concat(sc.nextLine()+"\n");
    }  
    }
    public void main(String[]args) throws FileNotFoundException, IOException{
        
     
       
    }
}
