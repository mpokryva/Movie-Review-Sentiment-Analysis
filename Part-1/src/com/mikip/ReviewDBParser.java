package com.mikip;

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
    private Perceptron perceptron;
    private NaiveBayes naiveBayes;
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
            for (String word : review.getBagOfWords().keySet()) {
                int termFrequencyInReview = review.getBagOfWords().get(word);
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
            for (String word : review.getBagOfWords().keySet()) {
                if (!vocabulary.contains(word)) {
                    vocabulary.add(word);
                }
            }
        }
        return vocabulary.toArray(new String[vocabulary.size()]);
    }

    public void initPerceptron(List<MovieReview> movieReviews, boolean useTfIdf, boolean initRandPerceptronWeights, double learningRate) {
        String[] vocabulary = getVocabulary(movieReviews);
        perceptron = new Perceptron(movieReviews, vocabulary, useTfIdf, initRandPerceptronWeights, learningRate);
    }

    public void initNaiveBayes(List<MovieReview> movieReviews, boolean useTfIdf) {
        HashMap<String, Integer> termInReviewFreqMap = new HashMap<>();
        for (MovieReview review : movieReviews) {
            for (String word : review.getBagOfWords().keySet()) {
                if (termInReviewFreqMap.get(word) == null) {
                    termInReviewFreqMap.put(word, 1);
                } else {
                    termInReviewFreqMap.put(word, termInReviewFreqMap.get(word) + 1);
                }
            }
        }
        naiveBayes = new NaiveBayes(movieReviews, termInReviewFreqMap.size(), termInReviewFreqMap, useTfIdf);
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
                double tf = (double) review.getTfLogNormMap().get(word);
                //double weightedIdf = smoothIdfMap.get(word);
                //double tfIdf = tf * weightedIdf;
                double tfIdf = tf * termToIdfMap.get(word);
                review.setTfIdf(word, tfIdf);
            }
            //Length norm the tfidf
            /*
            for (String word : review.getTfLogNormMap().keySet()) {
                Collection<Double> tfIdfValues = review.getTfIdfMap().values();

                Double[] tfIdfArr = tfIdfValues.toArray(new Double[tfIdfValues.size()]);
                double tfIdfSumSquared = 0;
                for (int i = 0; i < tfIdfArr.length; i++) {
                    tfIdfSumSquared += (tfIdfArr[i] * tfIdfArr[i]);
                }
                double tfIdfNorm = review.getTfIdfMap().get(word) / Math.sqrt(tfIdfSumSquared);
                review.getTfIdfMap().put(word, tfIdfNorm);
            }
            */
        }
    }

    public void shuffleReviews() {
        Collections.shuffle(movieReviews);
    }

    public HashMap<List<MovieReview>, List<MovieReview>> getTrainingToTestingMap() {
        return trainingToTestingMap;
    }

    public Perceptron getPerceptron() {
        return perceptron;
    }

    public NaiveBayes getNaiveBayes() {
        return naiveBayes;
    }

    public List<MovieReview> getMovieReviews() {
        return movieReviews;
    }

    public static void main(String[] args) {
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
        for (List<MovieReview> trainingList : dbParser.trainingToTestingMap.keySet()) {
            dbParser.initPerceptron(trainingList, true, true, 0.15);
            dbParser.perceptron.trainAll();
            List<MovieReview> testingList = dbParser.trainingToTestingMap.get(trainingList);
            for (MovieReview review : testingList) {
                boolean classification = dbParser.perceptron.classify(review);
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
        System.out.println("Perceptron: " + reviewsCorrect + " / " + dbParser.movieReviews.size() + " correct.");
        double precision = (double) truePos / (truePos + falsePos);
        double recall = (double) truePos / (truePos + falseNeg);
        double accuracy = (double) (truePos + trueNeg) / (truePos + trueNeg + falsePos + falseNeg);
        System.out.println("Perceptron accuracy: " + accuracy);
        System.out.println("Perceptron precision: " + precision);
        System.out.println("Perceptron recall: " + recall);
        System.out.println();
        System.out.println("-------------------------");
        System.out.println();
        reviewsCorrect = 0;
        falsePos = 0;
        falseNeg = 0;
        truePos = 0;
        trueNeg = 0;
        for (List<MovieReview> trainingList : dbParser.trainingToTestingMap.keySet()) {
            dbParser.initNaiveBayes(trainingList, true);
            List<MovieReview> testingList = dbParser.trainingToTestingMap.get(trainingList);
            for (MovieReview review : testingList) {
                boolean classification = dbParser.naiveBayes.trainReview(review);
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

        precision = (double)truePos / (truePos + falsePos);
        recall = (double) truePos / (truePos + falseNeg);
        accuracy = (double) (truePos + trueNeg) / (truePos + trueNeg + falsePos + falseNeg);
        System.out.println("Naive Bayes: " + reviewsCorrect + " / " + dbParser.movieReviews.size() + " correct.");
        System.out.println("Naive Bayes accuracy: " + accuracy);
        System.out.println("Naive Bayes precision: " + precision);
        System.out.println("Naive Bayes recall: " + recall);
    }
}
