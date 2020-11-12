/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pain.t;

import java.io.IOException;
import javafx.application.Platform;
import java.util.logging.Level;
/**
 *
 * @author 18fry
 */
public class LogActivity implements Runnable{
    int time;
    String tool;
    boolean isSaved;
    Log save_data;
    public LogActivity(int t) throws SecurityException, IOException{
        time = t*1000;
        save_data = new Log();
    }

    @Override
    public void run() {
        try {
                while(true){
                   System.out.println("started logTimer");
                   Thread.sleep(time);
                   Platform.runLater(()->{
                   tool = (PainT.selectedToolLabel.getText());
                   if (PainT.saveStatus == 0){
                       isSaved = false;
                   }else{
                       isSaved = true;
                   }
                   save_data.logger.info(tool + " Save Status Is: "+ isSaved);

                   });
                }
            } 
            catch (InterruptedException e) { 
                System.out.println("Caught:" + e); 
            } 
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
