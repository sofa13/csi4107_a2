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
		FastVector attEm;
		Instances instance;
		double[] vals;
		Set<String> stopWordsSet = new HashSet<String>();
		Set<String> positiveSet = new HashSet<String>();
		Set<String> negativeSet = new HashSet<String>();
		Map<String, Map<String, Integer>> bagOfWords = new HashMap<String, Map<String, Integer>>();
		HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
		HashMap<String, String> opinionMap = new HashMap<String, String>();
		HashMap<String, Boolean> exclamationMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> questionMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> emoticonMap = new HashMap<String, Boolean>();
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
				exclamationMap.put(tweetID, sentence.contains("!"));
				questionMap.put(tweetID, sentence.contains("?"));
				emoticonMap.put(tweetID, sentence.contains(":)") || sentence.contains("(:") || sentence.contains("(;")|| sentence.contains(";)"));
				
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
			if ((int) w.getValue() < 10) {
				it.remove();
			} else {
				vocabVector.add((String) w.getKey());
			}
		}

		System.out.println("Build bag of words");
		
		// Build bag of words for each tweet { tweetID: { word : {count} }}
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
						
						if(positiveSet.contains(wordCompare))
						{
							Integer countPos = posMap.get(tweetID);
							if(countPos == null) countPos = 1;
							else countPos++;
							
							posMap.put(tweetID,countPos);
						}
						
						if(negativeSet.contains(wordCompare))
						{
							Integer countNeg = negMap.get(tweetID);
							if(countNeg == null) countNeg = 1;
							else countNeg++;
							
							negMap.put(tweetID,countNeg);
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
		attEm = new FastVector();
		attEm.addElement("Y");
		attEm.addElement("N");
		atts.addElement(new Attribute("PositiveEmiticon", attEm));
		
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
		for (String key : bagOfWords.keySet()) {
			vals = new double[instance.numAttributes()];
			
			Map<String, Integer> sentence = bagOfWords.get(key);
			
			// set nominal opinion attribute
			vals[0] = attVals.indexOf(opinionMap.get(key));
			
			// set nominal exclamation mark attribute
			if (exclamationMap.get(key)) {
				vals[1] = attEx.indexOf("Y");
			} else {
				vals[1] = attEx.indexOf("N");
			}

			// set nominal question mark attribute
			if (questionMap.get(key)) {
				vals[2] = attQu.indexOf("Y");
			} else {
				vals[2] = attQu.indexOf("N");
			}

			// set nominal emoticon attribute
			if (emoticonMap.get(key)) {
				vals[3] = attEm.indexOf("Y");
			} else {
				vals[3] = attEm.indexOf("N");
			}
			
			// set numerical # positive words attribute
			if(posMap.get(key) == null)
				vals[4] = 0;
			else
				vals[4] = posMap.get(key);
			
			// set numerical # negative words attribute
			if(negMap.get(key) == null)
				vals[5] = 0;
			else
				vals[5] = negMap.get(key);
			
			// set numerical word attributes
			int x = 6; // set as one more than above
			for (int i = x; i < vocabVector.size(); i++) {
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

//		PrintWriter writer = new PrintWriter(
//				"./data/semeval_twitter_data_arff.txt", "UTF-8");
//		writer.println(instance);
//		writer.close();

		ArffSaver saver = new ArffSaver();
		saver.setInstances(instance);
		saver.setFile(new File("./data/semeval_twitter_data.arff"));
		saver.writeBatch();
		
		System.out.println("Complete");
	}
}