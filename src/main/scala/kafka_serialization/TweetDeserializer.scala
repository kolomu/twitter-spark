package kafka_serialization

import com.danielasfregola.twitter4s.entities.Tweet
import org.apache.kafka.common.serialization.Deserializer

import java.io.{ByteArrayInputStream, ObjectInputStream}

class TweetDeserializer extends Deserializer[Tweet]{
  override def deserialize(topic: String, data: Array[Byte]): Tweet = {
    val byteIn = new ByteArrayInputStream(data)
    val objIn = new ObjectInputStream(byteIn)
    val obj = objIn.readObject().asInstanceOf[Tweet]
    byteIn.close()
    objIn.close()
    obj
  }
}
