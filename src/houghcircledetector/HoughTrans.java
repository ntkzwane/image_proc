package houghcircledetector;

import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * Contains all the methods required to preform the Hough transform on edges. for each high pixel (pixel with value 255)
 * a circle is drawn around it for a range of radii (3 - maxRadius *variable defined in MainPanelController class*), this
 * is the transformation from normal (x,y) coordinate space, into Hough (a,b,r) coordinate space - where a, b and r are
 * the parameters in the equation of a circle r^2 = (x - a)^2 + (y - b)^2. After the transformation is done for all radii,
 * a sweep through is done and the points with high accumulations are identified, and mapped back to the normal coordinate
 * space
 * @author Ntokozo Zwane
 */
public class HoughTrans {
    final double tuner = 1.5;
    
    /**
     * transforms all the edges of an image into Hough Space (for a particular radius) and does a sweep through the space
     * to find high counts of pixels
     * @param imgObj the image object
     * @param canny the array containing the information about the edges of the image
     * @param accumulator the accumulated pixel values for this particular radius
     * @param radius the radius of the current search space
     * @param pixWriter the pixel writer object to be used to write the pixels. null if no image needs to be rendered
     */
    public void accumulator(ImageObject imgObj, int[][] canny, int[][] accumulator, int radius, PixelWriter pixWriter){
        int width = imgObj.getIntWidth();
        int height = imgObj.getIntHeight();
        int[][] circCanny = new int[width][height];
        
        // hough transform all the "on" pixels
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(canny[x][y] == 255){transformPT(imgObj, x, y, radius, width, height, circCanny);}
            }    
        }
        if(pixWriter != null){
            int max = 0;
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    if(circCanny[x][y] > max){ max = circCanny[x][y];}
                }
            }
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    pixWriter.setColor(x, y, Color.grayRgb((int)Math.round((circCanny[x][y]/(double)max)*255)));
                }   
            }
        }else{ // this implies that the "All Radius" accumulator is being used, thus simply incriment the general array
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    if(circCanny[x][y] > accumulator[x][y]){accumulator[x][y] = circCanny[x][y];}
                }
            }
        }
    }
    
    /**
     * transform a particular pixel into Hough Space by drawing circles corresponding to each on pixel.
     * the circles are drawn using Bresenham's circle drawing algorithm:
     * http://rosettacode.org/wiki/Bitmap/Midpoint_circle_algorithm
     * @param centerX the position of the picture
     * @param centerY the y position of the pixel
     * @param r the current radius for which a circle will be deawn
     * @param width the width of the image
     * @param height the height of the image
     * @param circCanny array to be accumulated for each successfully placed point
     * @return the number of points that have been placed for this radius
     */
    public int transformPT(ImageObject imgObj, int centerX, int centerY, int r, int width, int height, int[][] circCanny){
        int d = (5 - r*4)/4;
        int x = 0;
        int y = r;
        int counter = 0;
        do{
            if(imgObj.inBounds(centerX + x, centerY + y)){circCanny[centerX + x][centerY + y]++;counter++;}
            if(imgObj.inBounds(centerX + x, centerY - y)){circCanny[centerX + x][centerY - y]++;counter++;}
            if(imgObj.inBounds(centerX - x, centerY + y)){circCanny[centerX - x][centerY + y]++;counter++;}
            if(imgObj.inBounds(centerX - x, centerY - y)){circCanny[centerX - x][centerY - y]++;counter++;}
            if(imgObj.inBounds(centerX + y, centerY + x)){circCanny[centerX + y][centerY + x]++;counter++;}
            if(imgObj.inBounds(centerX + y, centerY - x)){circCanny[centerX + y][centerY - x]++;counter++;}
            if(imgObj.inBounds(centerX - y, centerY + x)){circCanny[centerX - y][centerY + x]++;counter++;}
            if(imgObj.inBounds(centerX - y, centerY - x)){circCanny[centerX - y][centerY - x]++;counter++;}
            
            if(d < 0){d += 2 * x + 1;}
            else{d += 2 * (x - y) + 1; y--;}
            x++;
        }while(x <= y);
        return counter;
    }
    
    /**
     * transform every point that is an "on" point in the edge-detected image
     * @param imgObj the image object
     * @param canny the array containing the information about the edges of the image
     * @param accumulator the accumulated pixel array
     * @param radius the radius of the current search space
     * @param pixWriter the pixel writer object to be used to write the pixels. null if no image needs to be rendered
     * @return the value of the maximum pixel, this will be used for normalizing the pixel values
     */
    public int hough(ImageObject imgObj, int[][] canny, int[][] accumulator, int radius, PixelWriter pixWriter){
        int width = imgObj.getIntWidth();
        int height = imgObj.getIntHeight();
        int[][] circCanny = new int[width][height];
        int total = 0, counter = 0;
        
        // hough transform all the "on" pixels
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(canny[x][y] == 255){counter += transformPT(imgObj, x, y, radius, width, height, circCanny);total++;}
            }    
        }
        // search through the Hough space and transform the high accumulated pixels back into normal
        // space using them as centeres of the circles with radius @arg radius
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(circCanny[x][y] > Math.round(counter/((double)total*tuner))){
                    revTransformPT(imgObj, radius, x, y, width, height, accumulator);
                }
            }   
        }
        
        // normalize the pixel values such that they are distributed throughout the range 0-255
        int max = 0;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(accumulator[x][y] > max){ max = accumulator[x][y];}
            }
        }
        if(pixWriter != null){
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){ 
                    pixWriter.setColor(x, y, Color.grayRgb((int)Math.round((accumulator[x][y]/(double)max)*255)));
                }
            }
        }
        return max;
    }
    
    /**
     * reverse transform a point in Hough Space back to normal space. this is done when there are sufficiently
     * many points found clustered together in hough space. the circles are drawn using Bresenham's circle drawing algorithm:
     * http://rosettacode.org/wiki/Bitmap/Midpoint_circle_algorithm
     * @param r the radius of the circle to map back to
     * @param centerX the x coordinate of image
     * @param centerY the y coordinate of the image
     * @param width the width of the image
     * @param height the height of the image
     */
    public void revTransformPT(ImageObject imgObj, int r,int centerX, int centerY, int width, int height, int[][] toDraw){
        int d = (5 - r*4)/4;
        int x = 0;
        int y = r;
        do{
            if(imgObj.inBounds(centerX + x, centerY + y))toDraw[centerX + x][centerY + y]++;
            if(imgObj.inBounds(centerX + x, centerY - y))toDraw[centerX + x][centerY - y]++;
            if(imgObj.inBounds(centerX - x, centerY + y))toDraw[centerX - x][centerY + y]++;
            if(imgObj.inBounds(centerX - x, centerY - y))toDraw[centerX - x][centerY - y]++;
            if(imgObj.inBounds(centerX + y, centerY + x))toDraw[centerX + y][centerY + x]++;
            if(imgObj.inBounds(centerX + y, centerY - x))toDraw[centerX + y][centerY - x]++;
            if(imgObj.inBounds(centerX - y, centerY + x))toDraw[centerX - y][centerY + x]++;
            if(imgObj.inBounds(centerX - y, centerY - x))toDraw[centerX - y][centerY - x]++;
            
            if(d < 0){d += 2 * x + 1;}
            else{d += 2 * (x - y) + 1; y--;}
            x++;
        }while(x <= y);
    }
}
