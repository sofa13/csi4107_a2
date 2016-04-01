# CSI4107, Winter 2016 - Assignment 2

## Team members

Theodore Morin - 6860630

Sophie Le Page - 5992312

## Work distribution

To implement a program that extracts features from the tweets and saves them in an arff file, we did peer programming outside of class and in class, and we did individual programming. To collaborate and manage our code, we used a GitHub repository.

Sophie worked on researching and setting up the basics of creating the arff file. This involved setting up the first feature extraction using the words from the tweets as features in the arff file. After, Sophie expanded the features and added positive emoticons, punctuation marks such as ! and ?, hash tags, and the number of positive and negative words using the LIWC external resource.

Ted worked on improving and optimizing the existing feature extractions, and worked on other feature extractions, such as negative emoticons and hashtags. Ted also included the use of the SentiWord, an external resources that includes list of positive and negative words. Ted also spent a lot of time experimenting with different features and refactoring code.

Both team members worked on evaluating the best classifier using Weka and on writing up documentation.

### Functionality of our program

The purpose of this assignment is to classify Twitter messages as expressing a positive opinion, a negative opinion, or no opinion (neutral or objective). To do so we wrote a program that extracts features from the tweets and saves them in an .arff file. Then we used a tool named Weka, opened the arff file in Weka’s GUI, and ran machine learning algorithms that were appropriate for our task.

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

Just run `java csi4107.AttTwitterData` with `weka-3-6-13.jar` in your class path. It will generate `data/semeval_twitter_data.arff`, which you can open with Weka. Then, you can run the classifications.

## Customization Process

### Step 1

For step 1 we trained a classifier using the bag-of-words (BOW) representation. This means we used words from the tweets as features in the arff file. We eliminated stop words, rare words, and punctuation, in order to reduce the dimension of the vector space. To do this we built the arff file with your own program.

We used an evaluation technique called 10-fold cross validation, available in Weka, and then applied three classifiers from Weka: SVM (SMO in Weka), Naive Bayes, and Decision Trees (J48 in Weka).

### Step 2

For step 2 we added more features and trained the same classifiers. We added the use of postive and negative emoticons from the tweets as features as well as punctuation marks such as ! and ?, and hash tags. We also added the number of positive words and negative words in the tweets as features. To get the list of positive and negative words we tried two resources: [LIWC](http://www.site.uottawa.ca/~diana/csi4107/A2_2016.htm) and [SentiWordNet](http://sentiwordnet.isti.cnr.it/).

Again, we used the same evaluation technique and classifiers as step 1 in order to try and improve the classification results.

## Results

### Part 1

Using the default BOW representation, our results hit about 44% correctness.

### Part 2

To get classification results, we applied three classifiers from Weka:

- SVM (SMO in Weka)

```
=== SMO ===
=== Summary ===

Correctly Classified Instances        3501               49.7231 %
Incorrectly Classified Instances      3540               50.2769 %
Kappa statistic                          0.2493
Mean absolute error                      0.3225
Root mean squared error                  0.4096
Relative absolute error                 93.3253 %
Root relative squared error             98.5383 %
Total Number of Instances             7041     

=== Detailed Accuracy By Class ===

               TP Rate   FP Rate   Precision   Recall  F-Measure   ROC Area  Class
                 0.72      0.377      0.616     0.72      0.664      0.701    positive
                 0.382     0.11       0.434     0.382     0.407      0.706    negative
                 0.267     0.168      0.3       0.267     0.283      0.567    neutral
                 0.285     0.089      0.362     0.285     0.319      0.698    objective
Weighted Avg.    0.497     0.241      0.478     0.497     0.485      0.673

=== Confusion Matrix ===

    a    b    c    d   <-- classified as
 2314  299  385  218 |    a = positive
  451  485  255   77 |    b = negative
  619  241  400  237 |    c = neutral
  371   93  294  302 |    d = objective
```

- Naive Bayes

```
=== Stratified cross-validation ===
=== Summary ===

Correctly Classified Instances        3549               50.4048 %
Incorrectly Classified Instances      3492               49.5952 %
Kappa statistic                          0.2814
Mean absolute error                      0.2859
Root mean squared error                  0.3988
Relative absolute error                 82.7229 %
Root relative squared error             95.9408 %
Total Number of Instances             7041     

=== Detailed Accuracy By Class ===

               TP Rate   FP Rate   Precision   Recall  F-Measure   ROC Area  Class
                 0.683     0.292      0.663     0.683     0.673      0.753    positive
                 0.38      0.089      0.484     0.38      0.426      0.747    negative
                 0.265     0.157      0.313     0.265     0.287      0.641    neutral
                 0.449     0.166      0.324     0.449     0.377      0.727    objective
Weighted Avg.    0.504     0.208      0.505     0.504     0.502      0.724

=== Confusion Matrix ===

    a    b    c    d   <-- classified as
 2195  281  359  381 |    a = positive
  324  482  266  196 |    b = negative
  512  174  396  415 |    c = neutral
  280   58  246  476 |    d = objective
```

- and Decision Trees (J48 in Weka)

```
=== Stratified cross-validation ===
=== Summary ===

Correctly Classified Instances        3462               49.1692 %
Incorrectly Classified Instances      3579               50.8308 %
Kappa statistic                          0.2117
Mean absolute error                      0.2926
Root mean squared error                  0.4117
Relative absolute error                 84.6839 %
Root relative squared error             99.0373 %
Total Number of Instances             7041     

=== Detailed Accuracy By Class ===

               TP Rate   FP Rate   Precision   Recall  F-Measure   ROC Area  Class
                 0.783     0.491      0.573     0.783     0.662      0.727    positive
                 0.341     0.101      0.425     0.341     0.378      0.68     negative
                 0.211     0.13       0.304     0.211     0.249      0.611    neutral
                 0.184     0.066      0.331     0.184     0.237      0.664    objective
Weighted Avg.    0.492     0.28       0.453     0.492     0.459      0.684

=== Confusion Matrix ===

    a    b    c    d   <-- classified as
 2519  245  314  138 |    a = positive
  572  432  185   79 |    b = negative
  783  221  316  177 |    c = neutral
  523  118  224  195 |    d = objective
```

The Naive Bayes classification method and the following feature representation led to the **best results**:

- 50.4048% correct

Our best results for each message can be viewed in the `Results(TYPE).txt` files.

The used feature extractions are:

- Number of positive words (including prefixes) (+1 if there are no negatives)
- Number of negative words (including suffixes)
- Bag of Words, for words occurring 3 or more times
- Binary presence of
    - question marks
    - exclamation points
    - hashtag/pound symbol
    - happy emoticon
    - sad emoticon

The following are feature extractions we tried and that did not help:

- Using SentiWordNet external resource to analyze sentence sentiment for words and phrases (decimal positive and negative values, objectivity and neutrality calculations included)
- Tweet length (words, characters)
- Counting the number of ! and ? instead of just the presence of them
- Including words in the vocabulary with hashtags
- Presence of words written in CAPITALS
- Repeated characters such as "yayyyy" or "....."
- heart emoticons

## Conclusion

We had a lot of trouble working through different methods, most things lowered our accuracy. Particularly, the more accurate we tried to be, the less percentage in the result. We suspect that part of the reason is that there's a differentiation between neutrality and objectivity, which our application doesn't handle very well. It would also be nice to be able to determine more about the results of Weka. A collaboration aspect would also make this project much more interesting.
