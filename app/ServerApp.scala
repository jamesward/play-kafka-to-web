import java.util.UUID

import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.mvc.{Call, Results, WebSocket}
import play.api.routing.Router
import play.api.routing.sird._
import play.api.{BuiltInComponents, Mode}
import play.core.server.{NettyServerComponents, ServerConfig}

object ServerApp extends App {

  val components = new NettyServerComponents with BuiltInComponents {

    val port = sys.env.getOrElse("PORT", "8080").toInt
    val mode = if (configuration.get[String]("play.http.secret.key").contains("changeme")) Mode.Dev else Mode.Prod

    override lazy val serverConfig = ServerConfig(port = Some(port), mode = mode)

    val consumerSettings = ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(configuration.get[String]("kafka.bootstrap_servers"))
      .withGroupId(UUID.randomUUID().toString)

    def kafkaSource(topic: String): Source[ConsumerRecord[String, String], _] = {
      val subscriptions = Subscriptions.topics(topic)
      Consumer.plainSource(consumerSettings, subscriptions)
    }

    lazy val router = Router.from {
      case GET(p"/") => Action { implicit request =>
        val revision = sys.env.getOrElse("K_REVISION", "(no revision)")
        Results.Ok(html.index(Call("GET", "/ws").webSocketURL(), revision))
      }
      case GET(p"/ws") => WebSocket.accept[String, String] { _ =>
        val sink = Sink.ignore
        val source = kafkaSource(configuration.get[String]("kafka.topic")).map(_.value())
        Flow.fromSinkAndSource(sink, source)
      }
    }

    override def httpFilters = Seq.empty
  }

  val server = components.server

  while (!Thread.currentThread.isInterrupted) {}

  server.stop()

}
