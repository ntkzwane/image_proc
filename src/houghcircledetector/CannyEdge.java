package houghcircledetector;

import java.awt.Point;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * methods for applying Canny Edge Detection to identify edges on the objects. the implementation
 * follows a step by step process:
 * 1. apply gaussian filter
 * 2. apply sobel operator
 * 3. nonmaximal supression
 * 4. hysteresis
 * @author Ntokozo Zwane
 */
public class CannyEdge {
    final double ANGLE_45 = Math.PI/4;
    final double ANGLE_90 = Math.PI/2;
    final double ANGLE_135 = (3*Math.PI)/4;
    final double ANGLE_180 = Math.PI;
    
    // high and low thresholds used when applying nonmaximal supression
    static int THRESHOLD_HIGH = 200;
    static int THRESHOLD_LOW = 100;
    // smoothing kernel size
    static int KERNEL_SIZE = 7;
    // the standard deviation used for calculating the Gaussian
    static double SIGMA = 1.0;
    
    // the kernel for the x directional derivative
    final int[][] Gx = {
        {-1, 0, 1},
        {-2, 0, 2},
        {-1, 0, 1}
    };
    
    // the kernel for the y directional derivative
    final int[][] Gy = {
        {1, 2, 1},
        {0, 0, 0},
        {-1, -2, -1}
    };
    
    /**
     * normalize the image so that the rgb values are all the same
     * @param imgObj the image object
     * @return 
     */
    public int[] grayscale(ImageObject imgObj, ImageView imageView){
        int[] buffer = new int[imgObj.getIntWidth()*imgObj.getIntHeight()];
        WritableImage dest = new WritableImage(imgObj.getIntWidth( ),imgObj.getIntHeight( ));
        PixelWriter pixWriter = dest.getPixelWriter();
        for(int y = 0; y < imgObj.getIntHeight( ); y++){
            for(int x = 0; x < imgObj.getIntWidth( ); x++){
                pixWriter.setColor(x, y, imgObj.getPixelReader().getColor(x, y).grayscale());
            }
        }
        dest.getPixelReader().getPixels(0, 0, imgObj.getIntWidth(), imgObj.getIntHeight(), PixelFormat.getIntArgbPreInstance(), buffer, 0, imgObj.getIntWidth());
        
        // render the image to the image viewer
        if(imageView != null){
            for(int y = 0; y < imgObj.getIntHeight(); y++){
                for(int x = 0; x < imgObj.getIntWidth(); x++){
                    pixWriter.setColor(x, y, imgObj.getColor(x, y).grayscale());
                }
            }
            imageView.setImage(dest);
            return null; // no buffer to be returned
        }else{return buffer;}
    }
    
    /**
     * construct the gaussean kernel for smoothing/filtering the image
     * @return the k
     */
    public double[][] constructKernel(){
        double[][] kernel = new double[KERNEL_SIZE][KERNEL_SIZE];
        double cons = 1.0/(2*ANGLE_180*SIGMA*SIGMA);
        double denominator = 2*SIGMA*SIGMA;
        for(int y = -KERNEL_SIZE/2, j = 0; y <= KERNEL_SIZE/2; y++, j++){
            for(int x = -KERNEL_SIZE/2, i = 0; x <= KERNEL_SIZE/2; x++, i++){
                kernel[i][j] = cons*Math.exp(-(x*x + y*y)/denominator);
            }
        }
        return kernel;
    }
    
    /**
     * 
     * @param imgObj the source image object
     * @param buffer the list of normalized pixel values. this is assumed to be in rgb form
     * @param pixWriter
     * @return 
     */
    public int[][] sobelXOperator(ImageObject imgObj, int[][] buffer){
        if(buffer == null){MainPanelController.handleError(0, "Do smoothing first");return null;}
        int width = imgObj.getIntWidth();
        int height = imgObj.getIntHeight();
        
        int[][] output = new int[width][height];

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                for(int i = -1, dx = 0; i <= 1; i++, dx++){
                    for(int j = -1, dy = 0; j <= 1; j++, dy++){
                        int xx = x + i; int yy = y + j;
                        if(imgObj.inBounds(xx, yy)){
                            output[x][y] += buffer[xx][yy] * Gx[dx][dy];
                        }
                    }
                }
            }
        }
        return output;
    }
    
    /**
     * 
     * @param imgObj the source image object
     * @param buffer the list of normalized pixel values. this is assumed to be in rgb form
     * @param pixWriter
     * @return 
     */
    public int[][] sobelYOperator(ImageObject imgObj, int[][] buffer){
        if(buffer == null){MainPanelController.handleError(0, "Do smoothing first");return null;}
        int width = imgObj.getIntWidth();
        int height = imgObj.getIntHeight();
        
        int[][] output = new int[width][height];

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                for(int i = -1, dx = 0; i <= 1; i++, dx++){
                    for(int j = -1, dy = 0; j <= 1; j++, dy++){
                        int xx = x + i; int yy = y + j;
                        if(imgObj.inBounds(xx, yy)){
                            output[x][y] += buffer[xx][yy] * Gy[dx][dy];
                        }
                    }
                }
            }
        }
        return output;
    }
    
    /**
     * compute the magnitude of the x and y derivative passes of the image
     * @param imgObj the source image object
     * @param xSobel x pass derivatives
     * @param ySobel y pass dirivatives
     * @return an array containing the magnitudes of the derivatives
     */
    public int[][] combineSobel(ImageObject imgObj, int[][] xSobel, int[][] ySobel){
        int[][] sobel = new int[imgObj.getIntWidth()][imgObj.getIntHeight()];
        for(int y = 0; y < imgObj.getIntHeight( ); y++){
            for(int x = 0; x < imgObj.getIntWidth( ); x++){
                sobel[x][y] = (int) Math.hypot(xSobel[x][y], ySobel[x][y]);
            }
        }
        return sobel;
    }
    
    /**
     * preform the gaussean filter by computing the convolution of the kernal and image pixels
     * @param buffer the int[] array of image pixels. the pixels in this array are unsigned ints
     * @param imgObj the source image object
     * @param pixWriter the image writer object to be used to render the image
     * @return an array containing the filtered pixels
     */
    public int[][] filter(int[][] buffer, ImageObject imgObj, PixelWriter pixWriter){
        int width = imgObj.getIntWidth();
        int height = imgObj.getIntHeight();
        int[][] output = new int[width][height];
        double[][] kernel = constructKernel();
        
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                 for(int i = -KERNEL_SIZE/2, dx = 0; i <= KERNEL_SIZE/2; i++, dx++){
                    for(int j = -KERNEL_SIZE/2, dy = 0; j <= KERNEL_SIZE/2; j++, dy++){
                        int xx = x + i; int yy = y + j;
                        if(imgObj.inBounds(xx, yy)){
                            // convert the pixel value from an unsigned integer to it's corresponding
                            // rgb value
                            int grey = buffer[xx][yy] & 0xFF;
                            output[x][y] += grey * kernel[dx][dy];
                        }
                    }
                }
                if(pixWriter!=null){pixWriter.setColor(x, y, Color.grayRgb(output[x][y]));}
            }
        }
        return output;
    }
    
    /**
     * calculate the directional angle for each pixel
     * @param imgObj
     * @param xSobel
     * @param ySobel
     * @return 
     */
    public double[][] classifyAnglesHelper(ImageObject imgObj, int[][] xSobel, int[][] ySobel){
        int width = imgObj.getIntWidth();
        int height = imgObj.getIntHeight();
        double[][] output = new double[width][height];
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){ 
                if(xSobel[x][y] == 0){
                    if(ySobel[x][y] == 0){
                        output[x][y] = 0;
                    }else{
                        output[x][y] = ANGLE_90;
                    }
                }else{
                    output[x][y] = Math.atan2(ySobel[x][y], xSobel[x][y]);
                }
            }
        }
        return output;
    }
    
    /**
     * using the magnitude of the sobel operator, calculate the angle in which the derivative is pointing.
     * this tells us which direction an edge is pointing towards, this direction is then clamped into 4 angle
     * classes
     * @param imgObj the source image object
     * @param xSobel the sobel operator applied in the x direction
     * @param ySobel the sobel operator applied in the y direction
     * @return array containing categorized angles
     */
    public double[][] classifyAngles(ImageObject imgObj, int[][] xSobel, int[][] ySobel){
        int width = imgObj.getIntWidth();
        int height = imgObj.getIntHeight();
        double[][] angles = new double[width][height];
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                double currAngle;
                // calculate the angle using the gradients
                if(xSobel[x][y] == 0){ // cater for the fact that arctan near pi/2 -> infinity
                    if(ySobel[x][y] == 0){
                        currAngle = 0;
                    }else{
                        currAngle = ANGLE_90;
                    }
                }else{
                    currAngle = Math.atan2(ySobel[x][y], xSobel[x][y]);
                }
                
                // put the angle into one of 4 angle classes
                if(currAngle < 0) 
                    {currAngle += ANGLE_180;}
                if(currAngle < Math.toRadians(22.5) || Math.toRadians(157.5) < currAngle)  
                    {angles[x][y] = 0;}
                else if(currAngle < Math.toRadians(67.5))
                    {angles[x][y] = ANGLE_45;}
                else if(currAngle < Math.toRadians(112.5))
                    {angles[x][y] = ANGLE_90;}
                else{angles[x][y] = ANGLE_135;}
            }
        }
        return angles;
    }
    
    /**
     * depending on the angle, the two adjacent points that are in the direction of the angle are returned
     * a helper method for nonmaximal surpression
     * @param pixelPos the position of the centre pixel
     * @param angle the angle obtained with the sobel operator
     * @return an array containing the two adjacent points
     */
    public Point[] direction(Point pixelPos, double angle){
        Point[] directions = new Point[2];
        if(angle == 0){
            directions[0] = new Point(pixelPos.x,pixelPos.y+1);
            directions[1] = new Point(pixelPos.x,pixelPos.y-1);
            return directions;
        }
        if(angle == ANGLE_45){
            directions[0] = new Point(pixelPos.x-1,pixelPos.y+1);
            directions[1] = new Point(pixelPos.x+1,pixelPos.y-1);
            return directions;
        }
        if(angle == ANGLE_90){
            directions[0] = new Point(pixelPos.x+1,pixelPos.y);
            directions[1] = new Point(pixelPos.x-1,pixelPos.y);
            return directions;
        }
        if(angle == ANGLE_135){
            directions[0] = new Point(pixelPos.x+1,pixelPos.y+1);
            directions[1] = new Point(pixelPos.x-1,pixelPos.y-1);
            return directions;
        }
        MainPanelController.handleError(0, "CannyEdge.direction(..): angle unclassifiable");
        return null; // should not happen
    }
    
    /**
     * thin the detected edges by using nonmaximal suppression on the pixels, 
     * @param imgObj the source image object
     * @param xSobel the sobel operator applied in the x direction
     * @param ySobel the sobel operator applied in the y direction
     * @param sobel the magnitude of the x and y sobel operators
     * @param pixWriter
     * @return the array of surpressed pixels
     */
    public int[][] nonMaximSupression(ImageObject imgObj, int[][] xSobel, int[][] ySobel, int[][] sobel, PixelWriter pixWriter){
        int width = imgObj.getIntWidth();
        int height = imgObj.getIntHeight();
        double[][] angles = classifyAngles(imgObj, xSobel, ySobel);
        boolean p1 = false, p2 = false; 
        
        int[][] buffer = new int[width][height];
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(sobel[x][y] < THRESHOLD_LOW){ // handle points below threshold
                    buffer[x][y] = 0;
                }else{
                    // handle intermidiate points
                    Point[] neighbours = direction(new Point(x,y), angles[x][y]);
                    if(imgObj.inBounds(neighbours[0].x, neighbours[0].y)){
                        if(sobel[x][y] > sobel[neighbours[0].x][neighbours[0].y]){
                            p1 = true;
                        }
                    }
                    if(imgObj.inBounds(neighbours[1].x, neighbours[1].y)){
                        if(sobel[x][y] > sobel[neighbours[1].x][neighbours[1].y]){
                            p2 = true;
                        }
                    }
                    
                    if(sobel[x][y] > THRESHOLD_HIGH && p1 && p2){ // handle points below threshold
                        buffer[x][y] = 255; 
                    }
                    else if(p1 && p2)
                            {buffer[x][y] = 128;}
                    else{buffer[x][y] = 0;}
                    p1 = false; p2 = false;
                }
                if(pixWriter != null){pixWriter.setColor(x, y, Color.grayRgb(buffer[x][y]));}
            }
            
        }
        return buffer;
    }
    
    /**
     * make the edges obtained through nonmaximal surpression a lot thinner as well as have less holes/break in the edge lines
     * @param imgObj
     * @param nonmax
     * @param pixWriter
     * @return the new edge pixels
     */
    public int[][] hysteresis(ImageObject imgObj, int nonmax[][], PixelWriter pixWriter){
        int width = imgObj.getIntWidth();
        int height = imgObj.getIntHeight();
        boolean isEdge = false;
        int[][] hyster = new int[width][height];
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                // compare the current pixel to its neighbours
                if(nonmax[x][y] == 128){
                    for(int i = -1; i <= 1; i++){
                       for(int j = -1; j <= 1; j++){
                           int xx = x + i; int yy = y + j;
                           if(imgObj.inBounds(xx, yy)){
                               if(nonmax[xx][yy] == 255){
                                   isEdge = true;
                               }
                           }
                       }
                   }
                   if(isEdge){hyster[x][y] = 255;}
                   else{hyster[x][y] = 0;}
                   isEdge = false;
                }else{hyster[x][y] = nonmax[x][y];}
                if(pixWriter!=null){pixWriter.setColor(x, y, Color.grayRgb(hyster[x][y]));}
            }
        }
    return hyster;
    }
    
    /**
     * clamp the pixel values between 0 and 255. this is used for the sole purpose of displaying the points on
     * that are a result of the sobel operator
     * @param number the possibly out of range number
     * @return the number within range
     */
    public static int clamp(double number){
        if(number > 255){return 255;}
        if(number < 0){return 0;}
        return (int) number;
    }
}
