package com.mikip;

import javax.swing.*;
import java.awt.*;

/**
 * Created by mpokr on 3/19/2017.
 */
public class ResultsPanel extends  JPanel {


    private JTextArea perceptronArea;
    private JTextArea naiveBayesArea;
    private final String PANEL_TITLE = "Results";
    private JLabel titleArea;

    public ResultsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        titleArea = new JLabel(PANEL_TITLE);
        this.add(titleArea);
        perceptronArea = new JTextArea();
        naiveBayesArea = new JTextArea();
        this.add(perceptronArea);
        this.add(naiveBayesArea);
        setVisible(false);
    }

    protected void setPerceptronAreaText(String text) {
        perceptronArea.setText(text);
        setVisible(true);
        repaint();
    }

    public void setNaiveBayesAreaText(String text) {
        naiveBayesArea.setText(text);
        setVisible(true);
        repaint();
    }


}

