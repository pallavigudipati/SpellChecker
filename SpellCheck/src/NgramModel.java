import java.io.*;
import java.util.*;

import edu.stanford.nlp.util.StringUtils;

public class NgramModel {
    public static int BIGRAM_COUNT = 286758206;
    public static int TRIGRAM_COUNT = 128125547;
    public static int QUADGRAM_COUNT = 46030908;
    public static int PENTAGRAM_COUNT = 16451820;
    public static int[] TOTAL_COUNT = { BIGRAM_COUNT, TRIGRAM_COUNT,
            QUADGRAM_COUNT, PENTAGRAM_COUNT };
    public ArrayList<ConfusionSet> confusionSetList = new ArrayList<ConfusionSet>();
    public HashMap<String, ConfusionSet> confusionReverseIndex = new HashMap<String, ConfusionSet>();
    public HashMap<String, Double> nGramCounts = new HashMap<String, Double>();

    public NgramModel() {
        this.loadFiles("confusion_sets.csv");
        this.populateIndex();
        Utils.addNGramCounts("ngram-counts/w2_.txt", nGramCounts);
        Utils.addNGramCounts("ngram-counts/w3_.txt", nGramCounts);
        Utils.addNGramCounts("ngram-counts/w4_.txt", nGramCounts);
        Utils.addNGramCounts("ngram-counts/w5_.txt", nGramCounts);
    }

    protected void loadFiles(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                // Create a new confusion set
                ConfusionSet confusionSet = new ConfusionSet();
                for (String part : parts) {
                    confusionSet.candidates.add(part);
                }
                confusionSetList.add(confusionSet);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Not able to read files");
        }
    }

    public void populateIndex() {
        for (ConfusionSet confusionSet : confusionSetList) {
            for (String confusionWord : confusionSet.candidates) {
                confusionReverseIndex.put(confusionWord, confusionSet);
            }
        }
    }

    public ArrayList<Integer> generateConfusionIndices(String phrase) {
        ArrayList<Integer> indiceList = new ArrayList<Integer>();
        String[] words = phrase.split(" ");
        int counter = 0;
        for (String word : words) {
            if (confusionReverseIndex.get(word) != null) {
                indiceList.add(counter);
            }
            counter += 1;
        }
        return indiceList;
    }

    public ArrayList<String> generateCandidatePhrases(String phrase) {
        ArrayList<String> phraseSet = new ArrayList<String>();
        phraseSet.add(phrase);
        String[] words = phrase.split(" ");
        int counter = 0;
        for (String word : words) {
            if (confusionReverseIndex.get(word) != null) {
                ArrayList<String> candidates = confusionReverseIndex.get(word).candidates;
                ArrayList<String> newphraseSet = new ArrayList<String>();
                for (String candidate : candidates) {
                    for (String oldphrase : phraseSet) {
                        String[] oldphrasewords = oldphrase.split(" ");
                        oldphrasewords[counter] = candidate;
                        String newphrase = StringUtils.join(oldphrasewords, " ");
                        newphraseSet.add(newphrase);
                    }
                }
                phraseSet = newphraseSet;
            }
            counter += 1;
        }
        return phraseSet;
    }

    public double getCount(String ngram) {
        Double ngramCount = nGramCounts.get(ngram);
        if (ngramCount == null) {
            return 0;
        }
        return ngramCount;
    }
    public static void main(String[] args) {
        String test = "1 2 3 4 5 6";
       /*
        System.out.println(generateWeight(test.split(" "), 1));
        System.out.println(generateWeight(test.split(" "), 2));
        System.out.println(generateWeight(test.split(" "), 3));
        System.out.println(generateWeight(test.split(" "), 4));
        System.out.println(generateWeight(test.split(" "), 5));
        System.out.println(generateWeight(test.split(" "), 7));*/
    }

    public double generateWeight(String[] phrase, int maxN) {
        double probability = 0.0;
        double denominator = 0.0;
        for (int N = 2; N <= maxN; ++N) {
            for (int i = 0; i < phrase.length - N + 1; ++i) {
                probability += Math.log(1 + getCount(Utils.concat(phrase, i, i + N)));
                denominator += -Math.log(TOTAL_COUNT[N - 2]);
            }
        }
        return probability + denominator;
    }
    /*
    public double generateWeight(String[] phrase, int confusionIndex) {
        double weight = 0;
        double denominator = 0;
        int sizeOfPhrase = phrase.length;
        String confusionWord = phrase[confusionIndex];
        List<String> leftGrams = new ArrayList<String>(); // small to big
        String leftString = "";
        int countCounter = 0;
        for (int i = confusionIndex - 1; i >= 0 && i >= confusionIndex - 4; --i) {
            leftString = phrase[i] + " " + leftString;
            leftGrams.add(leftString);
            denominator += Math.log(TOTAL_COUNT[countCounter]);
            countCounter++;
        }
        List<String> rightGrams = new ArrayList<String>();
        String rightString = phrase[confusionIndex];
        // String rightString = "";
        countCounter = 0;
        for (int i = confusionIndex + 1; i < sizeOfPhrase
                && i <= confusionIndex + 4; ++i) {
            rightString = rightString + " " + phrase[i];
            rightGrams.add(rightString);
            weight += Math.log(1 + getCount(rightString));
            denominator += Math.log(TOTAL_COUNT[countCounter]);
            countCounter++;
        }
        for (int i = 0; i < leftGrams.size(); ++i) {
            for (int j = 0; j < rightGrams.size(); ++j) {
                if (i + j + 3 > 5) {
                    break;
                }
                // mergedGrams.add(leftGrams.get(i) + " " + rightGrams.get(j));
                // TODO:
                weight += Math.log(1 + getCount(leftGrams.get(i) + " "
                        + rightGrams.get(j)));
                denominator += Math.log(TOTAL_COUNT[i + j + 1]);
            }
            // mergedGrams.add(leftGrams.get(i) + " " + confusionWord);
           // System.out.println(leftGrams.get(i) + " " + confusionWord); // Write
                                                                        // a
                                                                        // wrapper
            weight += Math.log(1 + getCount(leftGrams.get(i) + " "
                    + confusionWord));
        }
        // mergedGrams.addAll(rightGrams);
        // System.out.println(mergedGrams);
        return weight - denominator;
    }*/

    /*
    public ArrayList<String> generateNGrams(String[] phrase, int confusionIndex) {
        ArrayList<String> nGramList = new ArrayList<String>();
        // double weight = 0;
        // double denominator = 0;
        int sizeOfPhrase = phrase.length;
        String confusionWord = phrase[confusionIndex];
        List<String> leftGrams = new ArrayList<String>(); // small to big
        String leftString = "";
        int countCounter = 0;
        for (int i = confusionIndex - 1; i >= 0 && i >= confusionIndex - 4; --i) {
            leftString = phrase[i] + " " + leftString;
            leftGrams.add(leftString);
            // denominator += Math.log(TOTAL_COUNT[countCounter]);
            countCounter++;
        }
        List<String> rightGrams = new ArrayList<String>();
        String rightString = phrase[confusionIndex];
        // String rightString = "";
        countCounter = 0;
        for (int i = confusionIndex + 1; i < sizeOfPhrase
                && i <= confusionIndex + 4; ++i) {
            rightString = rightString + " " + phrase[i];
            rightGrams.add(rightString);
            // weight += Math.log(1 + getCount(rightString));
            nGramList.add(rightString);
            // denominator += Math.log(TOTAL_COUNT[countCounter]);
            countCounter++;
        }
        for (int i = 0; i < leftGrams.size(); ++i) {
            for (int j = 0; j < rightGrams.size(); ++j) {
                if (i + j + 3 > 5) {
                    break;
                }
                // mergedGrams.add(leftGrams.get(i) + " " + rightGrams.get(j));
                // TODO:
                // weight += Math.log(1 + getCount(leftGrams.get(i) + " " +
                // rightGrams.get(j)));
                nGramList.add(leftGrams.get(i) + " " + rightGrams.get(j));
                // denominator += Math.log(TOTAL_COUNT[i + j + 1]);
            }
            // mergedGrams.add(leftGrams.get(i) + " " + confusionWord);
            // System.out.println(leftGrams.get(i) + " " + confusionWord);
            // //Write a wrapper
            // weight += Math.log(1 + getCount(leftGrams.get(i) + " " +
            // confusionWord));
            nGramList.add(leftGrams.get(i) + " " + confusionWord);
        }
        // System.out.println(weight - denominator);
        // System.out.println(weight);
        return nGramList;
    }*/

    /*
    public double weighNGrams(ArrayList<String> ngrams) {
        double weight = 0.0;
        double denominator = 0.0;
        for (String ngram : ngrams) {
            weight += Math.log(1 + getCount(ngram));
            String[] words = ngram.split(" ");
            if (words.length > 5) {
                continue;
            }
            denominator += Math.log(TOTAL_COUNT[words.length - 2]);
        }
        return weight - denominator;
    }
    */
    // This function generates the substituted phrases given the phrase and typoPositions.
    public static List<List<Object>> generateSubstitutedPhrases(String phrase,
            List<List<List<Object>>> candidatesSequence, List<Integer> typoPositions) {
        List<List<Object>> phraseSet = new ArrayList<List<Object>>();
        List<Object> zerothPhrase = new ArrayList<Object>();
        zerothPhrase.add(phrase);
        zerothPhrase.add(1.0);
        phraseSet.add(zerothPhrase);
        int counter = 0;
        for (Integer typoPosition : typoPositions) {
            List<List<Object>> newPhraseSet = new ArrayList<List<Object>>();
            List<List<Object>> candidateSequence = candidatesSequence.get(counter);
            for (List<Object> phraseCandidate : phraseSet) {
                String[] words = phraseCandidate.get(0).toString().split(" ");
                double currentScore = (Double) phraseCandidate.get(1);
                for (List<Object> candidate : candidateSequence) {
                    words[typoPosition] = (String) candidate.get(0);
                    double updatedScore = currentScore * (Double) candidate.get(1);
                    String updatedPhraseString = StringUtils.join(words, " ");
                    List<Object> newPhraseCandidate = new ArrayList<Object>();
                    newPhraseCandidate.add(updatedPhraseString);
                    newPhraseCandidate.add(updatedScore);
                    newPhraseSet.add(newPhraseCandidate);
                    // System.out.println(newPhraseCandidate);
                }
            }
            counter += 1;
            phraseSet = newPhraseSet;
        }
        return phraseSet;
    }

    /*
    // TODO : remove it
    public void spellCheckPhrase(String phrase) {
        ArrayList<String> candidatePhrases = generateCandidatePhrases(phrase);
        ArrayList<Integer> confusionIndices = generateConfusionIndices(phrase);
        for (String candidatePhrase : candidatePhrases) {
            HashMap<String, Integer> contextNGrams = new HashMap<String, Integer>();
            for (int confusionIndice : confusionIndices) {
                String[] words = candidatePhrase.split(" ");
                ArrayList<String> nGramList = generateNGrams(words,
                        confusionIndice);
                for (String nGram : nGramList) {
                    contextNGrams.put(nGram, 1);
                }
            }
            ArrayList<String> nGramsUnion = new ArrayList<String>();
            for (String nGram : contextNGrams.keySet()) {
                nGramsUnion.add(nGram);
            }
            double weight = weighNGrams(nGramsUnion);
            // System.out.println(candidatePhrase + weight);
        }
    }
    */
}
