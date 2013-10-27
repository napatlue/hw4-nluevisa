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


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;


	private Map<Integer,Document> queryMap;
	
	private ArrayList<Map<String,Integer>> docTermMap;

	private ArrayList<String> oriTextList;
	
	private Map<Integer, ArrayList<DocScore>> queryResultMap;
	
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

			//System.out.println(doc.getCoveredText());
			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
			//System.out.println(tokenList);
			
			int qId = doc.getQueryID();
			int rel = doc.getRelevanceValue();
			qIdList.add(qId);
			relList.add(rel);
			oriTextList.add(doc.getText());
			
			Map<String, Integer> termMap = new HashMap<String, Integer>();
			//Do something useful here
			for(int i=0;i<tokenList.size();i++)
      {
        Token word = tokenList.get(i);
        String term = word.getText();
        term = term.toLowerCase();
        int freq = word.getFrequency();
        termMap.put(term, freq);
        //docTokList.get(i).add(new Pair<String, Integer>(term, freq));
      }
			//System.out.println(termMap);
			docTermMap.add(termMap);
			

		}
		

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// TODO :: compute the cosine similarity measure
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
		    double similarity = computeCosineSimilarity(queryVector, docVector);
		    //System.out.println(similarity);
		    answerList.add(new DocScore(oriText, rel,similarity));
		  }
		}
		

		
		// TODO :: compute the rank of retrieved sentences
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
		
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;

		// TODO :: compute cosine similarity between two sentences
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

	 /**
   * 
   * @return vectorSize ||v||
   */
  private double computeVectorSize(Map<String, Integer> docVector) {
    double v=0.0;

    // TODO :: compute cosine similarity between two sentences
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

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		for(int i=0; i< rrScoreList.size(); i++)
		{
		  metric_mrr += rrScoreList.get(i);
		}
		return metric_mrr/(rrScoreList.size());
	}

}
