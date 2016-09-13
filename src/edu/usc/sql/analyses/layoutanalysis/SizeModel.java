package edu.usc.sql.analyses.layoutanalysis;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by mian on 8/18/16.
 */
public class SizeModel {

    private static final double emParameter = 3.5256;
    private static final double emConstant = -0.0684;
    private static final double charParameter = 1.8977;
    private static final double charConstant = .0621;
    private static final double heightParameter = 3.9895;
    private static final double heightConstant= 1.0105;
    // Constants for Samsung Galaxy S5 phone
    private static final int SCREEN_WIDTH = 1080;
    private static final int SCREEN_HEIGHT = 1920 - 75;
    private static final int SCREEN_SCALING_FACTOR = 3;
    private static final int BUTTON_MINWIDTH = 88 * SCREEN_SCALING_FACTOR;
    private static final int BUTTON_MINHEIGHT = 48 * SCREEN_SCALING_FACTOR;

    public static int getWidthBasedonEm(String textSize, int emLength) {
        double fontSize = Double.parseDouble(textSize.replace("sp", ""));
        int unitWidth = (int) Math.round(emParameter * fontSize + emConstant);
        return Math.min(emLength * unitWidth, SCREEN_WIDTH);
    }

    public static int getWidthBasedonText(String textSize, int textLength) {
        double fontSize = Double.parseDouble(textSize.replace("sp", ""));
        int unitWidth = (int) Math.round(charParameter * fontSize + charConstant);
        return Math.min(textLength * unitWidth, SCREEN_WIDTH);
    }

    public static int getWidthBasedonImage(String imagePath) {
        File pic = new File(imagePath);
        int width = 0;
        try {
            BufferedImage bImg = ImageIO.read(new FileInputStream(pic));
            width = bImg.getWidth() * SCREEN_SCALING_FACTOR;

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return Math.min(width, SCREEN_WIDTH);
    }

    public static int getWidthBasedonUnit(String setting) {
        int width = 0;
        if (setting.endsWith("dp") || setting.endsWith("dip")) {
            width = (int) (SCREEN_SCALING_FACTOR * Double.parseDouble(setting.replace("dp", "").replace("dip", "")));
        }
        return width;
    }

    public static int getHeightBaseonText(String textSize) {
        double fontSize = Double.parseDouble(textSize.replace("sp", ""));
        int height = (int) Math.round(heightParameter * fontSize + heightConstant);
        return height;
    }

    public static int getHeightBasedonImage(String imagePath) {
        File pic = new File(imagePath);
        int height = 0;
        try {
            BufferedImage bImg = ImageIO.read(new FileInputStream(pic));
            height = bImg.getHeight() * SCREEN_SCALING_FACTOR;

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return Math.min(height, SCREEN_WIDTH);
    }

    public static int getScreenWidth() {
        return SCREEN_WIDTH;
    }

    public static int getScreenHeight() {
        return SCREEN_HEIGHT;
    }

    public static int getButtonWidth(int estimateWidth) {
        return Math.max(estimateWidth, BUTTON_MINWIDTH);
    }

    public static int getButtonHeight(int estimateHeight) {
        return Math.max(estimateHeight, BUTTON_MINHEIGHT);
    }
}
