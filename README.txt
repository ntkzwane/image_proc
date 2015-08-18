-----------------------------------------------------------------------------
Introduction to Computer Vision Assignment: Hough Circle Detector
-----------------------------------------------------------------------------

-----------------------------------------------------------------------------
INSTRUCTIONS
-----------------------------------------------------------------------------
1. Running the program
The program is compiled into a java .jar file which can be either run by
double-clicking the .jar file from a file browser, or through the command
line using the following command 'java -jar HoughCircleDetector.jar'.

2. Using the GUI
The explanation for each button (referenced with the name on the button):

Open Image  -   opens an image from the system directory
Grayscale   -   apply a grayscale filter to the input image (normalize
                the image)
Smooth      -   smooth the image by applying a Gaussian filter. The 
                kernel size can be selected using the +/- buttons next
                to the 'Filter Size' text box. The standard deviation
                for the Gaussian can also be modified in the same way
Sobel       -   Apply the Sobel operator to the filtered image. This
                displays the magnitudes of the x and y directional
                derivatives applied to the pixels
CannyEdge   -   Use Canny Edge Detection to identify edges in the image.
                The High and Low thresholds (used in nonmaximal
                suppression) can be modified using the +/- buttons in the
                'High/Low Threshold' text box
Accumulator -   Displays the Hough Transform accumulator for (by default)
                the radius in the 'Accumulator Radius' text box. The
                accumulator for all radii can be displayed by selecting
                the 'Accumulate All Radii in Image Range' radio button 
                (changes will take effect on the next 'Accumulator' button
                click)
Write       -   Writes the image in the output view (by default) to a .png
                file. An alternative image can be written when the 'Write
                Circles With Outlines' radio button is selected. This
                writes the original image with the edges of the circles
                highlighted with the chosen colour (a colour picker will
                appear when the radio button is clicked - red by default).
Reset       -   Reset the output image view to the original image

-----------------------------------------------------------------------------
IMPLEMENTATION
-----------------------------------------------------------------------------
Canny Edge Detection:

A step-by-step implementation was used to apply Canny edge detection to the
the image.
1. The image is filtered
    A Gaussian filter is applied to the image buy convolving all 'windows' of
    the image with a Gaussian kernel.

2. Edges are identified
    The Sobel operator is used to identify edges of objects in the image. The
    magnitude of the x and y directional derivatives (total derivative) is used
    in the steps to follow.
3. Edges are thinned
    The edges are thinned using nonmaximal suppression and hysteresis.

Circle Detection:
    The Hough transform is used to detect circular edges in the image. The
    edge pixels identified using Canny's edge detector are mapped to hough
    space. The highest points are mapped back to the normal coordinate space.
    The circles are drawn using Bresenham's circle drawing algorithm.

-----------------------------------------------------------------------------
REQUIREMENTS
-----------------------------------------------------------------------------
Java JDK/JRE 1.8.x

-----------------------------------------------------------------------------
REFERENCES
-----------------------------------------------------------------------------
http://rosettacode.org/wiki/Bitmap/Midpoint_circle_algorithm
