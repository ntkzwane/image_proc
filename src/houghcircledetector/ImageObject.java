package houghcircledetector;

import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.paint.Color;

/**
 * holds information about an image and provides access to the
 * properties of the particular image
 * @author Ntokozo Zwane
 */
public class ImageObject extends Image{
    
    private int WIDTH, HEIGHT;
    
    /**
     * @param img_file the image file object pointing to the image's location on the
     * computer
     * @param uri_string the uri of the location of the image
     * @param original true if this is the original input image
     */
    public ImageObject(String uri_string) {
        super(uri_string);
        WIDTH = (int) this.getWidth();
        HEIGHT = (int) this.getHeight();
    }
    
    /**
     * return all the pixels in a rectangular subarea of the image starting from a particular
     * coordinate and ending at the end (bottom right) of the image. returns all the pixels 
     * over the entire image if the starting point is the beginning (top left) of the image
     * @param x the x coordinate of the starting point of the subarea
     * @param y the y coordinate of the starting point of the subarea
     * @return the pixels within the subarea (the whole image if (x,y) = (0,0))
     */
    public int[] getAllPixels(int x, int y){
        int[] buffer = new int[WIDTH*HEIGHT];
        this.getPixelReader().getPixels(x, y, WIDTH, HEIGHT, PixelFormat.getIntArgbPreInstance(), buffer, 0, WIDTH);
        return buffer;
    }
    
    /**
     * @return the integer height of the image
     */
    public int getIntHeight( ){
        return HEIGHT;
    }
    
    /**
     * @return the integer width of the image
     */
    public int getIntWidth( ){
        return WIDTH;
    }
    
    /**
     * returns the colour of a pixel at a particular point in the image
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return the Color object representing the color of this pixel
     */
    public Color getColor(int x, int y){
        return this.getPixelReader().getColor(x, y);
    }
    
    /**
     * checks whether a given point is within the bounds of this image
     * @param x x position
     * @param y y position 
     * @return true if the points are within the boundss
     */
    public boolean inBounds(int x, int y){
        if(x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT){
            return true;
        }
        return false;
    }
}
