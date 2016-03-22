import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
 
 /**
  * Generates a little ARFF file with different attribute types.
  *
  * @author FracPete
  */
 public class AttTwitterData {
   public static void main(String[] args) throws Exception {
     FastVector      atts;
     FastVector      attsRel;
     FastVector      attVals;
     FastVector      attValsRel;
     Instances       data;
     Instances       dataRel;
     double[]        vals;
     double[]        valsRel;
     int             i;
 
     // 1. set up attributes
     atts = new FastVector();
     // - string
     atts.addElement(new Attribute("sentence", (FastVector) null));
     // - nominal
     attVals = new FastVector();
     attVals.addElement("positive");
     attVals.addElement("negative");
     attVals.addElement("neutral");
     attVals.addElement("objective");
     atts.addElement(new Attribute("category", attVals));
 
     // 2. create Instances object
     data = new Instances("opinion", atts, 0);
 
     // 3. fill with data
//     URL url = AttTwitterData.class.getResource("semeval_twitter_data.txt");
//     File file = new File(url.getPath());
     int num = 0;
     File file = new File("C:/Users/Sophie/Desktop/semeval_twitter_data.txt");
     try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	        // process the line.
    	    	vals = new double[data.numAttributes()];
    	    	@SuppressWarnings("resource")
				Scanner s = new Scanner(line).useDelimiter("\\t");
    	    	s.next();
    	    	s.next();
    	    	vals[1] = attVals.indexOf(s.next().replace("\"", ""));
    	    	vals[0] = data.attribute(0).addStringValue(s.next());
    	        data.add(new Instance(1.0, vals));
    	        num++;
    	        if(num > 100) break;
    	    }
    	}

     System.out.println(data);
 
//     FileOutputStream out = new FileOutputStream("C:/Users/Sophie/Desktop/MyRelation.arff");
//     out.write(data);
//     out.close();
   }
 }