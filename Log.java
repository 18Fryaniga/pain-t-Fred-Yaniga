/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pain.t;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author 18fry
 */
public class Log {
    
    public Logger logger;
    FileHandler fh;
    
    
   
   public Log() throws SecurityException, IOException{
       String path = Paths.get("").toAbsolutePath().toString();
       fh = new FileHandler(path+"\\src\\pain\\t\\Resources\\log.txt",true);
       logger = Logger.getLogger("test");
       logger.addHandler(fh);
       SimpleFormatter formatter = new SimpleFormatter();
       fh.setFormatter(formatter);
       
} 
   
   
   
}
