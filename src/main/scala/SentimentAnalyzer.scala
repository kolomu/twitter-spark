import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import java.util.Properties
import scala.collection.JavaConversions._

object SentimentAnalyzer {
  val props = new Properties()
  props.put("annotators", "tokenize,ssplit,pos,parse,sentiment")
  val pipeline = new StanfordCoreNLP(props)

  def getSentiment(text: String): Double = {
    val document = new Annotation(text)
    pipeline.annotate(document)
    val sentences = document.get(classOf[CoreAnnotations.SentencesAnnotation])

    var sum = 0.0
    for (sentence <- sentences) {
      val tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
      val sentiment = RNNCoreAnnotations.getPredictedClass(tree)
      val scaled = sentiment - 2
      sum = sum + scaled
    }

    sum / sentences.size
  }
}
