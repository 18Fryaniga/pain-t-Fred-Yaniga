/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pain.t;


import javafx.application.Platform;

/**
 *
 * @author 18fry
 */
public class TimeSave implements Runnable  {
    int time = 0;
    PainT main;
    
    public TimeSave(int t){
        time = 1000*t;  
    }
    @Override
    public void run() {
            try {
                while(true){
                   System.out.println("Save started again!");
                   Thread.sleep(time);
                   Platform.runLater(()->{
                   PainT.TimeToSave(true);
                   });
                   
                   System.out.println("Saved!");
                   Thread.sleep(5000);
                   Platform.runLater(()->{
                   PainT.TimeToSave(false);
                   });
                   System.out.println("Save reset!");
                }
            } 
            catch (InterruptedException e) { 
                System.out.println("Caught:" + e); 
            } 
        }    
    }
    

