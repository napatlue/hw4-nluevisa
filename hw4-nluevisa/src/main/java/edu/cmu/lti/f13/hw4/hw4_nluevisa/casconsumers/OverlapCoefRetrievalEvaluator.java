package edu.cmu.lti.f13.hw4.hw4_nluevisa.casconsumers;

import java.util.Iterator;
import java.util.Map;

public class OverlapCoefRetrievalEvaluator extends AbstractRetrievalEvaluator{

  @Override
  protected double computeSimilarity(Map<String, Integer> queryVector,
          Map<String, Integer> docVector) {
    // TODO :: compute cosine similarity between two sentences
    double overlapCoefficience=0.0;
    Iterator itr1 = queryVector.entrySet().iterator();
    while (itr1.hasNext()) {
        Map.Entry pairs = (Map.Entry)itr1.next();
        String term = (String)pairs.getKey();
        int freq1 = (Integer)pairs.getValue();
        
        if(docVector.containsKey(term))
        {
          int freq2 = docVector.get(term);
          overlapCoefficience += freq1*freq2;
        }
    }
    
    double sqQueryVectorSize = Math.pow(computeVectorSize(queryVector),2);
    double sqDocVectorSize = Math.pow(computeVectorSize(docVector),2);
    return (overlapCoefficience)/(Math.min(sqQueryVectorSize,sqDocVectorSize));
  }

}
