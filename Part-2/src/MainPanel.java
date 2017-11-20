import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 3/19/2017.
 */
public class MainPanel extends JFrame {

    private KNNPanel KNNPanel;
    private RocchioPanel rocchioPanel;
    private JButton startButton;
    private ResultsPanel resultArea;
    private JCheckBox ignorePunctuation;
    private JLabel generalOptionsTitle;
    private final String GENERAL_OPTIONS_TITLE = "General Options";


    public MainPanel() {
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIManager.put("Label.font", new Font("Arial", Font.BOLD, 20));
        UIManager.put("CheckBox.font", new Font("Arial", Font.PLAIN, 16));
        UIManager.put("TextArea.font", new Font("Arial", Font.PLAIN, 16));
        UIManager.put("TextLabel.font", new Font("Arial", Font.PLAIN, 16));
        KNNPanel = new KNNPanel();
        rocchioPanel = new RocchioPanel();
        startButton = new JButton("Start classification");
        addStartButtonListener();
        ignorePunctuation = new JCheckBox("Ignore punctuation");
        resultArea = new ResultsPanel();
        generalOptionsTitle = new JLabel(GENERAL_OPTIONS_TITLE);
        Font titleFont = generalOptionsTitle.getFont();
        generalOptionsTitle.setFont(titleFont);
        this.add(KNNPanel);
        this.add(rocchioPanel);
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
                double K = KNNPanel.getK();
                if (K < 0) {
                    resultArea.setKNNAreaText("K-NN K value invalid. Make sure it is an integer " +
                            "and is greater than or equal to 0");
                } else {
                    KNNPanel.disableComponents();
                    rocchioPanel.disableComponents();
                    ignorePunctuation.setEnabled(false);
                    String[] classificationResults = startClassification();
                    resultArea.setKNNAreaText(classificationResults[0]);
                    resultArea.setRocchioAreaText(classificationResults[1]);
                    KNNPanel.enableComponents();
                    rocchioPanel.enableComponents();
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
        String KNNResults = "";
        if (currentDir.contains("src")) {
            dbParser = new ReviewDBParser(new File("../txt_sentoken"), ignorePunctuation.isSelected());
        } else {
            dbParser = new ReviewDBParser(new File("./txt_sentoken"), ignorePunctuation.isSelected());
        }

        int reviewsCorrect = 0;
        List<FoldStats> KNNFoldStats = new ArrayList<>();
        boolean KNNUseTfIdf = KNNPanel.getUseTfIdf();
        String KNNDistanceMetric = (KNNPanel.shouldUseManhattan()) ? KNearestClassifier.MANHATTAN : KNearestClassifier.EUCLIDEAN;
        int K = KNNPanel.getK();
        for (List<MovieReview> trainingList : dbParser.getTrainingToTestingMap().keySet()) {
            FoldStats currentStats = new FoldStats();
            dbParser.initKNearest(K, trainingList, KNNUseTfIdf, KNNDistanceMetric);
            List<MovieReview> testingList = dbParser.getTrainingToTestingMap().get(trainingList);
            for (MovieReview review : testingList) {
                boolean classification = dbParser.getKNN().classifyReview(review);
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
            KNNFoldStats.add(currentStats);
        }
        double[] maxKNNStats = getMaxStats(KNNFoldStats);
        double maxKNNPrecision = maxKNNStats[0];
        maxKNNPrecision = round(maxKNNPrecision, 2);

        double maxKNNRecall = maxKNNStats[1];
        maxKNNRecall = round(maxKNNRecall, 2);

        double maxKNNAccuracy = maxKNNStats[2];
        maxKNNAccuracy = round(maxKNNAccuracy, 2);


        double[] minKNNStats = getMinStats(KNNFoldStats);
        double minKNNPrecision = minKNNStats[0];
        minKNNPrecision = round(minKNNPrecision, 2);

        double minKNNRecall = minKNNStats[1];
        minKNNRecall = round(minKNNRecall, 2);

        double minKNNAccuracy = minKNNStats[2];
        minKNNAccuracy = round(minKNNAccuracy, 2);


        double[] meanKNNStats = getMeanStats(KNNFoldStats);
        double meanKNNPrecision = meanKNNStats[0];
        meanKNNPrecision = round(meanKNNPrecision, 2);

        double meanKNNRecall = meanKNNStats[1];
        meanKNNRecall = round(meanKNNRecall, 2);

        double meanKNNAccuracy = meanKNNStats[2];
        meanKNNAccuracy = round(meanKNNAccuracy, 2);


        KNNResults += ("K-NN: " + reviewsCorrect + " / " + dbParser.getMovieReviews().size() + " correct.") + "\n";
        KNNResults += ("-------------------------") + "\n";
        KNNResults += ("Average K-NN stats: ") + "\n";
        KNNResults += "\n";
        KNNResults += ("Average K-NN precision: " + meanKNNPrecision) + "\n";
        KNNResults += ("Average K-NN recall: " + meanKNNRecall) + "\n";
        KNNResults += ("Average K-NN accuracy: " + meanKNNAccuracy) + "\n";
        KNNResults += ("-------------------------") + "\n";
        KNNResults += ("Minimum K-NN stats: ") + "\n";
        KNNResults += "\n";
        KNNResults += ("Minimum K-NN precision: " + minKNNPrecision) + "\n";
        KNNResults += ("Minimum K-NN recall: " + minKNNRecall) + "\n";
        KNNResults += ("Minimum K-NN accuracy: " + minKNNAccuracy) + "\n";
        KNNResults += ("-------------------------") + "\n";
        KNNResults += ("Maximum K-NN stats: ") + "\n";
        KNNResults += "\n";
        KNNResults += ("Maximum K-NN precision: " + maxKNNPrecision) + "\n";
        KNNResults += ("Maximum K-NN recall: " + maxKNNRecall) + "\n";
        KNNResults += ("Maximum K-NN accuracy: " + maxKNNAccuracy) + "\n";

        reviewsCorrect = 0;


        String rocchioResults = "";

        boolean rocchioUseTfIdf = rocchioPanel.shouldUseTfIdf();
        List<FoldStats> rocchioFoldStats = new ArrayList<>();
        String rocchioDistanceMetric = (rocchioPanel.shouldUseManhattan()) ? RocchioClassifier.MANHATTAN : RocchioClassifier.EUCLIDEAN;
        for (List<MovieReview> trainingList : dbParser.getTrainingToTestingMap().keySet()) {
            dbParser.initRocchio(trainingList, rocchioUseTfIdf, rocchioDistanceMetric);
            RocchioClassifier rocchio = dbParser.getRocchio();
            rocchio.train();
            FoldStats currentStats = new FoldStats();
            List<MovieReview> testingList = dbParser.getTrainingToTestingMap().get(trainingList);
            for (MovieReview review : testingList) {
                boolean classification = rocchio.classifyReview(review);
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
            rocchioFoldStats.add(currentStats);
        }


        double[] maxRocchioStats = getMaxStats(rocchioFoldStats);
        double maxRocchioPrecision = maxRocchioStats[0];
        maxRocchioPrecision = round(maxRocchioPrecision, 2);

        double maxRocchioRecall = maxRocchioStats[1];
        maxRocchioRecall = round(maxRocchioRecall, 2);

        double maxRocchioAccuracy = maxRocchioStats[2];
        maxRocchioAccuracy = round(maxRocchioAccuracy, 2);

        double[] minRocchioStats = getMinStats(rocchioFoldStats);
        double minRocchioPrecision = minRocchioStats[0];
        minRocchioPrecision = round(minRocchioPrecision, 2);

        double minRocchioRecall = minRocchioStats[1];
        minRocchioRecall = round(minRocchioRecall, 2);

        double minRocchioAccuracy = minRocchioStats[2];
        minRocchioAccuracy = round(minRocchioAccuracy, 2);

        double[] meanRocchioStats = getMeanStats(rocchioFoldStats);
        double meanRocchioPrecision = meanRocchioStats[0];
        meanRocchioPrecision = round(meanRocchioPrecision, 2);

        double meanRocchioRecall = meanRocchioStats[1];
        meanRocchioRecall = round(meanRocchioRecall, 2);

        double meanRocchioAccuracy = meanRocchioStats[2];
        meanRocchioAccuracy = round(meanRocchioAccuracy, 2);


        rocchioResults += ("Rocchio: " + reviewsCorrect + " / " + dbParser.getMovieReviews().size() + " correct.") + "\n";
        rocchioResults += ("-------------------------") + "\n";
        rocchioResults += ("Average Rocchio stats: ") + "\n";
        rocchioResults += "\n";
        rocchioResults += ("Average Rocchio precision: " + meanRocchioPrecision) + "\n";
        rocchioResults += ("Average Rocchio recall: " + meanRocchioRecall) + "\n";
        rocchioResults += ("Average Rocchio accuracy: " + meanRocchioAccuracy) + "\n";
        rocchioResults += ("-------------------------") + "\n";
        rocchioResults += ("Minimum Rocchio stats: ") + "\n";
        rocchioResults += "\n";
        rocchioResults += ("Minimum Rocchio precision: " + minRocchioPrecision) + "\n";
        rocchioResults += ("Minimum Rocchio recall: " + minRocchioRecall) + "\n";
        rocchioResults += ("Minimum Rocchio accuracy: " + minRocchioAccuracy) + "\n";
        rocchioResults += ("-------------------------") + "\n";
        rocchioResults += ("Maximum Rocchio stats: ") + "\n";
        rocchioResults += "\n";
        rocchioResults += ("Maximum Rocchio precision: " + maxRocchioPrecision) + "\n";
        rocchioResults += ("Maximum Rocchio recall: " + maxRocchioRecall) + "\n";
        rocchioResults += ("Maximum Rocchio accuracy: " + maxRocchioAccuracy) + "\n";
        return new String[]{KNNResults, rocchioResults};
    }

    /**
     * Given list of FoldStats, return the an array of doubles containing the average precision, recall, and accuracy,
     * in that order.
     *
     * @param foldStatsList List of fold statistics.
     * @return An array of doubles containing the average precision, recall, and accuracy,
     * in that order.
     */
    private double[] getMeanStats(List<FoldStats> foldStatsList) {
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
