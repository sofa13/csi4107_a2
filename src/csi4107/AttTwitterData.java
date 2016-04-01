package csi4107;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		FastVector atts;
		FastVector attVals;
		FastVector attEx;
		FastVector attQu;
		FastVector attHa;
		FastVector attHEM;
		FastVector attSEM;
		Instances instance;
		Set<String> stopWordsSet = new HashSet<String>();
		Set<String> positiveSet = new HashSet<String>();
		Set<String> negativeSet = new HashSet<String>();
		Set<String> positivePrefixes = new HashSet<String>();
		Set<String> negativePrefixes = new HashSet<String>();
		Map<String, Map<String, Integer>> bagOfWords = new HashMap<String, Map<String, Integer>>();
		HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
		HashMap<String, String> opinionMap = new HashMap<String, String>();
		HashMap<String, Boolean> exclamationMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> questionMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> hashtagMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> happyEmoteMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> sadEmoteMap = new HashMap<String, Boolean>();
		HashMap<String, Integer> posMap = new HashMap<String, Integer>();
		HashMap<String, Integer> negMap = new HashMap<String, Integer>();
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
				if (w.contains("*")) {
					positivePrefixes.add(w.replace("*", ""));
				} else {
					positiveSet.add(w);
				}
			}
		}
		
		// Set of negative words
		File negative = new File("./data/Negative.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(negative))) {
			for (String line; (line = br.readLine()) != null;) {
				String w = line.toLowerCase();
				if (w.contains("*")) {
					negativePrefixes.add(w.replace("*", ""));
				} else {
					negativeSet.add(w);
				}
			}
		}
		
		System.out.println("Build vocabulary");

		// Build vocabulary, removing stopwords, and punctuation
		// Extract features: exclamation mark, question mark, emoticons
		File file = new File("./data/semeval_twitter_data.txt");
		Pattern happyEmotes = Pattern.compile(".*(:\\)|;\\)|\\(:|\\(;|♥|♡|☺).*");
		Pattern sadEmotes = Pattern.compile(".*(:\\(|;\\(|\\):|\\);|\\>:\\||\\|:\\<|:@).*");

		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				@SuppressWarnings("resource")
				Scanner s = new Scanner(line).useDelimiter("\\t");
				s.next();
				String tweetID = s.next();
				String opinion = s.next().replace("\"", "");
				String sentence = s.next();

				opinionMap.put(tweetID, opinion);
				exclamationMap.put(tweetID, sentence.contains("!"));
				questionMap.put(tweetID, sentence.contains("?"));
				hashtagMap.put(tweetID, sentence.contains("#"));
				happyEmoteMap.put(tweetID, happyEmotes.matcher(sentence).matches());
				sadEmoteMap.put(tweetID, sadEmotes.matcher(sentence).matches());
				String[] words = sentence.split(" ");

				for (String word : words) {
					String wordCompare = word.replaceAll("[^a-zA-z]", "");
					wordCompare = wordCompare.toLowerCase();
					if (!stopWordsSet.contains(wordCompare)
							&& !wordCompare.isEmpty() && wordCompare.length()>2) {
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
			if ((int) w.getValue() < 3) {
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
				s.next();
				String sentence = s.next();

				String[] words = sentence.split(" ");
				HashMap<String, Integer> wordsMap = new HashMap<String, Integer>();

				int negativeCount = 0;
				int positiveCount = 0;
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

						if (positiveSet.contains(wordCompare)) {
							positiveCount += 1;
						} else {
							String current = wordCompare;
							if (positivePrefixes.stream().anyMatch(w -> current.startsWith(w))) {
								positiveCount += 1;
							}
						}
						
						if (negativeSet.contains(word)) {
							negativeCount += 1;
						} else {
							String current = wordCompare;
							if (negativePrefixes.stream().anyMatch(w -> current.startsWith(w))) {
								negativeCount += 1;
							}
						}
					}
				}
				if (negativeCount == 0) {
					positiveCount = positiveCount * 2 + 1;
				}
				posMap.put(tweetID, positiveCount);
				negMap.put(tweetID, negativeCount);
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
		
		// - nominal
		attEx = new FastVector();
		attEx.addElement("Y");
		attEx.addElement("N");
		atts.addElement(new Attribute("ExclamationMark", attEx));
		
		// - nominal
		attQu = new FastVector();
		attQu.addElement("Y");
		attQu.addElement("N");
		atts.addElement(new Attribute("QuestionMark", attQu));
		
		// - nominal
		attHa = new FastVector();
		attHa.addElement("Y");
		attHa.addElement("N");
		atts.addElement(new Attribute("HashTag", attHa));
		
		// - nominal
		attHEM = new FastVector();
		attHEM.addElement("Y");
		attHEM.addElement("N");
		attSEM = new FastVector();
		attSEM.addElement("Y");
		attSEM.addElement("N");
		atts.addElement(new Attribute("PositiveEmoticon", attHEM));
		atts.addElement(new Attribute("NegativeEmoticon", attSEM));
		
		// - numeric
		atts.addElement(new Attribute("PositiveWords"));
		
		// - numeric
		atts.addElement(new Attribute("NegativeWords"));
		
		// - numeric
		for (String word : vocabVector) {
			atts.addElement(new Attribute(word));
		}

		// 2. create Instances object
		instance = new Instances("Opinion", atts, 0);

		// 3. fill with data
		for (String tweetIDKey : bagOfWords.keySet()) {			
			Map<String, Integer> sentence = bagOfWords.get(tweetIDKey);
			
			List<Double> values = Arrays.asList(
					(double) attVals.indexOf(opinionMap.get(tweetIDKey)), // nominal opinion
					(double) attEx.indexOf(exclamationMap.get(tweetIDKey) ? "Y" : "N"), // set nominal exclamation mark
					(double) attQu.indexOf(questionMap.get(tweetIDKey) ? "Y" : "N"), // set nominal question mark
					(double) attHa.indexOf(hashtagMap.get(tweetIDKey) ? "Y" : "N"), // set nominal hashtag
					(double) attHEM.indexOf(happyEmoteMap.get(tweetIDKey) ? "Y" : "N"),
					(double) attSEM.indexOf(sadEmoteMap.get(tweetIDKey) ? "Y" : "N"),
					(double) posMap.get(tweetIDKey),
					(double) negMap.get(tweetIDKey)
			);

			List<Double> vocab = vocabVector.stream().map(key -> (double) (sentence.containsKey(key) ? sentence.get(key) : 0))
					.collect(Collectors.toList());
			List<Double> union = Stream.concat(values.stream(), vocab.stream()).collect(Collectors.toList());
			double[] valueArray = union.stream().mapToDouble(d -> d).toArray();
			instance.add(new Instance(1.0, valueArray));
		}

		System.out.println("Write to file");
		
		// Write to files

		ArffSaver saver = new ArffSaver();
		saver.setInstances(instance);
		saver.setFile(new File("data/semeval_twitter_data.arff"));
		saver.writeBatch();
		
		System.out.println("Complete");
	}
}