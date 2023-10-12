package com.nylostats.panels;

import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class WaveImage extends JLabel
{
    public WaveImage()
    {
        double IMAGE_SCALE = 2;
        BufferedImage image = ImageUtil.loadImageResource(getClass(), "/util/icon.png");
        Image scaledImage = image.getScaledInstance((int) (image.getWidth() * IMAGE_SCALE), (int) (image.getHeight() * IMAGE_SCALE),
                                                Image.SCALE_DEFAULT);
        this.setIcon(new ImageIcon(scaledImage));
    }
}
