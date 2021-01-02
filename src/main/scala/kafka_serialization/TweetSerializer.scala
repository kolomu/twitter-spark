package kafka_serialization

import com.danielasfregola.twitter4s.entities.Tweet
import org.apache.kafka.common.serialization.Serializer

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

class TweetSerializer extends Serializer[Tweet] {
  override def serialize(topic: String, data: Tweet): Array[Byte] = {
    try {
      val byteOut = new ByteArrayOutputStream()
      val objOut = new ObjectOutputStream(byteOut)
      objOut.writeObject(data)
      objOut.close()
      byteOut.close()
      byteOut.toByteArray
    } catch {
      case ex:Exception => throw new Exception(ex.getMessage)
    }
  }
}
