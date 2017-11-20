package com.mikip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by mpokr on 3/15/2017.
 */
public class Perceptron {

    List<MovieReview> movieReviews;
    String[] vocabulary;
    HashMap<String, Double> weights;
    private double bias;
    private double learningRate = 0.15;
    private boolean useTfIdf = true;

    public Perceptron(List<MovieReview> movieReviews, String[] vocabulary, boolean useTfIdf, boolean initRandWeights, double learningRate) {
        this.movieReviews = movieReviews;
        weights = new HashMap<>(vocabulary.length);
        bias = 0;
        this.useTfIdf = useTfIdf;
        this.learningRate = learningRate;
        if (initRandWeights) {
            initRandomWeights(vocabulary);
        } else {
            initWeightsToZero(vocabulary);
        }
    }

    public void trainAll() {
        for (MovieReview review : movieReviews) {
            trainReview(review);
        }
    }

    private void trainReview(MovieReview review) {
        double sum = 0;
        HashMap<String, Integer> bagOfWords = review.getBagOfWords();
        HashMap<String, Double> tfIdfMap = review.getTfIdfMap();
        for (String word : bagOfWords.keySet()) {
            double weight = weights.get(word) == null ? 0 : weights.get(word);
            if (useTfIdf) {
                sum += tfIdfMap.get(word) * weight; // x_i * w_i
            } else {
                sum += bagOfWords.get(word) * weight; // x_i * w_i
            }

        }
        sum+= bias;
        int output = signum(sum);
        int sentimentValue = review.getSentiment() ? 1 : -1; // sentiment is 1 if pos, -1 if negative
        double error = sentimentValue - output;
        bias = bias + learningRate * error; // Update bias
        for (String word : bagOfWords.keySet()) {
            double currentWeight = weights.get(word);
            double updatedWeight = currentWeight + error * learningRate * tfIdfMap.get(word);
            weights.put(word, updatedWeight); // Update weights
        }

    }
    /**
    Initializes weights to random doubles between -1.0 and 1.0
     */
    private void initRandomWeights(String[] vocabulary) {
        Random r = new Random();
        for (int i = 0; i < vocabulary.length; i++) {
            double weight = r.nextDouble() * 2 - 1;
            weights.put(vocabulary[i], weight);
        }
    }

    private void initWeightsToZero(String[] vocabulary) {
        for (int i = 0; i < vocabulary.length; i++) {
            weights.put(vocabulary[i], 0.0);
        }
    }

    private int signum(double value) {
        int output = 0;
        if (value < 0) {
            output = -1;
        } else {
            output = 1;
        }
        return output;
    }

    public boolean classify(MovieReview review) {
        double sum = 0;
        HashMap<String, Integer> bagOfWords = review.getBagOfWords();
        HashMap<String, Double> tfIdfMap = review.getTfIdfMap();
        for (String word : bagOfWords.keySet()) {
            double weight = weights.get(word) == null ? 0 : weights.get(word);
            if (useTfIdf) {
                sum += tfIdfMap.get(word) * weight; // x_i * w_i
            } else {
                sum += bagOfWords.get(word) * weight; // x_i * w_i
            }
        }
        sum+= bias;
        int output = 0;
        if (sum < 0) {
            output = -1;
        } else if (sum > 0) {
            output = 1;
        }
        return output == 1;
    }


}
