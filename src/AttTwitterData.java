import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
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
		Instances instance;
		double[] vals;
		Set<String> stopWordsSet = new HashSet<String>();
		Map<String, Map<String, Integer>> bagOfWords = new HashMap<String, Map<String, Integer>>();
		HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
		HashMap<String, String> opinionMap = new HashMap<String, String>();
		Vector<String> vocabVector = new Vector<String>();

		// Set of stopwords
		File stopWords = new File("./data/StopWords.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(stopWords))) {
			for (String line; (line = br.readLine()) != null;) {
				stopWordsSet.add(line);
			}
		}

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

				String[] words = sentence.split(" ");

				opinionMap.put(tweetID, opinion);

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

		// Remove rare words from vocabulary
		Iterator it = vocabulary.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry w = (HashMap.Entry) it.next();
			if ((int) w.getValue() < 100) {
				it.remove();
			} else {
				vocabVector.add((String) w.getKey());
			}
		}
		
		// Build bag of words for each tweet { tweetID: { word : {count} }}
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
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

		// Generates a ARFF file with different attribute types.

		// 1. set up attributes
		atts = new FastVector();

		// - numeric
		for (String word : vocabVector) {
			atts.addElement(new Attribute(word));
		}
		
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
		for(String key : bagOfWords.keySet()){
			vals = new double[instance.numAttributes()];  // important: needs NEW array!
            
			Map<String, Integer> sentence = bagOfWords.get(key);
            
            for(int i = 0; i < vocabVector.size(); i++)
            {
            		if(sentence.containsKey(vocabVector.get(i))){
            			vals[i] = sentence.get(vocabVector.get(i));
            		} 
            		else {
            			vals[i] = 0;
             		}
            }
            
            vals[instance.numAttributes()-1] = attVals.indexOf(opinionMap.get(key));
            
            instance.add(new Instance(1.0, vals));
        }
		
		
		// Write to files 
		
	     PrintWriter writer = new PrintWriter("./data/semeval_twitter_data_arff.txt", "UTF-8");
	     writer.println(instance);
	     writer.close();
	     
	     ArffSaver saver = new ArffSaver();
	     saver.setInstances(instance);
	     saver.setFile(new File("./data/semeval_twitter_data.arff"));
	     saver.writeBatch();
	}
}