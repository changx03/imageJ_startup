
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Rectangle;

/**
 * This is a startup project for imageJ plug-in. It is based on NetBeans IDE
 * 8.1. It uses Apache Maven to build. Make sure the internet connection is
 * working while build the project.
 *
 * @author Luke Chang, 25-02-2016
 *
 */
public class MyPlugIn_ implements PlugInFilter {

    // image properties
    private ImagePlus image;
    private String imageTitle;
    private Rectangle rec;  // contains width and height for the original image
    private int bitDepth;   // should be 8bit
    private int numBins;    // should be 256
    private int numPixels;  // width * height
    private int numStack;   // the stack number

    // from input dialog
    private double myVal = 1.0;

    @Override
    public int setup(String string, ImagePlus ip) {
        this.image = ip;
		IJ.hideProcessStackDialog = true;
        if (!showDialog()) {
            return DONE;
        }
        // add DOES_16, if you want process 16bit greyscale image.
        return IJ.setupDialog(image, DOES_8G + SUPPORTS_MASKING + NO_UNDO);
    }

    private boolean showDialog() {
        // Build dialog;
        GenericDialog gd = new GenericDialog("MyDialog");
        gd.addNumericField("input", myVal, 2);

        // Show dialog;
        gd.showDialog();
        if (gd.wasCanceled()) {
            return false;
        }

        // get value
        myVal = gd.getNextNumber();

        return true;
    }

    @Override
    public void run(ImageProcessor ip) {
        // the properties should same in entire stack
        imageTitle = image.getShortTitle();
        bitDepth = ip.getBitDepth();
        numBins = (int) Math.pow(2, bitDepth);
        rec = ip.getRoi();
        numPixels = rec.height * rec.width;

        // process each slice
        for (int i = 1; i <= image.getStackSize(); i++) {
            numStack = i;
            IJ.log(imageTitle + " [" + i + "]:");

            process(image.getStack().getProcessor(i));

            // update results
            image.updateAndDraw();
        }

    }

    /**
     * Put your filter here
     *
     * @param ip
     */
    private void process(ImageProcessor ip) {
        FloatProcessor fp = null;
        fp = ip.toFloat(0, fp);
        float[] pixels = (float[]) fp.getPixels();  // original image

        // A example of how to up date results on original image
        for (int y = 0; y < rec.height; y++) {
            for (int x = 0; x < rec.width; x++) {
                ip.putPixel(x, y, (int) pixels[y * rec.width + x]);
            }
        }

    }

    /**
     * Draw 8-bit image based on input array
     *
     * @param title the image title which shows on the new window
     * @param pixels the image data
     * @param rec it provides the width and height of the image
     */
    public static void createImage(String title, int[] pixels, Rectangle rec) {
        ImageProcessor imgPro = new ByteProcessor(rec.width, rec.height);
        ImagePlus thresholdImp = new ImagePlus(title, imgPro);
        for (int y = 0; y < rec.height; y++) {
            for (int x = 0; x < rec.width; x++) {
                imgPro.putPixel(x, y, pixels[y * rec.width + x]);
            }
        }
        thresholdImp.show();
        IJ.selectWindow(title);
    }

    /**
     * Main method for IDE to run. Not required for imageJ to compile. Do not
     * alter.
     *
     * @param args
     */
    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = MyPlugIn_.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();
    }

}
