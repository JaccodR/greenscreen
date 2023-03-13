package com.nylostats.panels;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class PlotPanel extends JPanel
{
    final ArrayList<Integer> nylosAlive;
    private int paddingX = 7;
    private int paddingY = 10;
    private int labelPadding = 7;
    private Color lineColor = Color.RED;
    private Color pointColor = Color.RED;
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 1;
    private float fontSize = 13.0f;
    private int numberYDivisions = 5;
    private int numberXDivisions = 5;
    
    public PlotPanel(ArrayList<Integer> nylosAlive)
    {
        this.nylosAlive = nylosAlive;
        setBorder(new EmptyBorder(45, 45, 55, 45));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double xScale = ((double) getWidth() - (2 * paddingX) - labelPadding) / (nylosAlive.size() - 1);
        double yScale = ((double) getHeight() - 2 * paddingY - labelPadding) / (getMaxAlive() - getMinAlive());

        ArrayList<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < nylosAlive.size(); i++)
        {
            int x1 = (int) (i * xScale + paddingX + labelPadding);
            int y1 = (int) ((getMaxAlive() - nylosAlive.get(i)) * yScale + paddingY);
            graphPoints.add(new Point(x1, y1));
        }

        g2.setColor(Color.BLACK);
        //g2.fillRect(paddingX + labelPadding, paddingY, getWidth() - (2 * paddingX) - labelPadding, getHeight() - 2 * paddingY - labelPadding);
        g2.setColor(Color.WHITE);

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i < numberYDivisions + 1; i++)
        {
            int x0 = paddingX + labelPadding;
            int x1 = pointWidth + paddingX + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - paddingY * 2 - labelPadding)) / numberYDivisions + paddingY + labelPadding);
            int y1 = y0;
            if (nylosAlive.size() > 0)
            {
                g2.setColor(gridColor);
                g2.drawLine(paddingX + labelPadding + 1 + pointWidth, y0, getWidth() - paddingX, y1);
                g2.setColor(Color.WHITE);
                int labelY = (int) (((int) ((getMinAlive() + (getMaxAlive() - getMinAlive()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0);
                String yLabel = labelY + "";
                g2.setFont(getFont().deriveFont(fontSize));
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g2.drawString(yLabel, x0 - labelWidth - 1, y0 + (metrics.getHeight() / 2) - 1);
            }
            g2.drawLine(x0, y0, x1, y1);
        }

        // and for x axis

        for (int i = 0; i < numberXDivisions + 1; i++)
        {
            int x0 = i * (getWidth() - paddingX * 2 - labelPadding) / numberXDivisions + paddingX + labelPadding;
            int x1 = x0;
            int y0 = getHeight() - paddingY - labelPadding;
            int y1 = y0 - pointWidth;
            if (nylosAlive.size() > 0)
            {
                g2.setColor(gridColor);
                g2.drawLine(x0, getHeight() - paddingY - labelPadding - 1 - pointWidth, x1, paddingY);
                g2.setColor(Color.WHITE);
                int labelX =  (int) (((int) ((getMinAlive() + (getMaxAlive() - getMinAlive()) * ((i * 1.0) / numberXDivisions)) * 100)) / 100.0);
                String xLabel = labelX + "";
                g2.setFont(getFont().deriveFont(fontSize));
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(xLabel);
                g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
            }
            g2.drawLine(x0, y0, x1, y1);
        }


        // create x and y axes
        g2.drawLine(paddingX + labelPadding, getHeight() - paddingY - labelPadding, paddingX + labelPadding, paddingY);
        g2.drawLine(paddingX + labelPadding, getHeight() - paddingY - labelPadding, getWidth() - paddingX, getHeight() - paddingY - labelPadding);

        Stroke oldStroke = g2.getStroke();
        g2.setColor(lineColor);
        g2.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(oldStroke);
        g2.setColor(pointColor);
        for (int i = 0; i < graphPoints.size(); i++)
        {
            int x = graphPoints.get(i).x - pointWidth / 2;
            int y = graphPoints.get(i).y - pointWidth / 2;
            int ovalW = pointWidth;
            int ovalH = pointWidth;
            g2.fillOval(x, y, ovalW, ovalH);
        }
    }

    private double getMinAlive()
    {
        double minScore = Double.MAX_VALUE;
        for (int alive : nylosAlive)
        {
            minScore = Math.min(minScore, alive);
        }
        return minScore;
    }

    private double getMaxAlive()
    {
        double maxScore = Double.MIN_VALUE;
        for (int alive : nylosAlive)
        {
            maxScore = Math.max(maxScore, alive);
        }
        return maxScore;
    }
}
