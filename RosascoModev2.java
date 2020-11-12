/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pain.t;



import java.io.File;
import java.nio.file.Paths;
import java.util.Random;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
/**
 *
 * @author 18fry
 */
public class RosascoModev2 implements Runnable {
double width;
double height;
int widthLimit;
int heightLimit;
int rosHeight;
int rosWidth;
        
File file;
ImageView sizedRosImage;
Image rosImage;
Thread worker;
private boolean shouldExit = false;


public int randomInRange(int min, int max) {
    Random rand = new Random();
    return rand.nextInt((max - min) + 1) + min;
}
public void requestStop(){
    shouldExit = true;
    
    
}
    public RosascoModev2(int canvasWidth, int canvasHeight){
        width = canvasWidth;
        height = canvasHeight;
  
        String path = Paths.get("").toAbsolutePath().toString();
        file=new File(path+"\\src\\pain\\t\\Resources\\RosImage2.0.jpg");
        rosImage = new Image(file.toURI().toString());
    
        rosHeight =(int)(height*.30);
        rosWidth =(int)((rosImage.getWidth()/rosImage.getHeight())*(width*.30));
        
        widthLimit = canvasWidth-(int)rosWidth;
        heightLimit = canvasHeight-(int)rosHeight;
    }
    @Override
    public void run() {
        try {
                while(true){
                    if(shouldExit){
                        Platform.runLater(()->{
                            PainT.setDefaultBackground(true);
                        });
                        break;
                    }
                   Thread.sleep(100);                   
                   int xChord = randomInRange(0,widthLimit);
                   int yChord = randomInRange(0,heightLimit);
                   Platform.runLater(()->{ 
                       PainT.pasteImage(rosImage,xChord,yChord,rosWidth,rosHeight,true);
                   });
                }
            } 
            catch (InterruptedException e) { 
                System.out.println("Caught:" + e); 
            }         
    }    
}
