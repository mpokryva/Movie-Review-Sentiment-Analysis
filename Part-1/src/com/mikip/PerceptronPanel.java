package com.mikip;

import javax.swing.*;
import java.awt.*;

/**
 * Created by mpokr on 3/19/2017.
 */
public class PerceptronPanel extends JPanel {

    private JCheckBox initRandWeights;
    private JTextField learningRate;
    private JCheckBox useTfIdf;
    private final String PANEL_TITLE = "Perceptron Options";
    private JLabel titleArea;

    public PerceptronPanel() {
        initRandWeights = new JCheckBox();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initRandWeights.setText("Initialize weights to random numbers between -1.0 and +1.0." +
                "(Initialized to 0 by default)");
        learningRate = new JTextField();
        learningRate.setText("Specify learning rate between 0 and 1.0. Default is 0.15.");
        useTfIdf = new JCheckBox();
        useTfIdf.setText("Use tf-idf instead of unary weighting.");
        titleArea = new JLabel(PANEL_TITLE);
        this.add(titleArea);
        this.add(useTfIdf);
        this.add(initRandWeights);
        this.add(learningRate);
        this.setVisible(true);
    }

    public boolean getUseTfIdf() {
        return useTfIdf.isSelected();
    }

    public boolean shouldInitRandomWeights() {
        return initRandWeights.isSelected();
    }

    public double getLearningRate() {
        try {
            double learningRateValue =  Double.parseDouble(learningRate.getText());
            return learningRateValue;
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    public void disableComponents() {
        useTfIdf.setEnabled(false);
        useTfIdf.setEnabled(false);
        learningRate.setEnabled(false);
    }


    public void enableComponents() {
        useTfIdf.setEnabled(true);
        useTfIdf.setEnabled(true);
        learningRate.setEnabled(true);
    }
}
