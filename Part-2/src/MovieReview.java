import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class MovieReview {
    /**
     * Maps a word to how many times it appears in this review (tf)
     */
    private HashMap<String, Integer> bagOfWords;

    private HashMap<String, Double> tfLogNormMap;
    /**
     * Maps a word to its term frequency-inverse document frequency (tfidf)
     */
    private HashMap<String, Double> tfIdfMap;
    /**
     * Indicates whether this was a positive or negative review. True is positive. False is negative.
     */
    private boolean sentimentLabel;

    public MovieReview(File file, boolean label, boolean noPunctuation) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            bagOfWords = new HashMap<>();
            tfIdfMap = new HashMap<>();
            tfLogNormMap = new HashMap<>();
            String nextLine = br.readLine();
            while (nextLine != null) {
                // Get words in line
                String[] words = nextLine.split(" ");
                // Updating frequency word map
                Pattern p = null;
                if (noPunctuation) {
                    p = Pattern.compile("\\p{Punct}");
                }
                for (int i = 0; i < words.length; i++) {
                    if (p != null) {
                        if (!p.matcher(words[i]).matches()) {
                            if (bagOfWords.get(words[i]) == null) {
                                bagOfWords.put(words[i], 1);
                            } else {
                                bagOfWords.put(words[i], bagOfWords.get(words[i]) + 1);
                            }
                        }
                    } else {
                        if (bagOfWords.get(words[i]) == null) { // store all words
                            bagOfWords.put(words[i], 1);
                        } else {
                            bagOfWords.put(words[i], bagOfWords.get(words[i]) + 1);
                        }
                    }
                }
                for (String word : bagOfWords.keySet()) {
                    int tf = bagOfWords.get(word);
                    double tfWeighted = tf > 0 ? Math.log(tf) + 1 : 0;
                    tfLogNormMap.put(word, tfWeighted);
                }
                nextLine = br.readLine();
            }
            this.sentimentLabel = label; // Set sentimentLabel
            br.close();
        } catch (IOException e) {
            e.printStackTrace(); // For now
        }
    }



    public HashMap<String, Double> getTfLogNormMap() {
        return tfLogNormMap;
    }

    public void addTfIdf(String word, double tfIdf) {
        if (bagOfWords.get(word) == null) {
            System.out.println("Word not in review");
        } else {
            tfIdfMap.put(word, tfIdf);
        }
    }

    public Iterator<String> getBagOfWordsIterator() {
        return bagOfWords.keySet().iterator();
    }

    public int getTermFreq(String term) {
        Integer tf = bagOfWords.get(term);
        tf = (tf == null) ? 0 : tf;
        return tf;
    }
    public double getTfIdf(String term) {
        Double tfIdf = tfIdfMap.get(term);
        tfIdf = (tfIdf == null) ? 0 : tfIdf;
        return tfIdf;
    }

    public HashMap<String, Integer> getBagOfWords() {
        return bagOfWords;
    }



    public HashMap<String, Double> getTfIdfMap() {
        return tfIdfMap;
    }

    public boolean getSentiment() {
        return sentimentLabel;
    }


}
