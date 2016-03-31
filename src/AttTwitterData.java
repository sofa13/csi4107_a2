import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * Generates a little ARFF file with different attribute types.
 *
 * @author FracPete
 */
public class AttTwitterData {
	public static void main(String[] args) throws Exception {
		
		System.out.println("Running");
		SentiWordAnalyser sentiwords = new SentiWordAnalyser("sentiword.txt");

		FastVector atts;
		FastVector attVals;
		Instances instance;
		double[] vals;
		Set<String> stopWordsSet = new HashSet<String>();
		Set<String> positiveSet = new HashSet<String>();
		Set<String> negativeSet = new HashSet<String>();
		Map<String, Map<String, Integer>> bagOfWords = new HashMap<String, Map<String, Integer>>();
		HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
		HashMap<String, String> opinionMap = new HashMap<String, String>();
		HashMap<String, Double> posMap = new HashMap<String, Double>();
		HashMap<String, Double> negMap = new HashMap<String, Double>();
		HashMap<String, Double> objectivityMap = new HashMap<String, Double>();

		Vector<String> vocabVector = new Vector<String>();

		System.out.println("Build set of stopwords, positive words, and negative words");
		
		// Set of stopwords
		File stopWords = new File("./data/StopWords.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(stopWords))) {
			for (String line; (line = br.readLine()) != null;) {
				stopWordsSet.add(line);
			}
		}
		
		// Set of positive words
		File positive = new File("./data/Positive.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(positive))) {
			for (String line; (line = br.readLine()) != null;) {
				String w = line.toLowerCase();
				w = w.replaceAll("[^a-zA-z]", "");
				positiveSet.add(w);
			}
		}
		
		// Set of negative words
		File negative = new File("./data/Negative.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(negative))) {
			for (String line; (line = br.readLine()) != null;) {
				String w = line.toLowerCase();
				w = w.replaceAll("[^a-zA-z]", "");
				negativeSet.add(w);
			}
		}
		
		System.out.println("Build vocabulary");

		// Build vocabulary, removing stopwords, and punctuation
		// Extract features: exclamation mark, question mark, emoticons
		File file = new File("./data/semeval_twitter_data.txt");
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				@SuppressWarnings("resource")
				Scanner s = new Scanner(line).useDelimiter("\\t");
				s.next();
				String tweetID = s.next();
				String opinion = s.next().replace("\"", "");
				String sentence = s.next();

				opinionMap.put(tweetID, opinion);
				
				SentiWordAnalyser.SentiWord sentenceSentiment = sentiwords.analyzeSentence(sentence);
				posMap.put(tweetID, sentenceSentiment.getPositive());
				negMap.put(tweetID, sentenceSentiment.getNegative());
				objectivityMap.put(tweetID, sentenceSentiment.getObjective());
			
				String[] words = sentence.split(" ");

				for (String word : words) {
					String wordCompare = word.replaceAll("[^a-zA-z]", "");
					wordCompare = wordCompare.toLowerCase();
					if (!stopWordsSet.contains(wordCompare)
							&& !wordCompare.isEmpty()) {
						Integer count = vocabulary.get(wordCompare);
						if (count == null) {
							vocabulary.put(wordCompare, new Integer(1));
						} else {
							count++;
							vocabulary.put(wordCompare, count);
						}
					}

				}
			}
		}
		
		System.out.println("Remove rare words");
		
		// Remove rare words from vocabulary
		Iterator it = vocabulary.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry w = (HashMap.Entry) it.next();
			if ((int) w.getValue() < 5) {
				it.remove();
			} else {
				vocabVector.add((String) w.getKey());
			}
		}

		System.out.println("Build bag of words");
		
		// Build bag of words for each tweet { tweetID: { word : {count} }}
		// Extract features: number of positive and negative words
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				@SuppressWarnings("resource")
				Scanner s = new Scanner(line).useDelimiter("\\t");
				s.next();
				String tweetID = s.next();
				String opinion = s.next().replace("\"", "");
				String sentence = s.next();

				String[] words = sentence.split(" ");
				HashMap<String, Integer> wordsMap = new HashMap<String, Integer>();

				for (String word : words) {
					String wordCompare = word.replaceAll("[^a-zA-z]", "");
					wordCompare = wordCompare.toLowerCase();
					if (vocabulary.containsKey(wordCompare)) {
						Integer count = wordsMap.get(wordCompare);
						if (count == null) {
							wordsMap.put(wordCompare, new Integer(1));
						} else {
							count++;
							wordsMap.put(wordCompare, count);
						}
					}
				}

				bagOfWords.put(tweetID, wordsMap);
			}
		}
		
		System.out.println("Generate arff file");
		
		// Generates a ARFF file with different attribute types.

		// 1. set up attributes
		atts = new FastVector();

		// - nominal
		attVals = new FastVector();
		attVals.addElement("positive");
		attVals.addElement("negative");
		attVals.addElement("neutral");
		attVals.addElement("objective");
		atts.addElement(new Attribute("OpinionCategory", attVals));

		// - numeric
		atts.addElement(new Attribute("Positivity"));
		// - numeric
		atts.addElement(new Attribute("Negativity"));
		
		atts.addElement(new Attribute("Objectivity"));
		
		// - numeric
		for (String word : vocabVector) {
			atts.addElement(new Attribute(word));
		}

		// 2. create Instances object
		instance = new Instances("Opinion", atts, 0);

		// 3. fill with data
		for (String key : bagOfWords.keySet()) {
			vals = new double[instance.numAttributes()];
			int index = 0;
			
			Map<String, Integer> sentence = bagOfWords.get(key);
			
			// set nominal opinion attribute
			vals[index] = attVals.indexOf(opinionMap.get(key));
			index++;
			
			// set numerical # positive words attribute
			vals[index] = posMap.get(key);
			index++;
			
			// set numerical # negative words attribute
			vals[index] = negMap.get(key);
			index++;

			vals[index] = objectivityMap.get(key);
			index++;
			
			// set numerical word attributes
			for (int i = index; i < vocabVector.size(); i++) {
				if (sentence.containsKey(vocabVector.get(i))) {
					vals[i] = sentence.get(vocabVector.get(i));
				} else {
					vals[i] = 0;
				}
			}

			instance.add(new Instance(1.0, vals));
		}

		System.out.println("Write to file");
		
		// Write to files

		ArffSaver saver = new ArffSaver();
		saver.setInstances(instance);
		saver.setFile(new File("./data/semeval_twitter_data.arff"));
		saver.writeBatch();
		
		System.out.println("Complete");
	}
	
	private static int countOccurences(String haystack, char needle) {
		int counter = 0;
		for (int i = 0; i < haystack.length(); i++ ) {
		    if (haystack.charAt(i) == needle) {
		        counter++;
		    }
		}
		return counter;
	}
}