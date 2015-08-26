/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houghcircledetector;

import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author Chuck
 */
public class MainPanelController implements Initializable {
    
    @FXML
    private ImageView img_in;
    @FXML
    private ImageView img_out;
    @FXML
    private TextField text_filter;
    @FXML
    private TextField text_sigma;
    @FXML
    private TextField text_high_thresh;
    @FXML
    private TextField text_low_thresh;
    @FXML
    private TextField text_radius;
    @FXML
    private RadioButton radio_write;
    @FXML
    private RadioButton radio_radius;
    @FXML
    private ColorPicker color_write;
    
    private ImageObject img_in_obj;
    
    private ImageObject img_pr_obj;
    
    private final CannyEdge cannyE = new CannyEdge();
    
    private final HoughTrans houghT = new HoughTrans();
    
    int maxRadius; // tha maximum possible radius of a circle in thie image minimum(image_width, image_height)/2
    
    /**
     * initialize the file chooser. this is the interface that allows the user to select a file (an image) from any 
     * directory
     * @return the selected image
     */
    private File initFileChooser( ){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        // initial directory is the current runtime class path (i.e where the .jar file is located)
        fileChooser.setInitialDirectory(new File("./"));
        return fileChooser.showOpenDialog(HoughCircleDetector.mainStage);
    }
    
    /**
     * handle all the button events
     * @param event
     */
    @FXML
    private void handleButtonAction(ActionEvent event) {
        String buttonCliced = ((Button) event.getSource()).getText();
        switch(buttonCliced){
            case "Open Image":
                File in_file = initFileChooser( );
                img_in_obj = new ImageObject(in_file.toURI().toString());
                img_pr_obj = new ImageObject(in_file.toURI().toString());
                img_in.setImage(new Image(in_file.toURI().toString()));
                maxRadius = Math.min(img_pr_obj.getIntWidth(), img_pr_obj.getIntHeight())/2;
                break;
            case "Reset":{
                img_pr_obj = img_in_obj;
                img_out.setImage(img_in_obj);
                break;}
            case "Smooth":{
                WritableImage dest = new WritableImage(img_pr_obj.getIntWidth( ),img_pr_obj.getIntHeight( ));   
                PixelWriter pixWriter = dest.getPixelWriter();
                int[] greyscaled = cannyE.grayscale(img_pr_obj, null);
                int[][] greyBuffer = new int[img_pr_obj.getIntWidth()][img_pr_obj.getIntHeight()];
                int index = 0;
                for(int y = 0; y < img_pr_obj.getIntHeight( ); y++){
                    for(int x = 0; x < img_pr_obj.getIntWidth( ); x++){
                        greyBuffer[x][y] = greyscaled[index];
                        index ++;
                    }
                }
                smooth(pixWriter);
                img_out.setImage(dest);
                break;}
            case "Grayscale":{
                cannyE.grayscale(img_pr_obj, img_out);}
                break;
            case "CannyEdge":{
                WritableImage dest = new WritableImage(img_pr_obj.getIntWidth( ),img_pr_obj.getIntHeight( ));   
                PixelWriter pixWriter = dest.getPixelWriter();
                
                cannyEdge(pixWriter);
                img_out.setImage(dest);
                break;}
            case "Accumulator":{
                WritableImage dest = new WritableImage(img_pr_obj.getIntWidth( ),img_pr_obj.getIntHeight( ));   
                PixelWriter pixWriter = dest.getPixelWriter();
                int[][] canny = cannyEdge(null);
                if(radio_radius.isSelected()){
                    int[][] accumulator = new int[img_pr_obj.getIntWidth()][img_pr_obj.getIntHeight()];
                    
                    for(int r = 3; r <= maxRadius; r++){
                        houghT.accumulator(img_pr_obj, canny, accumulator, r, null);
                    }
                    int max = 0;
                    for(int y = 0; y < img_pr_obj.getIntHeight( ); y++){
                        for(int x = 0; x < img_pr_obj.getIntWidth( ); x++){
                            if(accumulator[x][y] > max){ max = accumulator[x][y];}
                        }
                    }
                    for(int y = 0; y < img_pr_obj.getIntHeight( ); y++){
                        for(int x = 0; x < img_pr_obj.getIntWidth( ); x++){
                            pixWriter.setColor(x, y, Color.grayRgb((int)Math.round((accumulator[x][y]/(double)max)*255)));
                        }
                    }
                }else{houghT.accumulator(img_pr_obj, canny, new int[img_pr_obj.getIntWidth()][img_pr_obj.getIntHeight()], Integer.parseInt(text_radius.getText()), pixWriter);}
                img_out.setImage(dest);
                break;}
            case "Sobel":{
                WritableImage dest = new WritableImage(img_pr_obj.getIntWidth( ),img_pr_obj.getIntHeight( ));   
                PixelWriter pixWriter = dest.getPixelWriter();
                int[][] newBuffer = smooth(null);
                int[][] CannyXSobel = cannyE.sobelXOperator(img_pr_obj, newBuffer);
                int[][] CannyYSobel = cannyE.sobelYOperator(img_pr_obj, newBuffer);
                int[][] CannySobel = cannyE.combineSobel(img_pr_obj, CannyXSobel, CannyYSobel);
                for(int y = 0; y < img_pr_obj.getIntHeight( ); y++){
                    for(int x = 0; x < img_pr_obj.getIntWidth( ); x++){
                        pixWriter.setColor(x, y, Color.grayRgb(CannyEdge.clamp(CannySobel[x][y])));
                    }
                }
                img_out.setImage(dest);
                break;}
            case "Hough":{
                WritableImage dest = new WritableImage(img_pr_obj.getIntWidth( ),img_pr_obj.getIntHeight( ));   
                PixelWriter pixWriter = dest.getPixelWriter();
                int[][] canny = cannyEdge(null);
                int[][] toDraw = new int[img_pr_obj.getIntWidth()][img_pr_obj.getIntHeight()];
                
                for(int r = 3; r <= maxRadius; r++){
                    houghT.hough(img_pr_obj, canny, toDraw, r, pixWriter);
                }
                img_out.setImage(dest);
                break;}
            case "Write":{
                if(radio_write.isSelected()){
                    WritableImage dest = new WritableImage(img_pr_obj.getIntWidth( ),img_pr_obj.getIntHeight( ));   
                    PixelWriter pixWriter = dest.getPixelWriter();
                    int[][] canny = cannyEdge(null);
                    int max = 0;
                    int[][] toDraw = new int[img_pr_obj.getIntWidth()][img_pr_obj.getIntHeight()];

                    // preform the hough circle detection to detect the circles
                    for(int r = 3; r <= maxRadius; r++){
                        max = houghT.hough(img_pr_obj, canny, toDraw, r, null);
                    }

                    // copy the pixels from the original image
                    int[] buffer = img_pr_obj.getAllPixels(0, 0);
                    pixWriter.setPixels(0, 0,img_pr_obj.getIntWidth( ),img_pr_obj.getIntHeight( ), PixelFormat.getIntArgbInstance(), buffer, 0,img_pr_obj.getIntWidth( ));

                    // write the circles over this image
                    for(int y = 0; y < img_pr_obj.getIntHeight( ); y++){
                        for(int x = 0; x < img_pr_obj.getIntWidth( ); x++){
                            if((int)Math.round((toDraw[x][y]/(double)max)*255) > 0){
                                pixWriter.setColor(x, y, color_write.getValue());
                            }
                        }
                    }
                    img_out.setImage(dest);
                }
                
                // write the image to file
                writeImageToFile(img_out);
                break;
            }
            case "HoughLine":{
                WritableImage dest = new WritableImage(img_pr_obj.getIntWidth( ),img_pr_obj.getIntHeight( ));   
                PixelWriter pixWriter = dest.getPixelWriter();
                int[][] canny = cannyEdge(null);
                int[][] toDraw = new int[img_pr_obj.getIntWidth()][img_pr_obj.getIntHeight()];
                
                houghT.houghLine(img_pr_obj, canny, toDraw, pixWriter);
                img_out.setImage(dest);
                break;
            }
        }
    }
    
    /**
     * handle a radio button toggle
     * @param event 
     */
    @FXML
    private void handleRadioClick(ActionEvent event){
        String radioSelected = ((RadioButton) event.getSource()).getId();
        switch(radioSelected){
            case "radio_write":
                if(radio_write.isSelected())color_write.setVisible(true);
                else color_write.setVisible(false);
                break;
            case "radio_radius":
                if(radio_radius.isSelected()){
                    text_radius.setOpacity(0.5);
                    text_radius.setEditable(false);
                }
                else{
                    text_radius.setOpacity(1.0);
                    text_radius.setEditable(true);
                }
                break;
        }
    }
    
    /**
     * handle the increment/decrement buttons for the parameters. each value is checked
     * if it is in bounds before incrementing
     */
    @FXML
    private void incFilter(){
        if(outOfBounds(CannyEdge.KERNEL_SIZE + 2,1,21)) return;
        CannyEdge.KERNEL_SIZE += 2;
        text_filter.setText(CannyEdge.KERNEL_SIZE + "");
    }
    @FXML
    private void decFilter(){
        if(outOfBounds(CannyEdge.KERNEL_SIZE - 2,1,21)) return;
        CannyEdge.KERNEL_SIZE -= 2;
        text_filter.setText(CannyEdge.KERNEL_SIZE + "");
    }
    @FXML
    private void incSigma(){
        if(outOfBounds(CannyEdge.SIGMA + 0.1, 0.0, 10)) return;
        CannyEdge.SIGMA += 0.1;
        text_sigma.setText(CannyEdge.SIGMA + "");
    }
    @FXML
    private void decSigma(){
        if(outOfBounds(CannyEdge.SIGMA - 0.1, 0.0, 10)) return;
        CannyEdge.SIGMA -= 0.1;
        text_sigma.setText(CannyEdge.SIGMA + "");
    }
    @FXML
    private void incHThresh(){
        if(outOfBounds(CannyEdge.THRESHOLD_HIGH + 10, 0, 255)) return;
        CannyEdge.THRESHOLD_HIGH += 10;
        text_high_thresh.setText(CannyEdge.THRESHOLD_HIGH + "");
    }
    @FXML
    private void decHThresh(){
        if(outOfBounds(CannyEdge.THRESHOLD_HIGH - 10, 0, 255)) return;
        CannyEdge.THRESHOLD_HIGH -= 10;
        text_high_thresh.setText(CannyEdge.THRESHOLD_HIGH + "");
    }
    @FXML
    private void incLThresh(){
        if(outOfBounds(CannyEdge.THRESHOLD_LOW + 10, 0, 255)) return;
        CannyEdge.THRESHOLD_LOW += 10;
        text_low_thresh.setText(CannyEdge.THRESHOLD_LOW + "");
    }
    @FXML
    private void decLThresh(){
if(outOfBounds(CannyEdge.THRESHOLD_LOW - 10, 0, 255)) return;
        CannyEdge.THRESHOLD_LOW -= 10;
        text_low_thresh.setText(CannyEdge.THRESHOLD_LOW + "");
    }
    
    /**
     * initialize the javafx scene
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        text_filter.setText(CannyEdge.KERNEL_SIZE+"");
        text_sigma.setText(CannyEdge.SIGMA+"");
        text_high_thresh.setText(CannyEdge.THRESHOLD_HIGH+"");
        text_low_thresh.setText(CannyEdge.THRESHOLD_LOW+"");
        color_write.setValue(Color.RED);
        color_write.setVisible(false); // only show this on radio_write selected
    } 
    
    /**
     * handle errors with an error message displayed through a popup window
     * @param errNo
     * @param message 
     */
    public static void handleError(int errNo, String message){
        final Stage myDialog = new Stage();
                myDialog.initModality(Modality.WINDOW_MODAL);

                Button okButton = new Button("CLOSE");
                okButton.setOnAction(new EventHandler<ActionEvent>(){

                    @Override
                    public void handle(ActionEvent arg0) {
                        myDialog.close();
                    }

                });
        switch(errNo){
            case 0: // show popup
                Scene myDialogScene = new Scene(VBoxBuilder.create()
                     .children(new Text(message), okButton)
                     .alignment(Pos.CENTER)
                     .padding(new Insets(10))
                     .build());
           
                    myDialog.setScene(myDialogScene);
                    myDialog.show();
                break;
        }
    }
    
    
   /**
    * smooth the edges of the image by applying a Gaussian filter to it using the provided parameters
    * @param pixWriter the pixel writer object to be used to write the pixels. null if no image needs to be rendered
    * @return the array containing the smoothed pixels
    */
    public int[][] smooth(PixelWriter pixWriter){
        int[] greyscaled = cannyE.grayscale(img_pr_obj, null);
        int[][] greyBuffer = new int[img_pr_obj.getIntWidth()][img_pr_obj.getIntHeight()];
        int index = 0;
        for(int y = 0; y < img_pr_obj.getIntHeight( ); y++){
            for(int x = 0; x < img_pr_obj.getIntWidth( ); x++){
                greyBuffer[x][y] = greyscaled[index];
                index ++;
            }
        }
        return cannyE.filter(greyBuffer, img_pr_obj, pixWriter);        
    }
    
    /**
     * use Canny Edge Detection algorithm to detect edges in an image
     * @param pixWriter the pixel writer object to be used to write the pixels. null if no image needs to be rendered
     * @return the array containing the edge detected pixels
     */
    public int[][] cannyEdge(PixelWriter pixWriter){
        int[][] newBuffer = smooth(null);
        int[][] CannyXSobel = cannyE.sobelXOperator(img_pr_obj, newBuffer);
        int[][] CannyYSobel = cannyE.sobelYOperator(img_pr_obj, newBuffer);
        int[][] CannySobel = cannyE.combineSobel(img_pr_obj, CannyXSobel, CannyYSobel);
        int[][] nonmax = cannyE.nonMaximSupression(img_pr_obj, CannyXSobel, CannyYSobel, CannySobel, pixWriter);
        return cannyE.hysteresis(img_pr_obj, nonmax, pixWriter);
    }
    
    
   /**
    * check whether the attempt to change a parameter is within bounds
    * @param num the potential value of the parameter
    * @param min the minimum value the parameter can possess
    * @param max the maximum value the parameter can possess
    * @return true if the potential value of the parameter is out of bounds
    */
    public boolean outOfBounds(double num, double min, double max){
        if(num < min) return true;
        if(num > max) return true;
        return false;
    }
    
    /**
     * writes the currently displayed image to a .png file. checks are done to avoid overwriting
     * an image with a new one. the image names are saved incrementally
     * @param imageView the image currently on display on the output-view
     */
    public void writeImageToFile(ImageView imageView){
        try{
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            File file = new File("output.png");;
            // ensure that an existing file is not being overwritten
            int fileCount = 1;
            while(file.isFile()){
                file = new File("output"+"_"+fileCount+".png");
                fileCount++;
            }
            ImageIO.write(renderedImage, "png", file);
        }catch(Exception e){handleError(0, "Unable to write image to file. No image in the output view.");}
    }
}
