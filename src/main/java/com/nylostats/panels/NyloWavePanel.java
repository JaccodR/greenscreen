package com.nylostats.panels;

import com.nylostats.NyloStatsConfig;
import com.nylostats.data.NyloWave;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Slf4j
@Getter
@Setter
public class NyloWavePanel extends JPanel
{
    private JLabel dateTime;
    private JLabel durationWaves;
    private JLabel durationBoss;
    private JLabel splits;
    private JLabel bossRota;
    private JLabel damageDealt;
    private JLabel idleTicks;
    private WaveImage imageLabel;
    private JPanel textPanel;
    private NyloStatsConfig config;
    private static final Border normalBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 4, 0, ColorScheme.DARK_GRAY_COLOR),
            new EmptyBorder(4, 6, 4, 6));

    private static final Border hoverBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 4, 0, ColorScheme.DARK_GRAY_COLOR),
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_HOVER_COLOR),
                    new EmptyBorder(3, 5, 3, 5)));

    public NyloWavePanel(NyloStatsConfig config, NyloWave wave)
    {
        this.config = config;
        setLayout(new BorderLayout(5, 5));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(normalBorder);

        JPanel wavesPanel = new JPanel();
        wavesPanel.setLayout(new BoxLayout(wavesPanel, BoxLayout.Y_AXIS));
        wavesPanel.setBackground(null);

        JPanel wavesPanelTop = new JPanel();
        wavesPanelTop.setLayout(new BoxLayout(wavesPanelTop, BoxLayout.Y_AXIS));
        wavesPanelTop.setBackground(null);

        JPanel wavesPanelBottom = new JPanel();
        wavesPanelBottom.setLayout(new BoxLayout(wavesPanelBottom, BoxLayout.X_AXIS));
        wavesPanelBottom.setBackground(null);


        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(null);

        JPanel nyloDateLine = new JPanel();
        nyloDateLine.setLayout(new BorderLayout());
        nyloDateLine.setBackground(null);


        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        Date date = new Date();

        dateTime = new JLabel();
        dateTime.setHorizontalAlignment(SwingConstants.CENTER);
        dateTime.setText(formatter.format(date));
        dateTime.setForeground(Color.WHITE);
        nyloDateLine.add(dateTime);


        JPanel durationLine = new JPanel();
        durationLine.setLayout(new BorderLayout());
        durationLine.setBackground(null);

        durationWaves = new JLabel();
        durationWaves.setText("Boss spawn: " + formatTime(wave.getDurationWaves())); // wave duration
        durationWaves.setForeground(Color.WHITE);
        durationLine.add(durationWaves, BorderLayout.WEST);


        JPanel durationBossLine = new JPanel();
        durationBossLine.setLayout(new BorderLayout());
        durationBossLine.setBackground(null);

        durationBoss = new JLabel();
        durationBoss.setText("Boss Time: " + formatTime(wave.getDurationBoss())); // boss duration
        durationBoss.setForeground(Color.WHITE);
        durationBossLine.add(durationBoss, BorderLayout.WEST);

        JPanel splitsLine = new JPanel();
        splitsLine.setLayout(new BorderLayout());
        splitsLine.setBackground(null);


        String splitsText = "<html><font color='white'>Splits: [<font color='red'>" + wave.getSplits()[0] +
                "</font>] [<font color='green'>" + wave.getSplits()[1] + "</font>] [<font color='blue'>" +
                wave.getSplits()[2] + "</font>]</font>";
        splitsLine.add(new JLabel(splitsText), BorderLayout.WEST);

        JPanel bossRotaLine = new JPanel();
        bossRotaLine.setLayout(new BorderLayout());
        bossRotaLine.setBackground(null);

        String bossRotaText = "<html><font color='white'>Boss Rota: [<font color='red'>" + wave.getBossRota()[0] +
                "</font>] [<font color='green'>" + wave.getBossRota()[2] + "</font>] [<font color='blue'>" +
                wave.getBossRota()[1] + "</font>]</font>";
        bossRotaLine.add(new JLabel(bossRotaText), BorderLayout.WEST);


        JPanel damageDealtLine = new JPanel();
        damageDealtLine.setLayout(new BorderLayout());
        damageDealtLine.setBackground(null);

        damageDealt = new JLabel();
        damageDealt.setText("Damage Dealt: " + wave.getDamageDealt()); // dmg dealt
        damageDealt.setForeground(Color.WHITE);
        damageDealtLine.add(damageDealt, BorderLayout.WEST);

        JPanel idleTicksLine = new JPanel();
        idleTicksLine.setLayout(new BorderLayout());
        idleTicksLine.setBackground(null);

        idleTicks = new JLabel();
        idleTicks.setText("Idle Ticks: " + wave.getIdleTicks()); //idle ticks
        idleTicks.setForeground(Color.WHITE);
        idleTicksLine.add(idleTicks, BorderLayout.WEST);

        textPanel.add(durationLine);
        textPanel.add(durationBossLine);
        textPanel.add(splitsLine);
        textPanel.add(bossRotaLine);
        textPanel.add(damageDealtLine);
        textPanel.add(idleTicksLine);

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
        imagePanel.setBackground(null);

        imageLabel = new WaveImage();
        imagePanel.add(imageLabel);



        wavesPanelTop.add(nyloDateLine);
        wavesPanelBottom.add(textPanel);
        wavesPanelBottom.add(imagePanel);

        wavesPanel.add(wavesPanelTop, BorderLayout.NORTH);
        wavesPanel.add(wavesPanelBottom, BorderLayout.SOUTH);

        add(wavesPanel, BorderLayout.NORTH);
    }

    private String formatTime(int ticks)
    {
        int tickMs = ticks * 600;
        String hundredths = String.valueOf(tickMs % 1000).substring(0, 1);
        return String.format("%d:%02d.%s",
                TimeUnit.MILLISECONDS.toMinutes(tickMs) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(tickMs) % TimeUnit.MINUTES.toSeconds(1),
                hundredths);
    }
}
