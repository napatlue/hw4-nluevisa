package edu.cmu.lti.f13.hw4.hw4_nluevisa.casconsumers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_nluevisa.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_nluevisa.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_nluevisa.utils.DocScore;
import edu.cmu.lti.f13.hw4.hw4_nluevisa.utils.Utils;

/**
 * Abstract class for computing the similarity and performance metric of the collection 
 * This class has one abstract method computeSimilarity.
 */
public abstract class AbstractRetrievalEvaluator extends CasConsumer_ImplBase {

  /** query id number **/
  public ArrayList<Integer> qIdList;

  /** query and text relevant values **/
  public ArrayList<Integer> relList;

  /** map from queryId to Document **/
  private Map<Integer,Document> queryMap;
 
  /** map from queryId to Document Term Vector **/
  private ArrayList<Map<String,Integer>> docTermMap;

  /** query and original sentence **/
  private ArrayList<String> oriTextList;
  
  /** map from queryId to list of documents and their score **/
  private Map<Integer, ArrayList<DocScore>> queryResultMap;
  
  /** list of reciprocal rank score **/
  private ArrayList<Float> rrScoreList;
  
  
  public void initialize() throws ResourceInitializationException {

    qIdList = new ArrayList<Integer>();

    relList = new ArrayList<Integer>();
    
    queryMap = new HashMap<Integer, Document>();
    
    docTermMap = new ArrayList<Map<String,Integer>>();
    
    oriTextList = new ArrayList<String>();
    
    queryResultMap = new HashMap<Integer, ArrayList<DocScore>>();
    
    rrScoreList = new ArrayList<Float>(); 
  
  }

  /**
   * TODO :: 1. construct the global word dictionary 2. keep the word
   * frequency for each sentence
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {

    JCas jcas;
    try {
      jcas =aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    
    FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
    
    
    if (it.hasNext()) {
      Document doc = (Document) it.next();

      FSList fsTokenList = doc.getTokenList();
      ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);

      
      int qId = doc.getQueryID();
      int rel = doc.getRelevanceValue();
      qIdList.add(qId);
      relList.add(rel);
      oriTextList.add(doc.getText());
      
      Map<String, Integer> termMap = new HashMap<String, Integer>();
      
      // Create Document Term Vector 
      for(int i=0;i<tokenList.size();i++)
      {
        Token word = tokenList.get(i);
        String term = word.getText();
        term = term.toLowerCase();
        int freq = word.getFrequency();
        termMap.put(term, freq);
      }
      //System.out.println(termMap);
      docTermMap.add(termMap);
      

    }
    

  }

  /**
   * Compute Similarity and rank the retrieved sentences and compute the MRR metric
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0)
      throws ResourceProcessException, IOException {

    super.collectionProcessComplete(arg0);

    // Compute the similarity measure
    Map<String,Integer> queryVector = new HashMap<String, Integer>();
    Map<String,Integer> docVector;
    
    int currentQId = -1;
    ArrayList<DocScore> answerList = null;
    for(int i=0;i<qIdList.size();i++)
    {
      int qId = qIdList.get(i);
      int rel = relList.get(i);
      String oriText = oriTextList.get(i);
      
      if(rel == 99)
      {
        currentQId = qId;
        queryVector = docTermMap.get(i);
        answerList = new ArrayList<DocScore>();
        queryResultMap.put(currentQId, answerList);
      }
      else
      {
        docVector = docTermMap.get(i);
        double similarity = computeSimilarity(queryVector, docVector);
        //System.out.println(similarity);
        answerList.add(new DocScore(oriText, rel,similarity));
      }
    }
    
    System.out.println("Evaluator Class: "+this.getClass().toString());
    
    // Compute the rank of retrieved sentences
    Iterator it = queryResultMap.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        int qid = (Integer)pairs.getKey();
        ArrayList<DocScore> docScoreList = (ArrayList<DocScore>)pairs.getValue();
        Collections.sort(docScoreList);
        for(int i=0;i<docScoreList.size();i++){
          DocScore docScore = docScoreList.get(i);
          //System.out.println("rank "+i + docScore.getOriText() + " "+docScore.getScore() + "rel "+docScore.getRelevance());
          if(docScore.getRelevance() == 1)
          { 
            float rr  = 1/(float)(i+1);
           // output format: Score: 0.33806170189140655 rank=1 rel=1 qid=1 sent1
            System.out.println("Score: "+docScore.getScore()+" rank="+(i+1)+" rel="+docScore.getRelevance()+" qid="+qid+" "+ docScore.getOriText());
            rrScoreList.add(rr);
          }
        }
      // System.out.println(docScoreList);
    }
    
    
    // Compute the metric:: mean reciprocal rank
    double metric_mrr = compute_mrr();
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
    System.out.println("============================================");
  }

  /**
   * Abstract method to compute similarity between document vector
   * @return similarity value between two vector
   */
  protected abstract double computeSimilarity(Map<String, Integer> queryVector, Map<String, Integer> docVector);

   /**
   * 
   * @return vectorSize ||v||
   */
  protected double computeVectorSize(Map<String, Integer> docVector) {
    double v=0.0;

    // compute vector size
    Iterator it = docVector.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();

        int freq = (Integer)pairs.getValue();
        v += Math.pow(freq,2);
    }
    
    return Math.sqrt(v);
  }
    
 
  /**
   * 
   * @return mrr
   */
  private double compute_mrr() {
    double metric_mrr=0.0;

    // Compute Mean Reciprocal Rank (MRR) of the text collection
    for(int i=0; i< rrScoreList.size(); i++)
    {
      metric_mrr += rrScoreList.get(i);
    }
    return metric_mrr/(rrScoreList.size());
  }

}
