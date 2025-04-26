package utils;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class WebcamUtils {
    public static File captureImage() {

        try {
            Webcam webcam = Webcam.getDefault();
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();
            BufferedImage image = webcam.getImage();

            File outputFile = new File("face_capture.jpg");
            ImageIO.write(image, "JPG", outputFile);
            webcam.close();
            return outputFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

