package com.mikip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mpokr on 3/16/2017.
 */
public class NaiveBayes {
    /**
     * Maps word to how many times it occurs in negative reviews
     */
    private HashMap<String, Integer> negTermFreqMap;
    /**
     * Maps word to how many times it occurs in positive reviews.
     */
    private HashMap<String, Integer> posTermFreqMap;
    /**
     * List of MovieReviews with a negative sentiment.
     */
    private List<MovieReview> negReviews;
    /**
     * List of MovieReviews with a positive sentiment.
     */
    private List<MovieReview> posReviews;
    /**
     * Count of total words in negative reviews
     */
    private int negTotalTerms;
    /**
     * Count of total words in positive reviews
     */
    private int posTotalTerms;
    /**
     * Count of total unique words in negative reviews
     */
    private int negUniqueTerms;
    /**
     * Count of total unique words in positive reviews
     */
    private int posUniqueTerms;
    /**
     * Sum of the tf-idfs of all words in negative reviews
     */
    private double negTfIdfSum;
    /**
     * Sum of the tf-idfs of all words in positive reviews.
     */
    private double posTfIdfSum;
    /**
     * Constant m, used for m-estimation
     */
    private final int M = 1;
    /**
     * Count of how many unique words there are in positive and negative reviews.
     */
    private int vocabSize;
    /**
     * Maps a word to its average tf-idf across all reviews that have the word.
     * Not currently used**
     */
    private HashMap<String, Double> meanTfIdfMap;
    /**
     * Maps a word to how many reviews it has appeared in.
     */
    private HashMap<String, Integer> termInReviewFreqMap;

    private boolean useTfIdf;

    /**
     * Initializes fields of the NaiveBayes object, and populates necessary fields for later training calculations.
     * @param movieReviews A list of movie reviews
     * @param vocabSize The size of the vocabulary (unique word count)
     * @param termsInReviewFreqMap HashMap that maps a word to how many reviews it appears in.
     */
    public NaiveBayes(List<MovieReview> movieReviews, int vocabSize, HashMap<String, Integer> termsInReviewFreqMap, boolean useTfIdf) {
        negTermFreqMap = new HashMap<>();
        posTermFreqMap = new HashMap<>();
        negReviews = new ArrayList<>();
        posReviews = new ArrayList<>();
        meanTfIdfMap = new HashMap<>();
        this.useTfIdf = useTfIdf;
        this.termInReviewFreqMap = termsInReviewFreqMap;
        initReviewTermFreqMaps(movieReviews);
        for (MovieReview movieReview : negReviews) {
            for (String wordInBag : movieReview.getBagOfWords().keySet()) {
                negTfIdfSum += movieReview.getTfIdfMap().get(wordInBag);
            }
        }
        for (MovieReview movieReview : posReviews) {
            for (String wordInBag : movieReview.getBagOfWords().keySet()) {
                posTfIdfSum += movieReview.getTfIdfMap().get(wordInBag);
            }
        }
        this.vocabSize = vocabSize;
        calculateMeanTfIdfs();
        int k = 1;
    }


    /**
     * Fills up the negative and positive review frequency maps. Only used in the constructor.
     * @param movieReviews The list of movie reviews.
     */
    private void initReviewTermFreqMaps(List<MovieReview> movieReviews) {
        for (MovieReview review : movieReviews) {
            if (review.getSentiment()) {
                posReviews.add(review);  // Add  positive review to list of reviews
            } else {
                negReviews.add(review); // Add negative review to list of reviews.
            }
            for (String word : review.getBagOfWords().keySet()) {
                int freq = review.getBagOfWords().get(word); // Freq of this word in this review
                if (review.getSentiment()) {
                    // Put into positive frequency map, since review is positive.
                    if (posTermFreqMap.get(word) == null) {
                        posTermFreqMap.put(word, freq);
                        posUniqueTerms++; // Word never encountered before. Add to unique word count.
                    } else {
                        posTermFreqMap.put(word, posTermFreqMap.get(word) + freq);
                    }
                    posTotalTerms++; // Add to total word count.
                } else {
                    // Put into negative frequency map, since review is negative.
                    if (negTermFreqMap.get(word) == null) {
                        negTermFreqMap.put(word, freq);
                        negUniqueTerms++; // Word never encountered before. Add to unique word count.
                    } else {
                        negTermFreqMap.put(word, negTermFreqMap.get(word) + freq);
                    }
                    negTotalTerms++; // Add to total word count.
                }
            }
        }
    }


    public void trainAll() {
        for (MovieReview negReview : negReviews) {
            trainReview(negReview);
        }
        for (MovieReview posReview : posReviews) {
            trainReview(posReview);
        }
    }
    /**
     * Trains on a single review, and returns the predicted sentiment
     *
     * @param review The review to train on.
     * @return True if positive sentiment, false if negative.
     */
    public boolean trainReview(MovieReview review) {
        double posProb = 1;
        double negProb = 1;
        for (String word : review.getBagOfWords().keySet()) {
            double[] trainedProb;
            if (useTfIdf) {
                trainedProb = trainWordTfIdf(review, word);
            } else {
                trainedProb = trainWord(review, word);
            }

            posProb += trainedProb[0];
            negProb += trainedProb[1];
        }
        return posProb >= negProb;
    }

    /**
     * Trains on a single word from a single review, using relative frequencies, and returns an array containing the probability of the word being
     * in the positive class, and the probability of the word being in the negative class.
     *
     * @param review The review to train on.
     * @param word   The word from the specified review to train on.
     * @return An array with two doubles. The first value is the probability that the word belongs to the positive class, the
     * second double is the probability that the word belongs the negative class.
     */
    private double[] trainWord(MovieReview review, String word) {
        double posFreq = posTermFreqMap.get(word) == null ? 0 : (double) posTermFreqMap.get(word);
        double negFreq = negTermFreqMap.get(word) == null ? 0 : (double) negTermFreqMap.get(word);
        double testPos = (posFreq + 1) / (posTotalTerms + vocabSize);
        double testNeg = (negFreq + 1) / (negTotalTerms + vocabSize);
        if (posFreq == 0) {
            testPos = M * (1 / (double) vocabSize);
        }
        if (negFreq == 0) {
            testNeg = M * (1 / (double) vocabSize);
        }
        return new double[]{Math.log(testPos), Math.log(testNeg)};
    }

    /**
     * Trains on a single word from a single review, using tf-idfs, and returns an array containing the probability of the word being
     * in the positive class, and the probability of the word being in the negative class.
     *
     * @param review The review to train on.
     * @param word   The word from the specified review to train on.
     * @return An array with two doubles. The first value is the probability that the word belongs to the positive class, the
     * second double is the probability that the word belongs the negative class.
     */
    private double[] trainWordTfIdf(MovieReview review, String word) {
        double posFreq = posTermFreqMap.get(word) == null ? 0 : (double) posTermFreqMap.get(word);
        double negFreq = negTermFreqMap.get(word) == null ? 0 : (double) negTermFreqMap.get(word);
        double tfIdf = review.getTfIdfMap().get(word);
        //double meanTfIdfSum = meanTfIdfMap.get(word) * vocabSize;
        double testPos = (posFreq * tfIdf + 1) / (posTfIdfSum + vocabSize);//((posFreq * tfIdf) + 1) / (posTfIdfSum + meanTfIdfSum);
        double testNeg = (negFreq * tfIdf + 1) / (negTfIdfSum + vocabSize);//((negFreq * tfIdf) + 1) / (negTfIdfSum + meanTfIdfSum);
        if (posFreq == 0) {
            testPos =  (M * (1/ (double) vocabSize));
        }
        if (negFreq == 0) {
            testNeg = (M * (1 / (double) vocabSize));
        }
        return new double[]{Math.log(testPos), Math.log(testNeg)};
    }

    /**
     * Calculates the mean tdf-idf for each word in the vocabulary, and populates the meanTfIdfMap with these values.
     * Only used in the constructor.
     */
    private void calculateMeanTfIdfs() {
        for (MovieReview review : negReviews) {
            // Get the sum of the tfidfs for all words in negative reviews
            for (String word : review.getBagOfWords().keySet()) {
                if (meanTfIdfMap.get(word) == null) {
                    meanTfIdfMap.put(word, review.getTfIdfMap().get(word));
                } else {
                    meanTfIdfMap.put(word, meanTfIdfMap.get(word) + review.getTfIdfMap().get(word));
                }
            }
        }
        for (MovieReview review : posReviews) {
            // Get the sum of the tfidfs for all words in positive reviews
            for (String word : review.getBagOfWords().keySet()) {
                if (meanTfIdfMap.get(word) == null) {
                    meanTfIdfMap.put(word, review.getTfIdfMap().get(word));
                } else {
                    meanTfIdfMap.put(word, meanTfIdfMap.get(word) + review.getTfIdfMap().get(word));
                }
            }
        }
        // Find and put mean by dividing tfidf sum of each word by the amount of reviews it has appeared in.
        for (String word : meanTfIdfMap.keySet()) {
            double meanTfIdf = meanTfIdfMap.get(word) / termInReviewFreqMap.get(word);
            meanTfIdfMap.put(word, meanTfIdf);
        }
    }


}
