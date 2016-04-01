# CSI4107, Winter 2016 - Assignment 2

## Team members

Theodore Morin - 6860630

Sophie Le Page - 5992312

## Work distribution

To implement a program that extracts features from the tweets and saves them in an arff file, we did peer programming outside of class and in class, and we did individual programming. To collaborate and manage our code, we used a GitHub repository.

Sophie worked on researching and setting up the basics of creating the arff file. This involved setting up the first feature extraction using the words from the tweets as features in the arff file. After, Sophie expanded the features and added positive emoticons, punctuation marks such as ! and ?, hash tags, and the number of positive and negative words using the LIWC external resource.

Ted worked on greatly improving and optimizing the existing feature extractions, and worked on other feature extractions, such as negative emoticons and hashtags. Ted also included the use of the SentiWord, an external resources that includes list of positive and negative words.

Both team members worked on evaluating the best classifier using Weka and on writing up documentation.

## Functionality of our program

The purpose of this assignment is to classify tweeter messages as expressing a positive opinion, a negative opinion, or no opinion (neutral or objective). To do so we wrote a program that extracts features from the tweets and saves them in an .arff file. Then we used a tool named Weka, opened the arff file in Weka’s GUI, and ran machine learning algorithms that were appropriate for our task.

Our program does the following to extract features from the tweets:

1. Builds a set of [stopwords](http://www.site.uottawa.ca/~diana/csi5180/StopWords), positive words and negative words. The [LIWC](http://www.site.uottawa.ca/~diana/csi4107/A2_2016.htm) and [SentiWordNet](http://sentiwordnet.isti.cnr.it/) external resource were used to obtain a list of positive and negative words.

2. Builds the vocabulary of the [data](http://www.site.uottawa.ca/~diana/csi4107/semeval_twitter_data.txt) and removes stopwords and punctuation. While building the vocabulary we also keep track of the word occurrences, and extract the following features: exclamation marks, question marks, and emoticons.

3. Removes rare words from the vocabulary, in this case removing words from the vocabulary if they occur less than 5 times.

4. Uses the vocabulary to build a bag of words for each tweet, keeping track of word occurrences in the tweet. While building the bag of words, we extract the following features: number of positive words and negative words.

5. Generates an arff file with the following attributes:
  1. Opinion Category {positive, negative, neutral, objective}
  2. Exclamation Mark {Y,N}
  3. Question Mark {Y,N}
  4. Positive Emoticon {Y,N}
  5. Negative Emoticon {Y,N}
  7. Hash Tags {Y,N}
  6. Positive Words numeric (slightly favoring positive)
  7. Negative Words numeric
  8. word1 numeric
  9. word2 numeric...

6. Writes to arff file.

## Running our program

The following are complete instructions on how to run our program to extract features from the tweets.

## Step 1

For step 1 we trained a classifier using the bag-of-words (BOW) representation. This means we used words from the tweets as features in the arff file. We eliminated stop words, rare words, and punctuation, in order to reduce the dimension of the vector space. To do this we built the arff file with your own program.

We used an evaluation technique called 10-fold cross validation, available in Weka, and then applied three classifiers from Weka: SVM (SMO in Weka), Naive Bayes, and Decision Trees (J48 in Weka).

## Step 2

For step 2 we added more features and trained the same classifiers. We added the use of postive and negative emoticons from the tweets as features as well as punctuation marks such as ! and ?, and hash tags. We also added the number of positive words and negative words in the tweets as features. To get the list of positive and negative words we tried two resources: [LIWC](http://www.site.uottawa.ca/~diana/csi4107/A2_2016.htm) and [SentiWordNet](http://sentiwordnet.isti.cnr.it/).

Again, we used the same evaluation technique and classifiers as step 1 in order to try and improve the classification results.

## Results

To get classification results, we applied three classifiers from Weka:
- SVM (SMO in Weka)
- Naive Bayes
- and Decision Trees (J48 in Weka)

The Naive Bayes classification method and the following feature representation led to the **best results**:
- ...

This gives the following number of attributes:

Our best results for each message can be viewed in the `Results.txt` file.

The following are feature extractions we tried and that did not help:
- Using SentiWordNet external resource to analyze sentence sentiment
- Tweet length
- Counting the number of ! and ? vs just the presence of them
- Including words in the vocabulary with hashtags
- All-caps
- Repeated letters such as "yayyyy"
- heart emoticons
