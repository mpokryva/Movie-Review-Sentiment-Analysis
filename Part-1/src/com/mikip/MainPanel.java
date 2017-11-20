package com.mikip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by mpokr on 3/19/2017.
 */
public class MainPanel extends JFrame {

    private PerceptronPanel perceptronPanel;
    private NaiveBayesPanel naiveBayesPanel;
    private JButton startButton;
    private ResultsPanel resultArea;
    private JCheckBox ignorePunctuation;
    private JLabel generalOptionsTitle;
    private final String GENERAL_OPTIONS_TITLE = "General Options";


    public MainPanel() {
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIManager.put("Label.font", new Font("Arial", Font.BOLD, 45));
        UIManager.put("CheckBox.font", new Font("Arial", Font.PLAIN, 36));
        UIManager.put("TextArea.font", new Font("Arial", Font.PLAIN, 36));
        UIManager.put("TextLabel.font", new Font("Arial", Font.PLAIN, 36));
        UIManager.put("TextField.font", new Font("Arial", Font.PLAIN, 36));
        UIManager.put("Button.font", new Font("Arial", Font.PLAIN, 36));
        perceptronPanel = new PerceptronPanel();
        naiveBayesPanel = new NaiveBayesPanel();
        startButton = new JButton("Start classification");
        addStartButtonListener();
        ignorePunctuation = new JCheckBox("Ignore punctuation");
        resultArea = new ResultsPanel();
        generalOptionsTitle = new JLabel(GENERAL_OPTIONS_TITLE);
        Font titleFont = generalOptionsTitle.getFont();
        generalOptionsTitle.setFont(titleFont);
        this.add(perceptronPanel);
        this.add(naiveBayesPanel);
        this.add(generalOptionsTitle);
        this.add(ignorePunctuation);
        this.add(startButton);
        this.add(resultArea);
        pack();
        this.setVisible(true);
    }

    private void addStartButtonListener() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double percepLearningRate = perceptronPanel.getLearningRate();
                if (percepLearningRate <= 0 || percepLearningRate >= 1) {
                    resultArea.setPerceptronAreaText("Perceptron learning is invalid. Make sure it is a number " +
                            "greater than 0 and less than 1.");
                } else {
                    perceptronPanel.disableComponents();
                    naiveBayesPanel.disableComponents();
                    ignorePunctuation.setEnabled(false);
                    String[] classificationResults = startClassification();
                    resultArea.setPerceptronAreaText(classificationResults[0]);
                    resultArea.setNaiveBayesAreaText(classificationResults[1]);
                    perceptronPanel.enableComponents();
                    naiveBayesPanel.enableComponents();
                    ignorePunctuation.setEnabled(true);
                }
                MainPanel.this.repaint();
                MainPanel.this.pack();
            }
        });
    }

    public static void main(String[] args) {
        MainPanel mainPanel = new MainPanel();
    }

    public String[] startClassification() {
        String currentDir = System.getProperty("user.dir");
        ReviewDBParser dbParser;
        String perceptronResults = "";
        if (currentDir.contains("src")) {
            dbParser = new ReviewDBParser(new File("../txt_sentoken"), ignorePunctuation.isSelected());
        } else {
            dbParser = new ReviewDBParser(new File("./txt_sentoken"), ignorePunctuation.isSelected());
        }

        int reviewsCorrect = 0;
        java.util.List<FoldStats> perceptronFoldStats = new ArrayList<>();
        boolean percepUseTfIdf = perceptronPanel.getUseTfIdf();
        boolean percepInitRandomWeights = perceptronPanel.shouldInitRandomWeights();
        double learningRate = perceptronPanel.getLearningRate();
        for (java.util.List<MovieReview> trainingList : dbParser.getTrainingToTestingMap().keySet()) {
            FoldStats currentStats = new FoldStats();
            dbParser.initPerceptron(trainingList, percepUseTfIdf, percepInitRandomWeights, learningRate);
            dbParser.getPerceptron().trainAll();
            java.util.List<MovieReview> testingList = dbParser.getTrainingToTestingMap().get(trainingList);
            for (MovieReview review : testingList) {
                boolean classification = dbParser.getPerceptron().classify(review);
                if (classification == review.getSentiment()) {
                    reviewsCorrect++;
                    if (classification) {
                        currentStats.incrementTruePos();
                    } else {
                        currentStats.incrementTrueNeg();
                    }
                } else {
                    if (classification) {
                        currentStats.incrementFalsePos();
                    } else {
                        currentStats.incrementFalseNeg();
                    }
                }
            }
            perceptronFoldStats.add(currentStats);
        }
        double[] maxPerceptronStats = getMaxStats(perceptronFoldStats);
        double maxPerceptronPrecision = maxPerceptronStats[0];
        maxPerceptronPrecision = round(maxPerceptronPrecision, 2);

        double maxPerceptronRecall = maxPerceptronStats[1];
        maxPerceptronRecall = round(maxPerceptronRecall, 2);

        double maxPerceptronAccuracy = maxPerceptronStats[2];
        maxPerceptronAccuracy = round(maxPerceptronAccuracy, 2);


        double[] minPerceptronStats = getMinStats(perceptronFoldStats);
        double minPerceptronPrecision = minPerceptronStats[0];
        minPerceptronPrecision = round(minPerceptronPrecision, 2);

        double minPerceptronRecall = minPerceptronStats[1];
        minPerceptronRecall = round(minPerceptronRecall, 2);

        double minPerceptronAccuracy = minPerceptronStats[2];
        minPerceptronAccuracy = round(minPerceptronAccuracy, 2);


        double[] meanPerceptronStats = getMeanStats(perceptronFoldStats);
        double meanPerceptronPrecision = meanPerceptronStats[0];
        meanPerceptronPrecision = round(meanPerceptronPrecision, 2);

        double meanPerceptronRecall = meanPerceptronStats[1];
        meanPerceptronRecall = round(meanPerceptronRecall, 2);

        double meanPerceptronAccuracy = meanPerceptronStats[2];
        meanPerceptronAccuracy = round(meanPerceptronAccuracy, 2);


        perceptronResults += ("Perceptron: " + reviewsCorrect + " / " + dbParser.getMovieReviews().size() + " correct.") + "\n";
        perceptronResults += ("-------------------------") + "\n";
        perceptronResults += ("Average Perceptron stats: ") + "\n";
        perceptronResults += "\n";
        perceptronResults += ("Average Perceptron precision: " + meanPerceptronPrecision) + "\n";
        perceptronResults += ("Average Perceptron recall: " + meanPerceptronRecall) + "\n";
        perceptronResults += ("Average Perceptron accuracy: " + meanPerceptronAccuracy) + "\n";
        perceptronResults += ("-------------------------") + "\n";
        perceptronResults += ("Minimum Perceptron stats: ") + "\n";
        perceptronResults += "\n";
        perceptronResults += ("Minimum Perceptron precision: " + minPerceptronPrecision) + "\n";
        perceptronResults += ("Minimum Perceptron recall: " + minPerceptronRecall) + "\n";
        perceptronResults += ("Minimum Perceptron accuracy: " + minPerceptronAccuracy) + "\n";
        perceptronResults += ("-------------------------") + "\n";
        perceptronResults += ("Maximum Perceptron stats: ") + "\n";
        perceptronResults += "\n";
        perceptronResults += ("Maximum Perceptron precision: " + maxPerceptronPrecision) + "\n";
        perceptronResults += ("Maximum Perceptron recall: " + maxPerceptronRecall) + "\n";
        perceptronResults += ("Maximum Perceptron accuracy: " + maxPerceptronAccuracy) + "\n";

        reviewsCorrect = 0;


        String naiveBayesResults = "";

        boolean naiveBayesUseTfIdf = naiveBayesPanel.shouldUserTfIdf();
        java.util.List<FoldStats> naiveBayesFoldStats = new ArrayList<>();

        for (java.util.List<MovieReview> trainingList : dbParser.getTrainingToTestingMap().keySet()) {
            dbParser.initNaiveBayes(trainingList, naiveBayesUseTfIdf);
            FoldStats currentStats = new FoldStats();
            java.util.List<MovieReview> testingList = dbParser.getTrainingToTestingMap().get(trainingList);
            for (MovieReview review : testingList) {
                boolean classification = dbParser.getNaiveBayes().trainReview(review);
                if (classification == review.getSentiment()) {
                    reviewsCorrect++;
                    if (classification) {
                        currentStats.incrementTruePos();
                    } else {
                        currentStats.incrementTrueNeg();
                    }
                } else {
                    if (classification) {
                        currentStats.incrementFalsePos();
                    } else {
                        currentStats.incrementFalseNeg();
                    }
                }
            }
            naiveBayesFoldStats.add(currentStats);
        }


        double[] maxNaiveBayesStats = getMaxStats(naiveBayesFoldStats);
        double maxNaiveBayesPrecision = maxNaiveBayesStats[0];
        maxNaiveBayesPrecision = round(maxNaiveBayesPrecision, 2);

        double maxNaiveBayesRecall = maxNaiveBayesStats[1];
        maxNaiveBayesRecall = round(maxNaiveBayesRecall, 2);

        double maxNaiveBayesAccuracy = maxNaiveBayesStats[2];
        maxNaiveBayesAccuracy = round(maxNaiveBayesAccuracy, 2);

        double[] minNaiveBayesStats = getMinStats(naiveBayesFoldStats);
        double minNaiveBayesPrecision = minNaiveBayesStats[0];
        minNaiveBayesPrecision = round(minNaiveBayesPrecision, 2);

        double minNaiveBayesRecall = minNaiveBayesStats[1];
        minNaiveBayesRecall = round(minNaiveBayesRecall, 2);

        double minNaiveBayesAccuracy = minNaiveBayesStats[2];
        minNaiveBayesAccuracy = round(minNaiveBayesAccuracy, 2);

        double[] meanNaiveBayesStats = getMeanStats(naiveBayesFoldStats);
        double meanNaiveBayesPrecision = meanNaiveBayesStats[0];
        meanNaiveBayesPrecision = round(meanNaiveBayesPrecision, 2);

        double meanNaiveBayesRecall = meanNaiveBayesStats[1];
        meanNaiveBayesRecall = round(meanNaiveBayesRecall, 2);

        double meanNaiveBayesAccuracy = meanNaiveBayesStats[2];
        meanNaiveBayesAccuracy = round(meanNaiveBayesAccuracy, 2);


        naiveBayesResults += ("Naive Bayes: " + reviewsCorrect + " / " + dbParser.getMovieReviews().size() + " correct.") + "\n";
        naiveBayesResults += ("-------------------------") + "\n";
        naiveBayesResults += ("Average Naive Bayes stats: ") + "\n";
        naiveBayesResults += "\n";
        naiveBayesResults += ("Average Naive Bayes precision: " + meanNaiveBayesPrecision) + "\n";
        naiveBayesResults += ("Average Naive Bayes recall: " + meanNaiveBayesRecall) + "\n";
        naiveBayesResults += ("Average Naive Bayes accuracy: " + meanNaiveBayesAccuracy) + "\n";
        naiveBayesResults += ("-------------------------") + "\n";
        naiveBayesResults += ("Minimum Naive Bayes stats: ") + "\n";
        naiveBayesResults += "\n";
        naiveBayesResults += ("Minimum Naive Bayes precision: " + minNaiveBayesPrecision) + "\n";
        naiveBayesResults += ("Minimum Naive Bayes recall: " + minNaiveBayesRecall) + "\n";
        naiveBayesResults += ("Minimum Naive Bayes accuracy: " + minNaiveBayesAccuracy) + "\n";
        naiveBayesResults += ("-------------------------") + "\n";
        naiveBayesResults += ("Maximum Naive Bayes stats: ") + "\n";
        naiveBayesResults += "\n";
        naiveBayesResults += ("Maximum Naive Bayes precision: " + maxNaiveBayesPrecision) + "\n";
        naiveBayesResults += ("Maximum Naive Bayes recall: " + maxNaiveBayesRecall) + "\n";
        naiveBayesResults += ("Maximum Naive Bayes accuracy: " + maxNaiveBayesAccuracy) + "\n";
        return new String[]{perceptronResults, naiveBayesResults};
    }

    /**
     * Given list of FoldStats, return the an array of doubles containing the average precision, recall, and accuracy,
     * in that order.
     *
     * @param foldStatsList List of fold statistics.
     * @return An array of doubles containing the average precision, recall, and accuracy,
     * in that order.
     */
    private double[] getMeanStats(java.util.List<FoldStats> foldStatsList) {
        double meanAccuracy = 0;
        double meanRecall = 0;
        double meanPrecision = 0;
        for (FoldStats foldStat : foldStatsList) {
            double foldAccuracy = foldStat.getAccuracy();
            double foldRecall = foldStat.getRecall();
            double foldPrecision = foldStat.getPrecision();
            meanAccuracy += foldAccuracy;
            meanRecall += foldRecall;
            meanPrecision += foldPrecision;
        }
        meanAccuracy /= foldStatsList.size();
        meanRecall /= foldStatsList.size();
        meanPrecision /= foldStatsList.size();
        return new double[]{meanPrecision, meanRecall, meanAccuracy};
    }

    private double[] getMaxStats(List<FoldStats> foldStatsList) {
        double maxAccuracy = -1;
        double maxRecall = -1;
        double maxPrecision = -1;
        for (FoldStats foldStat : foldStatsList) {
            double foldAccuracy = foldStat.getAccuracy();
            double foldRecall = foldStat.getRecall();
            double foldPrecision = foldStat.getPrecision();
            if (foldAccuracy > maxAccuracy) {
                maxAccuracy = foldAccuracy;
            }
            if (foldRecall > maxRecall) {
                maxRecall = foldRecall;
            }
            if (foldPrecision > maxPrecision) {
                maxPrecision = foldPrecision;
            }
        }
        return new double[]{maxPrecision, maxRecall, maxAccuracy};
    }

    private double[] getMinStats(List<FoldStats> foldStatsList) {
        double minAccuracy = Double.MAX_VALUE;
        double minRecall = Double.MAX_VALUE;
        double minPrecision = Double.MAX_VALUE;
        for (FoldStats foldStat : foldStatsList) {
            double foldAccuracy = foldStat.getAccuracy();
            double foldRecall = foldStat.getRecall();
            double foldPrecision = foldStat.getPrecision();
            if (foldAccuracy < minAccuracy) {
                minAccuracy = foldAccuracy;
            }
            if (foldRecall < minRecall) {
                minRecall = foldRecall;
            }
            if (foldPrecision < minPrecision) {
                minPrecision = foldPrecision;
            }
        }
        return new double[]{minPrecision, minRecall, minAccuracy};
    }

    private double round(double value, int places) {
        double placesNum = Math.pow(10, places);
        return (double)Math.round(value * placesNum) / placesNum;
    }


}
