import java.security.spec.ECField;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.jar.Pack200;

/**
 * Created by mpokr on 4/23/2017.
 */
public class RocchioClassifier {

    private List<MovieReview> movieReviews;
    private boolean useTfIdf;
    public static final String EUCLIDEAN = "EUCLIDEAN";
    public static final String MANHATTAN = "MANHATTAN";
    private String distanceMetric;
    private HashMap<String, Double> posCentroidVector;
    private HashMap<String, Double> negCentroidVector;

    public RocchioClassifier(List<MovieReview> movieReviews, boolean useTfIdf, String distanceMetric) {
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
    }

    public void train() {
        HashMap<String, Double> posCentroidVector = new HashMap<>();
        HashMap<String, Double> negCentroidVector = new HashMap<>();
        HashMap<String, Double> reviewCentroidVector;
        for (MovieReview review : movieReviews) {
            Iterator<String> wordIt = review.getBagOfWordsIterator();
            // Determines which hashmap to put the feature values of the review in (the pos or neg map).
            reviewCentroidVector = (review.getSentiment()) ? posCentroidVector : negCentroidVector;
            // Add to the pos and neg vector sums (will divide later to get centroid)
            while (wordIt.hasNext()) {
                String word = wordIt.next();
                Double currentWordSum = reviewCentroidVector.get(word);
                currentWordSum = (currentWordSum == null) ? 0 : currentWordSum;
                if (useTfIdf) {
                    reviewCentroidVector.put(word, currentWordSum + review.getTfIdf(word));
                } else {
                    reviewCentroidVector.put(word, currentWordSum + review.getTermFreq(word));
                }
            }
        }
        for (String word : posCentroidVector.keySet()) {
            posCentroidVector.put(word, posCentroidVector.get(word) / posCentroidVector.size());
        }
        for (String word : negCentroidVector.keySet()) {
            negCentroidVector.put(word, negCentroidVector.get(word) / negCentroidVector.size());
        }
        this.posCentroidVector = posCentroidVector;
        this.negCentroidVector = negCentroidVector;
    }

    public boolean classifyReview(MovieReview review) {
        double posDistance = 0;
        double negDistance = 0;
        Iterator<String> wordIt = review.getBagOfWordsIterator();
        while (wordIt.hasNext()) {
            String word = wordIt.next();
            double wordFeatVal;
            if (useTfIdf) {
                wordFeatVal = review.getTfIdf(word);
            } else {
                wordFeatVal = review.getTermFreq(word);
            }
            Double posCentroidVal = posCentroidVector.get(word);
            posCentroidVal = (posCentroidVal == null) ? 0 : posCentroidVal;
            Double negCentroidVal = negCentroidVector.get(word);
            negCentroidVal = (negCentroidVal == null) ? 0 : negCentroidVal;
            if (distanceMetric.equals(EUCLIDEAN)) {
                double posDiff = wordFeatVal - posCentroidVal;
                posDistance += (posDiff * posDiff);
                double negDiff = wordFeatVal - negCentroidVal;
                negDistance += (negDiff * negDiff);
            } else { // using Manhattan distance
                double posDiff = Math.abs(wordFeatVal - posCentroidVal);
                posDistance += posDiff;
                double negDiff = Math.abs(wordFeatVal - negCentroidVal);
                negDistance += negDiff;
            }
        }
        if (distanceMetric.equals(EUCLIDEAN)) {
            posDistance = Math.sqrt(posDistance);
            negDistance = Math.sqrt(negDistance);
        }
        if (posDistance == negDistance) {
            Random r = new Random();
            double randDouble = r.nextDouble();
            return (randDouble > 0.5);
        } else {
            return (posDistance < negDistance);
        }
    }
}
