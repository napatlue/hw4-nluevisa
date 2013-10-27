package edu.cmu.lti.f13.hw4.hw4_nluevisa.casconsumers;

import java.util.Iterator;
import java.util.Map;

public class CosineRetrievalEvaluator extends AbstractRetrievalEvaluator{

  @Override
  protected double computeSimilarity(Map<String, Integer> queryVector,
          Map<String, Integer> docVector) {
    // TODO :: compute cosine similarity between two sentences
    double cosine_similarity=0.0;
    Iterator itr1 = queryVector.entrySet().iterator();
    while (itr1.hasNext()) {
        Map.Entry pairs = (Map.Entry)itr1.next();
        String term = (String)pairs.getKey();
        int freq1 = (Integer)pairs.getValue();
        
        if(docVector.containsKey(term))
        {
          int freq2 = docVector.get(term);
          cosine_similarity += freq1*freq2;
        }
    }
    
    double queryVectorSize = computeVectorSize(queryVector);
    double docVectorSize = computeVectorSize(docVector);
    return cosine_similarity/(queryVectorSize*docVectorSize);
  }

}
