import java.io.File;
import java.util.*;

/**
 * Parses the whole database of movie reviews, and creates the ReivewBagOfWords objects
 */
public class ReviewDBParser {

    private List<MovieReview> movieReviews;
    /**
     * Maps a word to how many times it has appeared in the entire corpus.
     */
    private HashMap<String, Integer> termInCorpusFreqMap; // *** Not sure if actually useful ***
    /**
     * Maps a word to how many reviews it has appeared in.
     */
    private HashMap<String, Integer> termInReviewFreqMap;
    /**
     * Maps a word to its inverse document frequency (idf)
     */
    private HashMap<String, Double> termToIdfMap;
    /**
     * Maps a word to its WEIGHTED inverse document frequency (idf)
     */
    private HashMap<String, Double> smoothIdfMap;
    /**
     * How many movie reviews there are in total.
     */
    private int reviewCount;
    /**
     * Maps a training list of MovieReviews, to a testing list of MovieReviews.
     * Used for cross validation.
     */
    private HashMap<List<MovieReview>, List<MovieReview>> trainingToTestingMap;
    private KNearestClassifier KNearestClassifier;
    private RocchioClassifier rocchioClassifier;
    private static final String POSITIVE_DIR = "/pos";
    private static final String NEGATIVE_DIR = "/neg";
    private boolean ignorePunctuation;

    public ReviewDBParser(File parentFolder, boolean ignorePunctuation) {
        movieReviews = new ArrayList<>();
        this.ignorePunctuation = ignorePunctuation;
        // Parse review directories
        parseReviewDirectory(parentFolder, POSITIVE_DIR, ignorePunctuation);
        parseReviewDirectory(parentFolder, NEGATIVE_DIR, ignorePunctuation);
        reviewCount = movieReviews.size();

        /*
        Create corpus frequency and review frequency maps
         */
        termInCorpusFreqMap = new HashMap<>();
        termInReviewFreqMap = new HashMap<>();
        for (MovieReview review : movieReviews) {
            Iterator<String> it = review.getBagOfWordsIterator();
            while (it.hasNext()) {
                String word = it.next();
                int termFrequencyInReview = review.getTermFreq(word);
                if (termInCorpusFreqMap.get(word) == null) {
                    // Set frequency to frequency encountered
                    termInCorpusFreqMap.put(word, termFrequencyInReview);
                } else {
                    // Set frequency to previous frequency + frequency encountered
                    termInCorpusFreqMap.put(word, termInCorpusFreqMap.get(word) + termFrequencyInReview);
                }
                if (termInReviewFreqMap.get(word) == null) {
                    termInReviewFreqMap.put(word, 1);
                } else {
                    termInReviewFreqMap.put(word, termInReviewFreqMap.get(word) + 1);
                }
            }
        }
        /*
        Calculate inverse document frequency (idf)
         */
        termToIdfMap = new HashMap<>();
        smoothIdfMap = new HashMap<>();
        calculateIdf();
        calculateTfIdf();
        shuffleReviews();
        trainingToTestingMap = new HashMap<>();
        partitionReviews(5);
        for (MovieReview review : movieReviews) {
            if (review.getTfIdfMap().size() != review.getBagOfWords().size()) {
                int i = 3;
            }
        }
    }

    /**
     * Given a list of movie reviews, return an array that contains all unique words from this list.
     *
     * @param movieReviews List of movie reviews.
     * @return Array containg all unique words from the specified list of movie reviews (the vocabulary).
     */
    private String[] getVocabulary(List<MovieReview> movieReviews) {
        Set<String> vocabulary = new HashSet<>();
        for (MovieReview review : movieReviews) {
            Iterator<String> it = review.getBagOfWordsIterator();
            while (it.hasNext()) {
                String word = it.next();
                if (!vocabulary.contains(word)) {
                    vocabulary.add(word);
                }
            }
        }
        return vocabulary.toArray(new String[vocabulary.size()]);
    }

    public void initKNearest(int K, List<MovieReview> movieReviews, boolean useTfIdf, String distanceMetric) {
        KNearestClassifier = new KNearestClassifier(K, movieReviews, useTfIdf, distanceMetric);
    }

    public void initRocchio(List<MovieReview> movieReviews, boolean useTfIdf, String distanceMetric) {
        rocchioClassifier = new RocchioClassifier(movieReviews, useTfIdf, distanceMetric);
    }

    private void parseReviewDirectory(File parentFolder, String posOrNeg, boolean ignorePunctuation) {
        boolean sentiment = posOrNeg.equals(POSITIVE_DIR); // Sentiment = true if positive reviews. False otherwise.
        File reviewDir = new File(parentFolder + posOrNeg);
        File[] reviewArr = reviewDir.listFiles();
        // Create positive movie reviews
        if (reviewArr != null) {
            for (File file : reviewArr) {
                movieReviews.add(new MovieReview(file, sentiment, ignorePunctuation)); // True because sentiment is positive
            }
        }
    }

    /**
     * Partitions the list of movie revies into the specified amount of partitions for cross validation testing.
     *
     * @param partitions The amount of partitions to partition to.
     */
    private void partitionReviews(int partitions) {
        shuffleReviews();
        int partitionSize = movieReviews.size() / partitions; // 2000 / 5 = 400
        List<List<MovieReview>> initPartition = new ArrayList<>(partitions);
        for (int i = 0; i < partitions; i++) {
            int lowerIndex = i * partitionSize;
            int upperIndex = (i + 1) * partitionSize;
            initPartition.add(i, movieReviews.subList(lowerIndex, upperIndex));
        }
        for (int i = 0; i < partitions; i++) {
            List<List<MovieReview>> partitionToAdd = new ArrayList<>();
            for (int j = 0; j < partitions; j++) {
                partitionToAdd.add(initPartition.get(j));
            }
            List<MovieReview> testingList = partitionToAdd.remove(i);
            List<MovieReview> trainingList = new ArrayList<>();
            for (int k = 0; k < partitionToAdd.size(); k++) {
                trainingList.addAll(partitionToAdd.get(k)); // Fill training list with all reviews from the union of partitions - 1 lists.
            }
            trainingToTestingMap.put(trainingList, testingList);
        }
    }


    private void calculateIdf() {
        for (String word : termInCorpusFreqMap.keySet()) {
            int termInReviewFreq = termInReviewFreqMap.get(word);
            double quotient = (double) reviewCount / (1 + termInReviewFreq);
            double idf = Math.log(quotient);
            idf = idf < 0 ? 0 : idf; // Set idf to 0 if idf < 0
            termToIdfMap.put(word, idf);
            // Calculate weighted idf
            // Using "inverse document frequency smooth" weighting scheme
            double smoothDocWeight = Math.log(((double) reviewCount) / (1 + termInReviewFreqMap.get(word)));
            double weightedIdf = idf * smoothDocWeight;
            smoothIdfMap.put(word, weightedIdf);
        }
    }


    private void calculateTfIdf() {
        for (MovieReview review : movieReviews) {
            for (String word : review.getTfLogNormMap().keySet()) {
                double tf = review.getTfLogNormMap().get(word);
                double tfIdf = tf * termToIdfMap.get(word);
                review.addTfIdf(word, tfIdf);
            }
        }
    }


    public void shuffleReviews() {
        Collections.shuffle(movieReviews);
    }

    public HashMap<List<MovieReview>, List<MovieReview>> getTrainingToTestingMap() {
        return trainingToTestingMap;
    }

    public KNearestClassifier getKNN() {
        return KNearestClassifier;
    }

    public RocchioClassifier getRocchio() {
        return rocchioClassifier;
    }


    public List<MovieReview> getMovieReviews() {
        return movieReviews;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        String currentDir = System.getProperty("user.dir");
        ReviewDBParser dbParser;
        if (currentDir.contains("src")) {
            dbParser = new ReviewDBParser(new File("../txt_sentoken"), false);
        } else {
            dbParser = new ReviewDBParser(new File("./txt_sentoken"), false);
        }
        double reviewsCorrect = 0;
        double perceptronAccuracy = 0;
        int trainingCycles = 0;
        int falsePos = 0;
        int falseNeg = 0;
        int truePos = 0;
        int trueNeg = 0;
        int K = 34;
        boolean useTfIdf = true;
        /*
        for (List<MovieReview> trainingList : dbParser.trainingToTestingMap.keySet()) {
            dbParser.initKNearest(K, trainingList, useTfIdf, "MANHATTAN");
            List<MovieReview> testingList = dbParser.trainingToTestingMap.get(trainingList);
            for (MovieReview review : testingList) {
                boolean classification = dbParser.KNearestClassifier.classifyReview(review);
                if (classification == review.getSentiment()) {
                    reviewsCorrect++;
                    if (classification) {
                        truePos++;
                    } else {
                        trueNeg++;
                    }
                } else {
                    if (classification) {
                        falsePos++;
                    } else {
                        falseNeg++;
                    }
                }
            }
        }
        System.out.println("K: " + K);
        System.out.println("Use tfidf: " + useTfIdf);
        System.out.println("KNN: " + reviewsCorrect + " / " + dbParser.movieReviews.size() + " correct.");
        double precision = (double) truePos / (truePos + falsePos);
        double recall = (double) truePos / (truePos + falseNeg);
        double accuracy = (double) (truePos + trueNeg) / (truePos + trueNeg + falsePos + falseNeg);
        System.out.println("KNN accuracy: " + accuracy);
        System.out.println("KNN precision: " + precision);
        System.out.println("KNN recall: " + recall);
        System.out.println();
        System.out.println("-------------------------");
        System.out.println();
        reviewsCorrect = 0;
        falsePos = 0;
        falseNeg = 0;
        truePos = 0;
        trueNeg = 0;
        */
        for (List<MovieReview> trainingList : dbParser.trainingToTestingMap.keySet()) {
            dbParser.initRocchio(trainingList, useTfIdf, "EUCLIDEAN");
            dbParser.rocchioClassifier.train();
            List<MovieReview> testingList = dbParser.trainingToTestingMap.get(trainingList);
            for (MovieReview review : testingList) {
                boolean classification = dbParser.rocchioClassifier.classifyReview(review);
                if (classification == review.getSentiment()) {
                    reviewsCorrect++;
                    if (classification) {
                        truePos++;
                    } else {
                        trueNeg++;
                    }
                } else {
                    if (classification) {
                        falsePos++;
                    } else {
                        falseNeg++;
                    }
                }
            }
        }
        System.out.println("Use tfidf: " + useTfIdf);
        System.out.println("Rocchio: " + reviewsCorrect + " / " + dbParser.movieReviews.size() + " correct.");
        double precision = (double) truePos / (truePos + falsePos);
        double recall = (double) truePos / (truePos + falseNeg);
        double accuracy = (double) (truePos + trueNeg) / (truePos + trueNeg + falsePos + falseNeg);
        System.out.println("Rocchio accuracy: " + accuracy);
        System.out.println("Rocchio precision: " + precision);
        System.out.println("Rocchio recall: " + recall);
        System.out.println();
        System.out.println("-------------------------");
        System.out.print(System.currentTimeMillis() - start + " ms");

    }
}
