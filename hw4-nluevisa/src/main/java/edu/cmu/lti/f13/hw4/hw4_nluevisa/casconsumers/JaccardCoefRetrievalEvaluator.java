package edu.cmu.lti.f13.hw4.hw4_nluevisa.casconsumers;

import java.util.Iterator;
import java.util.Map;
/**
 * Class for computing the Jaccard Coefficient and performance metric of the collection 
 */
public class JaccardCoefRetrievalEvaluator extends AbstractRetrievalEvaluator{

  @Override
  /**
   * @return Jaccard Coefficient
   */
  protected double computeSimilarity(Map<String, Integer> queryVector,
          Map<String, Integer> docVector) {
    // TODO :: compute cosine similarity between two sentences
    double jaccardCoefficience=0.0;
    Iterator itr1 = queryVector.entrySet().iterator();
    while (itr1.hasNext()) {
        Map.Entry pairs = (Map.Entry)itr1.next();
        String term = (String)pairs.getKey();
        int freq1 = (Integer)pairs.getValue();
        
        if(docVector.containsKey(term))
        {
          int freq2 = docVector.get(term);
          jaccardCoefficience += freq1*freq2;
        }
    }
    
    double sqQueryVectorSize = Math.pow(computeVectorSize(queryVector),2);
    double sqDocVectorSize = Math.pow(computeVectorSize(docVector),2);
    return jaccardCoefficience/(sqQueryVectorSize+sqDocVectorSize-jaccardCoefficience);
  }

}
