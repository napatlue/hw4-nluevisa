package edu.cmu.lti.f13.hw4.hw4_nluevisa.utils;



import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * Class for storing the similarity result between query and document
 */
public class DocScore implements Comparable<DocScore>{

  private int relevance;
  private String oriText;
  private double score;
  public DocScore(String oriText, int relevance, double score) {
    // TODO Auto-generated constructor stub
    this.relevance = relevance;
    this.oriText = oriText;
    this.score = score;
  }
  @Override
  public int compareTo(DocScore doc2) {
    // TODO Auto-generated method stub
    
    if(this.score-doc2.score>0)
      return -1;
    else if(this.score-doc2.score<0)
      return 1;
    else
      return 0;
   
  }
  public int getRelevance() {
    return relevance;
  }
  public void setRelevance(int relevance) {
    this.relevance = relevance;
  }
  public String getOriText() {
    return oriText;
  }
  public void setOriText(String oriText) {
    this.oriText = oriText;
  }
  public double getScore() {
    return score;
  }
  public void setScore(double score) {
    this.score = score;
  }

}