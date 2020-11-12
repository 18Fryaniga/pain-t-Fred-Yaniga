/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pain.t;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
/**
*@author Fred Yaniga
*/
public class PainT extends Application implements EventHandler<KeyEvent> {
    //Global Variables to be accessed by multiple functions    
    Stage window =new Stage();
    BorderPane layout;
    ImageView displayImage;
    Image image,image2;
    ScrollPane scrollPane;
    static Canvas canvas = new Canvas();
    static GraphicsContext gc, gcRosasco;
    File quickFile;
    ColorPicker colorPicker = new ColorPicker();
    Slider slider;
    FileChooser saveChooser = new FileChooser();
    static int saveStatus = 0;
    Stack<WritableImage> undoStack, redoStack;
    double shapeY;
    double shapeX;
    WritableImage wim;
    String selectedToolValue = null;
    static Label selectedToolLabel = new Label();
    static Label prepareSave = new Label();
    TabPane tabs = new TabPane();
    int isTabOpen = 0;
    static double startX;
    static double startY;
    double endX;
    double endY;    
    static int threadTalk = 0;
    static Thread t3;
    RosascoModev2 rosChildThread;
    private static boolean timeToSave=false;
    private static boolean rosascoStatus;
   
    /**
     *if true runs a file chooser for picture importing
     * @param checkDraw a boolean to check run or not
     */
    public void importFile(boolean checkDraw){
        if(checkDraw == true){
            if(isTabOpen==1){
                FileChooser importChooser2 = new FileChooser();
                importChooser2.setTitle("Import File");
                importChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.*"), new FileChooser.ExtensionFilter("PNG", "*.png")); 
                File file2 = importChooser2.showOpenDialog(window);
                if(file2 !=null){
                    image2 = new Image(file2.toURI().toString());
                    ImageView iv = new ImageView(image2);
                    Tab tab = new Tab(file2.getName());
                    tab.setContent(iv);
                    tabs.getTabs().add(tab);
                }
            }
             if(isTabOpen==0){
                gc = canvas.getGraphicsContext2D();gc = canvas.getGraphicsContext2D();
                FileChooser importChooser = new FileChooser();
                importChooser.setTitle("Import File");
                importChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.*"), new FileChooser.ExtensionFilter("PNG", "*.png"));
                File file = importChooser.showOpenDialog(window); 
                
                if (file != null){
                    image = new Image(file.toURI().toString());
                    quickFile=file;
                    canvas.setHeight(image.getHeight());
                    canvas.setWidth(image.getWidth());
                    gc.drawImage(image,0,0);
                    isTabOpen++;
                }
            }        
        }
    } 
 
    /**
     *if file has not been saved before, this launches dialog box to prompt user to saveas first
     */
    public void checkSaveStatus(){
        if(saveStatus==0){
        Stage saveStatusWindow =new Stage();
        VBox saveBoxLayout = new VBox();
        Text saveText = new Text();
        saveText.setFont(new Font(20));
        saveText.setText("First Save Your File With A Name!");
        Button saveAs = new Button();
        Button cancel = new Button(); 
        cancel.setText("Cancel Dont Save");
        saveAs.setText("Save As");
        cancel.setOnAction(e-> saveStatusWindow.close());
        saveAs.setOnAction(e -> {
            saveAs();
            saveStatusWindow.close();
                });
        saveBoxLayout.getChildren().addAll(saveText,saveAs,cancel);
        Scene saveScene = new Scene(saveBoxLayout,400,150); 
        saveStatusWindow.setScene(saveScene);
        saveStatusWindow.show();
        }else{
            save();
        }    
    }

    /**
     *quicksave without file picker
     */
    public void save(){
         if (quickFile != null) {
            try {
                WritableImage writableImage = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", quickFile);
                //ImageIO.write(SwingFXUtils.fromFXImage(image,null), "png", file);
            } catch (IOException ex) {
                Logger.getLogger(PainT.class.getName()).log(Level.SEVERE, null, ex);
            }
        }     
    }

    /**
     *File Chooser saves file Image making new nameable file
     */
    public void saveAs(){
        saveChooser.setTitle("Save File");
        saveChooser.getExtensionFilters().addAll (new FileChooser.ExtensionFilter("PNG", "*.png"));
        File file = saveChooser.showSaveDialog(window);
        quickFile = file;
        if (file != null) {
            try {
                saveStatus = 1;//this variable now allows save() to work               
                WritableImage writableImage = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
                startTimeSaveThread();               
            } catch (IOException ex) {
                Logger.getLogger(PainT.class.getName()).log(Level.SEVERE, null, ex);
            }
        }          
    }

    /**
     *closes program
     */
    public void closeProgram(){ 
        Platform.exit();
    } 

    /**
     *this simply shows a message in the gui so you can see when auto save is working
     * @param s string of message to be shown in savestatus label
     */
    public static void setPrepareSave(String s){
        prepareSave.setText("          "+s);
    }

    /**
     *this is called by the autosave timer thread to quicksave
     * @param b boolean to control label 
     */
    public static void TimeToSave(boolean b){
        timeToSave=b;
        if(timeToSave==true){
            save.fire();
            setPrepareSave("Saving!");
        }
        if(timeToSave==false){
           setPrepareSave(""); 
        }
    }

    /**
     *starts auto save thread
     */
    public void startTimeSaveThread(){
        TimeSave childThread = new TimeSave(15);
        Thread t1 = new Thread(childThread);
        System.out.println("INITIALIZED THE THREAD");
        t1.setDaemon(true);
        t1.start();
    }

    /**
     *starts thread to log tool activities
     * @throws SecurityException
     * @throws IOException
     */
    public void startLogActivityThread() throws SecurityException, IOException{
        LogActivity childThread = new LogActivity(10);
        Thread t2 = new Thread(childThread);
        System.out.println("INITIALIZED LOG THREAD");
        t2.setDaemon(true);
        t2.start();
    }

    /**
     *ends the RosascoMode thread loop
     */
    public void stopRosascoModeThread() { 
        try {
            if(t3.isAlive()){
            rosChildThread.requestStop();
            t3.join();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(PainT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *starts the Rosascomode thread loop
     */
    public void startRosascoModeThread(){
            rosChildThread = new RosascoModev2((int)canvas.getWidth(),(int)canvas.getHeight());
            t3 = new Thread(rosChildThread);
            System.out.println("INITIALIZED ROSASCO THREAD");
            t3.setDaemon(true);
            t3.start();
    }

    /**
     *called by rosasco mode to paste Rosimage since only main thread can alter gui
     * @param ros
     * @param xChord
     * @param yChord
     * @param rosWidth
     * @param rosHeight
     * @param checkDraw
     */
    public static void pasteImage(Image ros,int xChord,int yChord,int rosWidth,int rosHeight, boolean checkDraw){
        if (checkDraw==true){
            gcRosasco = canvas.getGraphicsContext2D();
            gcRosasco.drawImage(ros, xChord, yChord,rosWidth,rosHeight);
        }        
    }

    /**
     *paste tool drag and drop
     * @param snapshot image to be pasted
     * @param checkDraw boolean to initialize or not
     */
    public void pasteTool(WritableImage snapshot,boolean checkDraw){
        if(checkDraw==true){
        saveToStack();
        gc = canvas.getGraphicsContext2D();
        canvas.setOnMousePressed(e->{
            saveToStack();
            startX=e.getX();
            startY=e.getY();
            canvas.setOnMousePressed(event->{}); 
        });
        canvas.setOnMouseDragged(e->{
            gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
            gc.drawImage(snapshot, e.getX(), e.getY()); 
        });
        canvas.setOnMouseReleased(e->{
           clearTool();
           canvas.setOnMouseReleased(event->{}); 
        });
        }
    }

    /**
     *allows selection before the calling of pasteTool()  
     * @param checkDraw boolean initialize or not
     */
    public void copyTool(boolean checkDraw){ 
        if( checkDraw==true){
        gc = canvas.getGraphicsContext2D();
        Rectangle selectArea = new Rectangle();
            canvas.setOnMousePressed(e->{
            saveToStack();
            startX=e.getX();
            startY=e.getY();
            canvas.setOnMousePressed(event->{});
        });
        canvas.setOnMouseDragged(e->{
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);
            gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
                if ((startX < e.getX())&&(startY < e.getY())){
                    gc.drawImage((undoStack.peek()), 0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeRect(startX, startY, (e.getX()-startX), (e.getY()-startY));
                }
                if ((e.getX() < startX)&&(e.getY() < startY)){
                    gc.drawImage((undoStack.peek()), 0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeRect(e.getX(), e.getY(), (startX-e.getX()), (startY-e.getY()));
                }
                if ((e.getX() < startX )&&(e.getY() > startY)){
                    gc.drawImage((undoStack.peek()), 0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeRect(e.getX(), startY, (startX-e.getX()), (e.getY()-startY));
                }
                if ((e.getX() > startX )&&(e.getY() < startY)){
                    gc.drawImage((undoStack.peek()), 0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeRect(startX, e.getY(), (e.getX()-startX), (startY - e.getY()));
                }
        });
        canvas.setOnMouseReleased(e->{
            gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
            endX=e.getX();
            endY=e.getY();
            selectArea.setFrameFromDiagonal(startX, startY, endX, endY);
            
            WritableImage temp = new WritableImage((int)canvas.getWidth(),(int)canvas.getHeight());
            canvas.snapshot(null, temp);
            gc.clearRect(startX, startY,(int)selectArea.getWidth(),(int)selectArea.getHeight());
            gc.setFill(Color.WHITE);
            gc.setStroke(Color.WHITE);
            gc.strokeRect(startX, startY,(int)selectArea.getWidth()+1,(int)selectArea.getHeight()+1);
            gc.fillRect(startX, startY,(int)selectArea.getWidth()+1,(int)selectArea.getHeight()+1);
            WritableImage croppedSnap = new WritableImage(temp.getPixelReader(),(int)startX,(int)startY,(int)selectArea.getWidth(),(int)selectArea.getHeight());
            canvas.setOnMouseReleased(event->{});
            pasteTool(croppedSnap,true); 
        });
        }
    }
    
    /**
     *This function picks up chords based on the mouse on click and then also does it whie dragging.  Notice colors and size values from slider/colorpicker
     * @param checkDraw boolean initialize or not
     */
    public void squareDraw(boolean checkDraw){
       
        if(checkDraw== true){
            gc = canvas.getGraphicsContext2D();       
            canvas.setOnMousePressed(e ->{
            saveToStack();
            gc.setStroke(colorPicker.getValue());
            gc.setFill(colorPicker.getValue());
            gc.beginPath();
            shapeX = e.getX();
            shapeY = e.getY();
        });
        canvas.setOnMouseDragged(e->{
            gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
            gc.rect(shapeX, shapeY, e.getX()-shapeX, e.getX()-shapeX);
            gc.fillRect(shapeX, shapeY, e.getX()-shapeX, e.getX()-shapeX);
            gc.closePath();
        });
        canvas.setOnMouseReleased(e->{
             gc.rect(shapeX, shapeY, e.getX()-shapeX, e.getX()-shapeX);
             gc.fillRect(shapeX, shapeY, e.getX()-shapeX, e.getX()-shapeX);
             gc.closePath();     
        });   
        }   
    }

    /**
     *shape tool start choords and end choords and draws roundedrectangle shape
     * @param checkDraw boolean initialize or not
     */
    public void roundedRectangleDraw(boolean checkDraw){
       if(checkDraw==true){
            Rectangle selectArea=new Rectangle();
            gc = canvas.getGraphicsContext2D();
            gc.setStroke(colorPicker.getValue());
            gc.setFill(colorPicker.getValue());
            canvas.setOnMousePressed(e->{
                saveToStack();
                startX = e.getX();
                startY = e.getY(); 
            });
            canvas.setOnMouseDragged(e->{
                gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
                endX = e.getX();
                endY = e.getY();
                selectArea.setFrameFromDiagonal(startX, startY,endX ,endY);
                gc.strokeRoundRect(startX, startY,selectArea.getWidth() , selectArea.getHeight(),.3*selectArea.getWidth() ,.3*selectArea.getHeight());
                gc.fillRoundRect(startX, startY,selectArea.getWidth() , selectArea.getHeight(),.3*selectArea.getWidth() , .3*selectArea.getHeight());    
        });
           canvas.setOnMouseReleased(e->{
               endX = e.getX();
               endY = e.getY();
               selectArea.setFrameFromDiagonal(startX, startY,endX ,endY);
               gc.strokeRoundRect(startX, startY,selectArea.getWidth() , selectArea.getHeight(),.3*selectArea.getWidth() ,.3*selectArea.getHeight());
               gc.fillRoundRect(startX, startY,selectArea.getWidth() , selectArea.getHeight(),.3*selectArea.getWidth() , .3*selectArea.getHeight());
           });
       }
   }

    /**
     *uses start chords and end chords to draw elipse
     * @param checkDraw boolean initialize or not
     */
    public void elipseDraw(boolean checkDraw){
       if(checkDraw==true){
           Rectangle selectArea=new Rectangle();
           gc = canvas.getGraphicsContext2D();
           gc.setStroke(colorPicker.getValue());
           gc.setFill(colorPicker.getValue());
           canvas.setOnMousePressed(e->{
                saveToStack();
                startX = e.getX();
                startY = e.getY(); 
           });
            canvas.setOnMouseDragged(e->{
                gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
                endX = e.getX();
                endY = e.getY();
                selectArea.setFrameFromDiagonal(startX, startY,endX ,endY);
                gc.strokeOval(startX, startY,selectArea.getWidth() , selectArea.getHeight());
                gc.fillOval(startX, startY,selectArea.getWidth() , selectArea.getHeight());
        });
           canvas.setOnMouseReleased(e->{
                endX = e.getX();
                endY = e.getY();
                selectArea.setFrameFromDiagonal(startX, startY,endX ,endY);        
                gc.strokeOval(startX, startY,selectArea.getWidth() , selectArea.getHeight());
                gc.fillOval(startX, startY,selectArea.getWidth() , selectArea.getHeight());
           });
       }
   }

    /**
     *uses start chords and end chords to draw circle
     * @param checkDraw boolean initialize or not
     */
    public void circleDraw(boolean checkDraw){
       if(checkDraw==true){
            int sides = 2000;
            gc = canvas.getGraphicsContext2D();
            canvas.setOnMousePressed(e->{
            saveToStack();
            shapeX = e.getX();
            shapeY = e.getY(); 
            gc.setFill(colorPicker.getValue());
            gc.setStroke(colorPicker.getValue());
        });
        canvas.setOnMouseDragged(e->{
            gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
            double centerX, centerY;
            double radius =(e.getX()-shapeX)/2;
            centerX = (e.getX()-shapeX)/2;
            centerY= (e.getY()-shapeY)/2;
            double angleStep = Math.PI * 2 / sides;
            double angle = 0;
            double[]xPoints = new double[sides];
            double[]yPoints = new double[sides];
            
            for (int i = 0; i<sides; i++, angle += angleStep){
                xPoints[i]= (Math.sin(angle)*radius+centerX)+shapeX;
                yPoints[i]= (Math.cos(angle)*radius+ centerY)+shapeY;
            }
             gc.strokePolygon(xPoints, yPoints, sides);
             gc.fillPolygon(xPoints, yPoints, sides);
        });
        canvas.setOnMouseReleased(e-> {
            double centerX, centerY;
            double radius =(e.getX()-shapeX)/2;
            centerX = (e.getX()-shapeX)/2;
            centerY= (e.getY()-shapeY)/2;
            double angleStep = Math.PI * 2 / sides;
            double angle = 0;
            double[]xPoints = new double[sides];
            double[]yPoints = new double[sides];
            
            for (int i = 0; i<sides; i++, angle += angleStep){
                xPoints[i]= (Math.sin(angle)*radius+centerX)+shapeX;
                yPoints[i]= (Math.cos(angle)*radius+ centerY)+shapeY;
            }
             gc.strokePolygon(xPoints, yPoints, sides);
             gc.fillPolygon(xPoints, yPoints, sides);
        });
        } 
   }

    /**
     *uses start chords and end chords to draw polygon and ask for #of sides
      
     * @param checkDraw boolean initialize or not
     */
    public void polygonDraw(boolean checkDraw){
        if(checkDraw==true){
        TextInputDialog polygonAsk = new TextInputDialog("Polygon");
        polygonAsk.setHeaderText("Please enter the amount of sides for your polygon:");
        polygonAsk.showAndWait();
        String amount =polygonAsk.getEditor().getText();
        int sides = Integer.parseInt(amount);
        
        gc = canvas.getGraphicsContext2D();
        canvas.setOnMousePressed(e->{
            saveToStack();
            shapeX = e.getX();
            shapeY = e.getY();             
            gc.setFill(colorPicker.getValue());
            gc.setStroke(colorPicker.getValue());
        });
        canvas.setOnMouseDragged(e->{
            gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
            double centerX, centerY;
            double radius =(e.getX()-shapeX)/2;
            centerX = (e.getX()-shapeX)/2;
            centerY= (e.getY()-shapeY)/2;
            double angleStep = Math.PI * 2 / sides;
            double angle = 0;
            double[]xPoints = new double[sides];
            double[]yPoints = new double[sides];
            
            for (int i = 0; i<sides; i++, angle += angleStep){
                xPoints[i]= (Math.sin(angle)*radius+centerX)+shapeX;
                yPoints[i]= (Math.cos(angle)*radius+ centerY)+shapeY;
            }
             gc.strokePolygon(xPoints, yPoints, sides);
             gc.fillPolygon(xPoints, yPoints, sides);
        });

        canvas.setOnMouseReleased(e-> {
            double centerX, centerY;
            double radius =(e.getX()-shapeX)/2;
            centerX = (e.getX()-shapeX)/2;
            centerY= (e.getY()-shapeY)/2;
            double angleStep = Math.PI * 2 / sides;
            double angle = 0;
            double[]xPoints = new double[sides];
            double[]yPoints = new double[sides];
            
            for (int i = 0; i<sides; i++, angle += angleStep){
                xPoints[i]= (Math.sin(angle)*radius+centerX)+shapeX;
                yPoints[i]= (Math.cos(angle)*radius+ centerY)+shapeY;
            }
             gc.strokePolygon(xPoints, yPoints, sides);
             gc.fillPolygon(xPoints, yPoints, sides);    
        });
        } 
    }
    
    /**
     *will set the canvas background white as default
     * @param checkDraw boolean initialize or not
     */
    public static void setDefaultBackground(boolean checkDraw){
        if(checkDraw==true){
            gc = canvas.getGraphicsContext2D();
            startX = 0;
            startY = 0;
            gc.setStroke(Color.WHITE);
            gc.setFill(Color.WHITE);
            gc = canvas.getGraphicsContext2D();
            gc.fillRect(startX, startY, canvas.getWidth(),canvas.getHeight());
        }
    }
    
    /**
     *uses start chords and end chords to draw rectangle
     * @param checkDraw boolean initialize or not
     */
    public void rectangleDraw(boolean checkDraw){
        
        if(checkDraw==true){
            gc = canvas.getGraphicsContext2D();
            canvas.setOnMousePressed(e->{
            saveToStack();
            gc.beginPath();
            gc.setStroke(colorPicker.getValue());
            gc.setFill(colorPicker.getValue());
            shapeX = e.getX();
            shapeY = e.getY();
        });
        canvas.setOnMouseDragged(e -> {
             gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
             gc.rect(shapeX, shapeY, e.getX()-shapeX, e.getY()-shapeY);
             gc.fillRect(shapeX, shapeY, e.getX()-shapeX, e.getY()-shapeY);
        });
        canvas.setOnMouseReleased(e->{
             gc.rect(shapeX, shapeY, e.getX()-shapeX, e.getY()-shapeY);
             gc.fillRect(shapeX, shapeY, e.getX()-shapeX, e.getY()-shapeY);
             gc.closePath();
        });  
        }
        if(checkDraw==false){gc=null;}
    }

    /**
     *alters the label to show what tool is active
     * @param InUseTool tool in use
     */
    public void selectedTool(String InUseTool){ 

        selectedToolLabel.setFont(new Font("Arial",24));
        if (InUseTool.equals("none")){
        selectedToolLabel.setText("No Tool Selected");
    }else{
            selectedToolLabel.setText(InUseTool); 
        }
    }
    
    /**
     *calls stopRosascoModeThread()
     */
    public void clearToolRosMode(){
        stopRosascoModeThread(); 
    }

    /**
     *This is run before every draw function and ensures all of the tools are disabled before the initialization of a new tool.  Also sets selectedTool
     */
    public void clearTool(){
        pencilDraw(false);
        lineDraw(false);
        rectangleDraw(false);
        squareDraw(false);
        polygonDraw(false);
        textDraw(false);
        copyTool(false);
        pasteTool(wim,false);
        textDraw(false);
        circleDraw(false);
        eraserDraw(false);
        roundedRectangleDraw(false);
        importFile(false);
        undo(false);
        redo(false);
        setDefaultBackground(false);
        selectedTool("No Tool Selected");  
    }

    /**
     *draws color of background to erase
     * @param checkDraw boolean initialize or not
     */
    public void eraserDraw(boolean checkDraw){//
        if(checkDraw==true){
            gc= canvas.getGraphicsContext2D();
            gc.setStroke(Color.WHITE);
            gc.setFill(Color.WHITE);
        canvas.setOnMousePressed(e->{
            saveToStack();
            gc.beginPath();
            if (slider.getValue()==0)gc.fillRect(e.getX(), e.getY(),10,10);
            gc.fillRect(e.getX(), e.getY(), slider.getValue(), slider.getValue());
            gc.fillRect(e.getX(), e.getY(),slider.getValue(),slider.getValue());
            gc.stroke();
        });
        canvas.setOnMouseDragged(e->{
            if (slider.getValue()==0)gc.fillRect(e.getX(), e.getY(),10,10);
            gc.fillRect(e.getX(), e.getY(),slider.getValue(),slider.getValue());
            gc.stroke();
        });
        canvas.setOnMouseReleased(e->{ 
        });
        }
    }

    /**
     *uses start chords and end chords to draw free hand lines
     * @param checkDraw boolean initialize or not
     */
    public void pencilDraw(boolean checkDraw){  
        if(checkDraw==true){
            gc= canvas.getGraphicsContext2D(); 
            canvas.setOnMousePressed(e->{
            saveToStack();
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(slider.getValue());
            gc.beginPath();
            gc.lineTo(e.getX(),e.getY());
            gc.stroke();
        });
        canvas.setOnMouseDragged(e->{
            gc.lineTo(e.getX(),e.getY());
            gc.stroke();
        });
        canvas.setOnMouseReleased(e->{ 
        });
    }if(checkDraw==false){
            gc=null;
        }
    }

    /**
     *uses start chords and end chords to draw straight lines
     * @param checkDraw boolean initialize or not
     */
    public void lineDraw(boolean checkDraw){//
        if(checkDraw==true){
            clearTool();
            gc= canvas.getGraphicsContext2D();
            canvas.setOnMousePressed(e->{
                saveToStack();
                gc.setStroke(Color.TRANSPARENT);
                gc.setLineWidth(slider.getValue());
                gc.beginPath();
                gc.lineTo(e.getX(),e.getY());
                startX = e.getX();
                startY = e.getY();
                gc.stroke();          
            });
            canvas.setOnMouseDragged(e->{
                gc.setStroke(colorPicker.getValue());
                gc.setFill(colorPicker.getValue());
                if ((startX < e.getX())&&(startY < e.getY())){
                    gc.drawImage((undoStack.peek()), 0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeLine(startX, startY, e.getX(), e.getY());
                    }
                if ((e.getX() < startX)&&(e.getY() < startY)){
                    gc.drawImage((undoStack.peek()), 0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeLine(e.getX(), e.getY(), startX, startY);
                    }
                if ((e.getX() < startX )&&(e.getY() > startY)){
                    gc.drawImage((undoStack.peek()), 0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeLine(e.getX(), e.getY(), startX, startY);
                    }
                if ((e.getX() > startX )&&(e.getY() < startY)){
                    gc.drawImage((undoStack.peek()), 0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeLine(startX, startY, e.getX(), e.getY());
                    }
        });
        canvas.setOnMouseReleased(e -> {
            if ((startX < e.getX())&&(startY < e.getY())){
                gc.strokeLine(startX, startY, e.getX(), e.getY());
                }
            if ((e.getX() < startX)&&(e.getY() < startY)){
                gc.strokeLine(e.getX(), e.getY(), startX, startY);
                }
            if ((e.getX() < startX )&&(e.getY() > startY)){
                gc.strokeLine(e.getX(), e.getY(), startX, startY);
                }
            if ((e.getX() > startX )&&(e.getY() < startY)){
                gc.strokeLine(startX, startY, e.getX(), e.getY());
                }
        });  gc.closePath();
        }if(checkDraw==false){gc=null;}
    }
    
    /**
     *allows the zoom factor to be set with a gui
     */
    public void zoom(){
        TextInputDialog zoomAmount = new TextInputDialog("Zoom");
        zoomAmount.setHeaderText("Please enter the zoom amount in or out:");
        zoomAmount.showAndWait();
        String amount =zoomAmount.getEditor().getText();
        double zoomFactor = Integer.parseInt(amount);
        canvas.setScaleX((Double)zoomFactor/100);
        canvas.setScaleY((Double)zoomFactor/100);    
    }

    /**
     *grabs the pixel of chord and sets color to that.
     */
    public void colorGrab(){//
        selectedTool("Color Grabber Active");
        canvas.setOnMousePressed(e->{
            WritableImage temp = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.TRANSPARENT);
            WritableImage snapshot = canvas.snapshot(sp,temp);
            PixelReader pr = snapshot.getPixelReader();
            colorPicker.setValue(pr.getColor((int)e.getX(),(int)e.getY()));   
            canvas.setOnMousePressed(event->{});
                    });
        canvas.setOnMouseReleased(e ->{
            wim = null;
            clearTool();
            canvas.setOnMouseReleased(event->{});
        });     
    }

    /**
     *uses start chords and end chords to draw text which is set with a gui 
     * @param checkDraw
     */
    public void textDraw(boolean checkDraw){//
        if(checkDraw==true){
            clearTool();
            TextInputDialog textInput = new TextInputDialog("Text to Enter");
            textInput.setHeaderText("Please Enter The Text to Draw: ");
            textInput.showAndWait();
            String text =textInput.getEditor().getText();
            gc = canvas.getGraphicsContext2D();
            gc.setFont(Font.font(STYLESHEET_CASPIAN, FontWeight.LIGHT, FontPosture.REGULAR, 50));
            gc.setLineWidth(1);
            canvas.setOnMousePressed(e ->{
                saveToStack();
                startX = e.getX();
                startY = e.getY();
            });
            canvas.setOnMouseDragged(e -> {
            });
            canvas.setOnMouseReleased(e -> {
                gc.setStroke(colorPicker.getValue());
                gc.setFill(colorPicker.getValue());
                gc.strokeText(text, startX, startY);
                gc.fillText(text, startX, startY);
            });
        }
    }

    /**
     *gui box to show help options
     */
    public void helpWindow(){//
        VBox helpLayout = new VBox();
        Text helpText = new Text();
        helpText.setFont(new Font(20));
        helpText.setText("Help Options Coming Soon");
        helpLayout.getChildren().addAll(helpText);
        Stage helpWindow =new Stage();
        Scene helpScene = new Scene(helpLayout,400,150);
        helpWindow.setScene(helpScene);
        helpWindow.show();
    }
    
    /**
     *initializes FileRetriever class to read and display releasenotes file from package
       
     * @throws FileNotFoundException
     */
    public void relaseNotes() throws FileNotFoundException{
        FileRetriever notes  = new FileRetriever();
        VBox releaseNoteLayout = new VBox();  
        ScrollPane scrollPane = new ScrollPane();
        final TextArea textArea = TextAreaBuilder.create()
                .prefWidth(500)
                .prefHeight(600)
                .wrapText(true)       
                .build();         
        textArea.setEditable(false);
        textArea.setText(notes.getText());
        System.out.println();
        releaseNoteLayout.getChildren().addAll(textArea);
        Stage helpWindow =new Stage();
        helpWindow.setTitle("Relase Notes");
        Scene releaseNoteScene = new Scene(releaseNoteLayout,500,600);
        helpWindow.setScene(releaseNoteScene);
        helpWindow.show();
    }

    /**
     *saves to stack the current canvas so undo/redo can be done later.  This is executed before or after tool actions
     */
    public void saveToStack(){//
        wim = new WritableImage((int)canvas.getWidth(),(int)canvas.getHeight());
        undoStack.push(canvas.snapshot(null, wim));
    }

    /**
     *accesses stack to draw on canvas will undo and push to redostack
     * @param checkDraw
     */
    public void undo(boolean checkDraw){//
         if (checkDraw==true){
            gc = canvas.getGraphicsContext2D();
            if(undoStack.empty()){
                System.out.println("undo stack is empty!");
            }else{
                gc.drawImage((undoStack.peek()),0,0,canvas.getWidth(),canvas.getHeight());
                redoStack.push(undoStack.pop());  
            }
         }
    }

    /**
     *accesses redo stack and draws to canvas
     * @param checkDraw
     */
    public void redo(boolean checkDraw){
        if (checkDraw==true){
        gc= canvas.getGraphicsContext2D();
            if(redoStack.empty()){           
            }else{
                gc.drawImage((redoStack.peek()), 0, 0, canvas.getWidth(),canvas.getHeight());
                undoStack.push(redoStack.pop());
            }
        }
    }
 
    private static MenuItem save = new MenuItem("Save");//had to call outside of start with override
    @Override

    public void start(Stage primaryStage) throws SecurityException, IOException  {  
        scrollPane = new ScrollPane();
        scrollPane.setPrefSize(800, 500);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        window = primaryStage;
        window.setTitle("Pain(t)");
        //------------------------------------------------------
        //Menu Declaration
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu helpMenu = new Menu("Help");
        Menu exitMenu = new Menu("Exit");
            //File Menu Items
            MenuItem importFile = new MenuItem("Import File...");
            MenuItem saveAs = new MenuItem("Save As...");
            //MenuItem save = new MenuItem("Save"); LOOK ABOVE
            importFile.setOnAction(e -> importFile(true));// the e -> is a "lambda" and its like an onClickListener
            saveAs.setOnAction(e -> saveAs());
            save.setOnAction(e->save());
            
                //Add File Menu Items
                fileMenu.getItems().add(importFile);
                fileMenu.getItems().add(new SeparatorMenuItem());
                fileMenu.getItems().add(saveAs);
                fileMenu.getItems().add(save);
        //------------------------------------------------------
            //Help Menu Items
            MenuItem help = new MenuItem("Help...");
            help.setOnAction(e -> helpWindow());
            MenuItem releaseNotes = new MenuItem("Release Notes");
            releaseNotes.setOnAction(e-> {
                try {
                    relaseNotes();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(PainT.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
                helpMenu.getItems().add(help);
                helpMenu.getItems().add(releaseNotes);
        //------------------------------------------------------
            //Exit Menu Items
            MenuItem closeProgram = new MenuItem("Close Program");
            closeProgram.setOnAction(e -> closeProgram());
                //Add Exit Menu Items
                exitMenu.getItems().add(closeProgram);
        //-------------------------------------------------------
        //Items added to the layout
        menuBar.getMenus().addAll(fileMenu,helpMenu,exitMenu);
       //----------------------------------------------------------

       //Tool Buttons and tooltips
        Button colorGrab = new Button("Color Grab");
        Tooltip colorGrabTT = new Tooltip("Tool used to get the color from whatever you click");
        colorGrab.setTooltip(colorGrabTT);
        
        Button pencil = new Button("Pencil");
        Tooltip pencilTT = new Tooltip("Tool used to draw a freehand line");
        pencil.setTooltip(pencilTT);
        
        Button straightLn = new Button("Line");
        Tooltip straightlnTT = new Tooltip("Tool used to draw a straight line");
        straightLn.setTooltip(straightlnTT);
        
        Button eraser = new Button("Eraser");
        Tooltip eraserTT= new Tooltip("Tool used to erase your mistakes");
        eraser.setTooltip(eraserTT);
        
        Button square = new Button("Square");
        Tooltip squareTT = new Tooltip("Tool used to draw an equal sided square");
        square.setTooltip(squareTT);
        
        Button rectangle = new Button("Rectangle");
        Tooltip rectangleTT = new Tooltip("Tool used to draw a variable sided rectangle");
        rectangle.setTooltip(rectangleTT);
        
        Button polygon = new Button("Polygon");
        Tooltip polygonTT= new Tooltip("Tool used to draw a polygon with as many sides as you wish");
        polygon.setTooltip(polygonTT);
        
        Button roundRectangle = new Button("Rounded Rectangle");
        Tooltip roundRectangleTT = new Tooltip("Tool used to draw a variable sided rounded rectangle");
        roundRectangle.setTooltip(roundRectangleTT);
        
        Button circle = new Button("Circle");
        Tooltip circleTT = new Tooltip("Tool used to draw a circle of any size");
        circle.setTooltip(circleTT);
        
        Button elipse = new Button("Elipse");
        Tooltip elipseTT = new Tooltip("Tool used to draw an elipse of any size");
        elipse.setTooltip(elipseTT);
        
        Button text= new Button("Text");
        Tooltip textTT = new Tooltip("Tool used to add text to the canvas");
        text.setTooltip(textTT);
        
        Button selectPaste = new Button("Select Move");
        Tooltip selectPasteTT = new Tooltip("Tool used to cut and move a piece of your image to a new spot");
        selectPaste.setTooltip(selectPasteTT);
        
        Button rosascoMode = new Button("Rosasco Mode");
        Tooltip rosascoModeTT = new Tooltip("Try this and figure out if you dare...");
        rosascoMode.setTooltip(rosascoModeTT);
       
        Button stopDraw = new Button("Clear Tool");
        Tooltip stopDrawTT = new Tooltip("Click this to force clear any active tool");
        stopDraw.setTooltip(stopDrawTT);
        
        Button undo = new Button("Undo");
        Tooltip undoTT = new Tooltip("Click this to undo to a previous action");
        undo.setTooltip(undoTT);
        
        Button redo = new Button("Redo");
        Tooltip redoTT = new Tooltip("Click this to redo after an undo");
        redo.setTooltip(redoTT);
        
        Button zoom = new Button("Zoom");
        Tooltip zoomTT = new Tooltip("Tool used to zoom in or out of your image");
        zoom.setTooltip(zoomTT);

        //All actions of buttons declared
        colorGrab.setOnAction(e -> colorGrab());
        
        pencil.setOnAction(e -> {
            clearTool();
            pencilDraw(true);
            selectedTool("Pencil Tool Active");
                });
        straightLn.setOnAction(e ->{
            clearTool();
            lineDraw(true);
            selectedTool("Line Tool Active");
                });
        eraser.setOnAction(e ->{
            clearTool();
            eraserDraw(true);
            selectedTool("Eraser Tool Active");
        });
        square.setOnAction(e->{
            clearTool();
            squareDraw(true);
            selectedTool("Square Tool Active");
                });
        rectangle.setOnAction(e->{
            clearTool();
            rectangleDraw(true);
            selectedTool("Rectangle Tool Active");
                });
        roundRectangle.setOnAction(e->{
            clearTool();
            roundedRectangleDraw(true);
            selectedTool("Rounded Rectangle Tool Active");
        });
        polygon.setOnAction(e->{
            clearTool();
            polygonDraw(true);
            selectedTool("Polygon Tool Active");
        });
        circle.setOnAction(e->{
            clearTool();
            circleDraw(true);
            selectedTool("Circle Tool Active");
        });
        elipse.setOnAction(e->{
            clearTool();
            elipseDraw(true);
            selectedTool("Elipse Tool Active");
        });
        selectPaste.setOnAction(e->{
            clearTool();
            copyTool(true);
            selectedTool("Copy Paste Tool Active");
        });
        rosascoMode.setOnAction(e->{
            startRosascoModeThread();
            selectedTool("MUAHAHAHA BE THANKFUL FOR YOUR DEADLINE EXTENSIONS");
        });
        text.setOnAction(e->{
            clearTool();
            textDraw(true);
        });
        stopDraw.setOnAction(e ->{
            if(null!=rosChildThread){
                clearToolRosMode();
            }else{
                clearTool();  
            }
            selectedTool("No Tool Selected");  
        });
        undo.setOnAction(e->{
            clearTool();
            undo(true);
        });
        redo.setOnAction(e->{
            clearTool();
            redo(true);
        });
        zoom.setOnAction(e-> {
            clearTool();
            zoom();
                });
       //tab functionality
        Tab tab1 = new Tab("Main Canvas");
        tabs.getTabs().add(tab1);
        tab1.setContent(canvas);

       //Toolbar Eliments
        slider = new Slider(0,200,0);
        slider.setShowTickMarks(true);      
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(100f);
        slider.setBlockIncrement(20f);
        selectedTool("none");
        prepareSave.setFont(new Font("Arial",24));
        prepareSave.setText("");
        layout = new BorderPane();
        //declares all Horizontal bars for tool capacity
        HBox toolBar = new HBox();//Hbox and menuBar are both in the VBox (vertical)
        HBox toolBar2 = new HBox();
        HBox toolBar3 = new HBox();
        HBox toolBar4 = new HBox();
        VBox allMenus = new VBox();//Vbox holds all of the Hboxes
        //setting the content in the ToolBars
        toolBar.getChildren().addAll(colorPicker,slider,colorGrab,pencil,straightLn,eraser,square,rectangle,circle,elipse,polygon,selectPaste, rosascoMode); 
        toolBar2.getChildren().addAll(roundRectangle,text,stopDraw,zoom,undo,redo);
        toolBar3.getChildren().addAll(selectedToolLabel,prepareSave);
        toolBar4.getChildren().addAll(tabs);
        allMenus.getChildren().addAll(menuBar,toolBar,toolBar2,toolBar3,toolBar4);
        layout.setTop(allMenus);

        //final scene set up
        Scene scene = new Scene(layout,1200, 900);//resolution window size
        window.setScene(scene);
        canvas.setWidth(1200);
        canvas.setHeight(900);
        setDefaultBackground(true);
        
        tabs.setPrefWidth(canvas.getWidth());//sets tab dimensions
        tabs.setPrefHeight(canvas.getHeight());

        undoStack = new Stack<WritableImage>();//initializes undo stack WIM
        redoStack = new Stack<WritableImage>();//initializes redo stack WIM
        
        scrollPane.setContent(tabs);//puts all tabs into scrollpane
        layout.setCenter(scrollPane);
        
        startLogActivityThread();//starts the tool log thread on startup
        window.show();
       
        scene.setOnKeyPressed(this);  
    }

    public static void main(String[] args) {
        launch(args);//launches gui
    }

    @Override
    public void handle(KeyEvent event) {//shortcut stuff
        final KeyCombination ctrlSave = new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN);
        final KeyCombination ctrlOpen = new KeyCodeCombination(KeyCode.O,KeyCombination.CONTROL_DOWN);
        final KeyCombination ctrlUndo = new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN);
        if(ctrlSave.match(event))checkSaveStatus();
        if(ctrlOpen.match(event))importFile(true);
        if(ctrlUndo.match(event))undo(true);       
    }
}
