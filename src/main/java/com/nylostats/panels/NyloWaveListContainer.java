package com.nylostats.panels;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;

public class NyloWaveListContainer extends JScrollPane
{
    public NyloWaveListContainer(NyloWaveListPanel waveListPanel)
    {
        super(waveListPanel);

        this.setBackground(ColorScheme.DARK_GRAY_COLOR);
        this.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
    }
}
