package de.uniks.stpmon.team_m.utils;


import de.uniks.stpmon.team_m.App;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import okhttp3.ResponseBody;
import java.io.InputStream;

import static de.uniks.stpmon.team_m.Constants.*;

public class ImageProcessor {
    public static String toBase64(String inputImagePath) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(inputImagePath));
            return getBase64(originalImage);
        } catch (IOException e) {
            return IMAGE_PROCESSING_ERROR;
        }
    }

    public static javafx.scene.image.Image fromBase64ToFXImage(String avatar) {
        javafx.scene.image.Image image;
        if (avatar == null) {
            image = new javafx.scene.image.Image(Objects.requireNonNull(App.class.getResource(AVATAR_1)).toString());
            return image;
        }
        if (avatar.startsWith("data:image/png;base64, ")){
            byte[] imageBytes = Base64.getDecoder().decode(avatar.replaceFirst("data:image/png;base64, ", ""));
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            image = new javafx.scene.image.Image(bis);
        } else if (avatar.startsWith("data:image/jpg;base64, ")){
                byte[] imageBytes = Base64.getDecoder().decode(avatar.replaceFirst("data:image/jpg;base64, ", ""));
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                image = new javafx.scene.image.Image(bis);
        } else {
            image = new javafx.scene.image.Image(Objects.requireNonNull(App.class.getResource(AVATAR_2)).toString());
        }
        return image;
    }

    private static String getBase64(BufferedImage image) throws IOException {
        double scaleFactor = 1.0;
        while (true) {
            BufferedImage scaledImage = scaleImage(image, scaleFactor);
            String base64Image;
            base64Image = convertToBase64(scaledImage);

            if (base64Image.length() <= MAX_BASE64_LENGTH) {
                return base64Image;
            }

            scaleFactor *= 0.9;
        }
    }

    private static BufferedImage scaleImage(BufferedImage image, double scaleFactor) {
        int scaledWidth = (int) (image.getWidth() * scaleFactor);
        int scaledHeight = (int) (image.getHeight() * scaleFactor);

        java.awt.Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, java.awt.Image.SCALE_SMOOTH);

        BufferedImage bufferedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return bufferedImage;
    }
    /**
     * This method is used to crop the
     * @param trainerChumk into 6 Images, that displays the player character moving in some direction
     * @param direction : either "up", "down", "left" or "right" are possible directions
     * @param isWalking : determine if the character is moving or not
     */
    public static Image[] cropTrainerImages(Image trainerChumk, String direction, Boolean isWalking) {
        Image[] array = new Image[6];
        int x,y;
        if (isWalking) {
            y = 71;
        }
        else {
            y = 39;
        }
        switch (direction) {
            case "right"    -> x = 0;
            case "up"       -> x = 96;
            case "left"     -> x = 192;
            default         -> x = 288;
        }
        for (int i=0; i < 6; i++) {
            array[i] = getSubImage(trainerChumk, x, y, 16, 25);
            x += 16;
        }
        return array;
    }

    public static Image getSubImage(Image img, int x, int y, int w, int h) {
        PixelReader reader = img.getPixelReader();
        return new WritableImage(reader, x, y, w, h);
    }

    private static String convertToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        byte[] imageBytes = baos.toByteArray();
        baos.close();

        return Base64.getEncoder().encodeToString(imageBytes);
    }
  
  public static Image resonseBodyToJavaFXImage(ResponseBody responseBody) throws IOException {
        try (InputStream inputStream = responseBody.byteStream()) {
            byte[] imageData = toByteArray(inputStream);
            return new Image(new ByteArrayInputStream(imageData));
        }
    }

    private static byte[] toByteArray(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }
}

