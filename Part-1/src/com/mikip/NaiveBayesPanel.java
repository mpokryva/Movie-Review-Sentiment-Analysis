package com.mikip;

import javax.swing.*;
import java.awt.*;

/**
 * Created by mpokr on 3/19/2017.
 */
public class NaiveBayesPanel extends JPanel {

    private JCheckBox useTfIdf;
    private final String PANEL_TITLE = "Naive Bayes Options";
    private JLabel titleArea;

    public NaiveBayesPanel() {
        useTfIdf = new JCheckBox();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        useTfIdf.setText("Check to use tf-idf instead of unary weighting.");
        titleArea = new JLabel(PANEL_TITLE);
        this.add(titleArea);
        this.add(useTfIdf);
        this.setVisible(true);
    }

    public boolean shouldUserTfIdf() {
        return useTfIdf.isSelected();
    }

    public void disableComponents() {
        useTfIdf.setEnabled(false);
    }

    public void enableComponents() {
        useTfIdf.setEnabled(true);
    }
}
