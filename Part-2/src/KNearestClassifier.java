import java.util.*;

/**
 * A K-Means Classifier (K-NN). Various parameters can be modified, such as K, how features
 * are normalized, and what distance metric the K-NN uses.
 */
public class KNearestClassifier {

    private int K;
    public static final String EUCLIDEAN = "EUCLIDEAN";
    public static final String MANHATTAN = "MANHATTAN";
    private String distanceMetric;
    private List<MovieReview> movieReviews;
    private HashMap<String, Double> tfIdfMeanMap;
    private HashMap<String, Double> tfIdfVarianceMap;
    private HashMap<String, Double> termToNormTfIdfMap;
    private boolean useTfIdf;
    // Include a field to indicate how features are normalized

    /**
     * Initializes a K-NN with the specified K. Uses Euclidean distance as a similarity metric
     * by default.
     *
     * @param K The number of nearest neighbors the K-NN should look at when classifying.
     */
    public KNearestClassifier(int K, List<MovieReview> movieReviews) {
        this.K = K;
        distanceMetric = EUCLIDEAN;
        this.movieReviews = movieReviews;
    }

    public KNearestClassifier(int K, List<MovieReview> movieReviews, boolean useTfIdf, String distanceMetric) {
        this.K = K;
        this.movieReviews = movieReviews;
        this.useTfIdf = useTfIdf;
        if (distanceMetric == null) {
            this.distanceMetric = EUCLIDEAN;
        } else {
            if (distanceMetric.equals(EUCLIDEAN) || distanceMetric.equals(MANHATTAN)) {
                this.distanceMetric = distanceMetric;
            } else {
                throw new IllegalArgumentException("Invalid distance metric");
            }
        }
        termToNormTfIdfMap = new HashMap<>();
        // Calculate tfidf norm values
        /*
        for (MovieReview review : movieReviews) {
           Iterator<String> wordsIt = review.getBagOfWordsIterator();
            while(wordsIt.hasNext()) {
                String currentWord = wordsIt.next();
                double tfIdfInReview = review.getTfIdfMap().get(currentWord); // tfidf in current review
                Double currentTfIdfNorm = termToNormTfIdfMap.get(currentWord); // current tfidf norm sum
                currentTfIdfNorm = (currentTfIdfNorm == null) ? 0 : currentTfIdfNorm;
                // new tf-idf norm sum fo rthis term = old + (tfidf in current review)^2
                termToNormTfIdfMap.put(currentWord, currentTfIdfNorm + (tfIdfInReview * tfIdfInReview));
            }
        }

        for (String word : termToNormTfIdfMap.keySet()) {
            termToNormTfIdfMap.put(word, Math.sqrt(termToNormTfIdfMap.get(word))); // sqrt sums to get actual norms
        }
        */

        /*
        tfIdfMeanMap = new HashMap<>();
        for (MovieReview review : movieReviews) {
            HashMap<String, Double> reviewTfIdfMap = review.getTfIdfMap();
            for (String word : reviewTfIdfMap.keySet()) {
                Double tfIdf = reviewTfIdfMap.get(word);
                if (tfIdf != null) {
                    Double currentMeanSum = tfIdfMeanMap.get(word);
                    currentMeanSum = (currentMeanSum == null) ? 0 : currentMeanSum;
                    tfIdfMeanMap.put(word, currentMeanSum + tfIdf);
                }
            }
        }
        for (String word : tfIdfMeanMap.keySet()) {
            // Divide by n to get average value
            tfIdfMeanMap.put(word, tfIdfMeanMap.get(word) / movieReviews.size());
        }
        tfIdfVarianceMap = new HashMap<>();
        for (MovieReview review : movieReviews) {
            HashMap<String, Double> reviewTfIdfMap = review.getTfIdfMap();
            for (String word : reviewTfIdfMap.keySet()) {
                Double tfIdf = reviewTfIdfMap.get(word);
                if (tfIdf != null) {
                    double meanTfIdf = tfIdfMeanMap.get(word);
                    double squaredDiff = Math.pow((tfIdf - meanTfIdf), 2);
                    Double currentVarSum = tfIdfVarianceMap.get(word);
                    currentVarSum = (currentVarSum == null) ? 0 : currentVarSum;
                    tfIdfVarianceMap.put(word, currentVarSum + squaredDiff);
                }
            }
        }
        for (String word : tfIdfVarianceMap.keySet()) {
            tfIdfVarianceMap.put(word, tfIdfVarianceMap.get(word) / movieReviews.size());
        }
        */
    }

    public boolean classifyReview(MovieReview testReview) {
        // long startCDTime = System.currentTimeMillis();
        ArrayList<ReviewDistanceTuple> tupleList = new ArrayList<>();
        for (MovieReview trainingReview : movieReviews) {
            double distance = computeDistance(testReview, trainingReview);
            ReviewDistanceTuple reviewDistanceTuple = new ReviewDistanceTuple(testReview, trainingReview, distance); // testReview = originReview
            tupleList.add(reviewDistanceTuple);
        }
        int posReviews = 0;
        int negReviews = 0;
        Collections.sort(tupleList);
        for (int i = 0; i < K; i++) {
            if (tupleList.get(i).getDestinationReview().getSentiment()) {
                posReviews++;
            } else {
                negReviews++;
            }
            i++;
        }
        // System.out.print("Current time ");
        //  System.out.println(System.currentTimeMillis() - startCDTime);
        return posReviews >= negReviews;
    }

    private double computeDistance(MovieReview testReview, MovieReview trainingReview) {
        double distance = 0;
        Iterator<String> testIt = testReview.getBagOfWordsIterator();
        double distanceSum = 0;
        while (testIt.hasNext()) {
            String word = testIt.next();
            double testFeatValue;
            double trainingFeatValue;
            if (useTfIdf) {
                testFeatValue = testReview.getTfIdf(word);
                trainingFeatValue = trainingReview.getTfIdf(word);
            } else {
                testFeatValue = (int) testReview.getTermFreq(word);
                trainingFeatValue = (int) trainingReview.getTermFreq(word);
            }
            if (distanceMetric.equals(EUCLIDEAN)) {
                distanceSum += Math.pow((testFeatValue - trainingFeatValue), 2);
            } else {
                distanceSum += Math.abs(testFeatValue - trainingFeatValue);
            }
        }
        if (distanceMetric.equals(EUCLIDEAN)) {
            distance = Math.sqrt(distanceSum);
        } else {
            distance = distanceSum;
        }
        return distance;
    }

    private double normalizeTfIdf(String word, double tfIdf) {
        Double tfIdfNorm = termToNormTfIdfMap.get(word);
        if (tfIdfNorm == null || tfIdfNorm == 0) {
            return tfIdf;
        } else {
            return tfIdf / tfIdfNorm;
        }
    }

    private double normalizeByMean(String word, double tfIdf) {
        Double tfIdfMean = tfIdfMeanMap.get(word);
        tfIdfMean = (tfIdfMean == null) ? 0 : tfIdfMean;
        Double variance = tfIdfVarianceMap.get(word);
        variance = (variance == null) ? 0 : variance;
        double standardDev = Math.sqrt(variance);
        if (standardDev == 0) {
            return tfIdf;
        } else {
            return (tfIdf - tfIdfMean) / (standardDev);
        }
    }


}
