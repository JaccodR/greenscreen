package com.nylostats;

import com.nylostats.data.NyloWave;
import com.nylostats.panels.*;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

@Getter(AccessLevel.PACKAGE)
public class NyloStatsPanel extends PluginPanel
{
    private NyloWavePanel nyloWavePanel;
    private final TitlePanel titlePanel = new TitlePanel();
    private final NyloWaveListPanel waveListPanel = new NyloWaveListPanel();
    private final NyloWaveListContainer waveListContainer = new NyloWaveListContainer(waveListPanel);

    private final NyloStatsPlugin plugin;
    private final NyloStatsConfig config;

    @Inject
    private NyloStatsPanel(NyloStatsPlugin plugin, NyloStatsConfig config)
    {
        this.plugin = plugin;
        this.config = config;

        setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(this.titlePanel, BorderLayout.NORTH, 0);
        add(this.waveListContainer, BorderLayout.CENTER, 1);
    }

    void addNyloWave(NyloWave wave)
    {
        SwingUtilities.invokeLater(() ->
        {
            nyloWavePanel = new NyloWavePanel(config, wave);
            PlotPanel plotPanel = new PlotPanel(wave.getNylosAlive());
            waveListPanel.add(nyloWavePanel, 0);
            //waveListPanel.add(plotPanel, 1);

            updateUI();
        });
    }

    void addPlot(NyloWave wave)
    {
      SwingUtilities.invokeLater(() ->
      {
          PlotPanel plotPanel = new PlotPanel(wave.getNylosAlive());
          add(plotPanel, 0);

          updateUI();;
      });
    }

    public void clearNyloWaves()
    {
        waveListPanel.removeAll();
        waveListPanel.validate();
        waveListPanel.repaint();
    }
}
