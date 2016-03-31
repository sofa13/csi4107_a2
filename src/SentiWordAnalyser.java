import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SentiWordAnalyser {
	public static class SentiWord {
		private String word;
		private double positive;
		private double negative;
		private int length = 0;
		SentiWord(String word, String positive, String negative) {
			this(word, Double.parseDouble(positive), Double.parseDouble(negative));
		}
		
		SentiWord(String word, double positive, double negative) {
			this.word = word;
			this.positive = positive;
			this.negative = negative;
			this.length = word.split(" ").length;
		}
		
		public double getPositive() {
			return positive;
		}
		
		public double getNegative() {
			return negative;
		}
		
		public double getObjective() {
			double denominator = Math.max(Math.abs(positive + negative), 1);
			return 1.0 - (Math.max(positive, negative) / denominator);
		}
		
		public int length() {
			return length;
		}
		
		public String toString() {
			return "\"" + this.word + "\": " + this.positive + " positive, " + this.negative + " negative.";
		}
	}

	private HashMap<String, SentiWord> wordlist = new HashMap<String, SentiWord>();
	private int longestPhrase = 0;

	public SentiWordAnalyser(String path) throws IOException, URISyntaxException {
		Files.lines(Paths.get(ClassLoader.getSystemResource(path).toURI()))
		  .map(line -> line.split("\t"))
		  .forEach(item -> wordlist.put(item[0], new SentiWord(item[0], item[1], item[2])));

		this.longestPhrase = wordlist.values().stream().max((a, b) -> a.length - b.length).get().length;
	}
	
	public SentiWord analyzeSentence(String sentence) {
		double sentencePositive = 0.00;
		double sentenceNegative = 0.00;
		String cleanedUpSentence = sentence.replace("[.,#()\"!?]", "");
		int numberOfExclams = countOccurences(sentence, '!');
		int numberOfQs = countOccurences(sentence, '?');
		List<String> words = Arrays.asList(cleanedUpSentence.split(" "))
				.parallelStream()
				.map(word -> word.toLowerCase())
				.collect(Collectors.toList());
		for (int i = this.longestPhrase; i > 0; i--) {
			for (int j = 0; j < words.size() - i; j++) {
				String substring = words
						.subList(j, j + i)
						.stream()
						.collect(Collectors.joining(" "));;
				if (this.wordlist.containsKey(substring)) {
					SentiWord validWord = this.wordlist.get(substring);
					sentencePositive += validWord.getPositive();
					sentenceNegative += validWord.getNegative();
				}
			}
		}
		sentencePositive *= (1.0 + 0.1 * numberOfExclams);
		sentencePositive *= (1.0 - 0.1 * numberOfQs);
		sentenceNegative *= (1.0 + 0.1 * numberOfExclams);
		sentenceNegative *= (1.0 - 0.1 * numberOfQs);
		return new SentiWord(sentence, sentencePositive, sentenceNegative);
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
