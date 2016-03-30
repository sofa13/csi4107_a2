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
		Map<String, Map<String, Integer>> bagOfWords = new HashMap<String, Map<String, Integer>>();
		HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
		HashMap<String, String> opinionMap = new HashMap<String, String>();
		HashMap<String, Boolean> exclamationMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> questionMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> emoticonMap = new HashMap<String, Boolean>();
		Vector<String> vocabVector = new Vector<String>();

		System.out.println("Build set of stopwords");
		
		// Set of stopwords
		File stopWords = new File("./data/StopWords.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(stopWords))) {
			for (String line; (line = br.readLine()) != null;) {
				stopWordsSet.add(line);
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
					}
				}

				bagOfWords.put(tweetID, wordsMap);
			}
		}

		System.out.println("Generate arff file");
		
		// Generates a ARFF file with different attribute types.

		// 1. set up attributes
		atts = new FastVector();

		// - numeric
		for (String word : vocabVector) {
			atts.addElement(new Attribute(word));
		}
		
		// - nominal
		attEm = new FastVector();
		attEm.addElement("Y");
		attEm.addElement("N");
		atts.addElement(new Attribute("PositiveEmiticon", attEm));

		// - nominal
		attQu = new FastVector();
		attQu.addElement("Y");
		attQu.addElement("N");
		atts.addElement(new Attribute("QuestionMark", attQu));

		// - nominal
		attEx = new FastVector();
		attEx.addElement("Y");
		attEx.addElement("N");
		atts.addElement(new Attribute("ExclamationMark", attEx));

		// - nominal
		attVals = new FastVector();
		attVals.addElement("positive");
		attVals.addElement("negative");
		attVals.addElement("neutral");
		attVals.addElement("objective");
		atts.addElement(new Attribute("OpinionCategory", attVals));

		// 2. create Instances object
		instance = new Instances("Opinion", atts, 0);

		// 3. fill with data
		for (String key : bagOfWords.keySet()) {
			vals = new double[instance.numAttributes()];
			
			Map<String, Integer> sentence = bagOfWords.get(key);

			// set numerical word attributes
			for (int i = 0; i < vocabVector.size(); i++) {
				if (sentence.containsKey(vocabVector.get(i))) {
					vals[i] = sentence.get(vocabVector.get(i));
				} else {
					vals[i] = 0;
				}
			}
			
			// set nominal emoticon attribute
			if (emoticonMap.get(key)) {
				vals[instance.numAttributes() - 4] = attEm.indexOf("Y");
			} else {
				vals[instance.numAttributes() - 4] = attEm.indexOf("N");
			}

			// set nominal question mark attribute
			if (questionMap.get(key)) {
				vals[instance.numAttributes() - 3] = attQu.indexOf("Y");
			} else {
				vals[instance.numAttributes() - 3] = attQu.indexOf("N");
			}
			
			// set nominal exclamation mark attribute
			if (exclamationMap.get(key)) {
				vals[instance.numAttributes() - 2] = attEx.indexOf("Y");
			} else {
				vals[instance.numAttributes() - 2] = attEx.indexOf("N");
			}

			// set nominal opinion attribute
			vals[instance.numAttributes() - 1] = attVals.indexOf(opinionMap
					.get(key));

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