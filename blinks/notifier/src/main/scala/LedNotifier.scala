import scala.util.{Failure, Success, Try}

import Gpio.leds
import com.ericsson.otp.erlang.{OtpErlangAtom => EAtom, OtpErlangDecodeException => EDecodeException, OtpErlangObject => EObject, OtpErlangPid => EPid, OtpErlangString => EString, OtpErlangTuple => ETuple, OtpMbox => EMbox, OtpNode => ENode}
import com.google.common.util.concurrent.AbstractExecutionThreadService
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinState.{LOW => OFF, HIGH => ON}
import com.typesafe.scalalogging.LazyLogging

object LedController {
  def blinkSay: String = "blink"
  def blink: Unit = {
    leds.foreach(_.on)
    Thread.sleep(2000)
    leds.foreach(_.off)
  }
  implicit class Led(gpioController: GpioPinDigitalOutput) {
    def on = gpioController.setState(ON)
    def off = gpioController.setState(OFF)
  }
}

class Server(node: ENode, mbox: EMbox) extends AbstractExecutionThreadService with LazyLogging{
  def handle(tuple: ETuple): Unit = tuple.elementAt(0).asInstanceOf[EAtom].atomValue match {
    case "stop" => stopAsync()
    case "$gen_call" =>
      println(s"received: $tuple")
      logger.info(s"received $tuple")
      handleCall(from=tuple.elementAt(1).asInstanceOf[ETuple],
                 req=tuple.elementAt(2).asInstanceOf[ETuple])
    case _ => throw new EDecodeException(s"Bad message: $tuple")
  }

  def handleCall(from: ETuple, req: ETuple): Unit = req.elementAt(0).asInstanceOf[EAtom].atomValue match {
    case "pid" => reply(from, mbox.self())
    case "blinkSay" => reply(from, new EString(LedController.blinkSay))
    case _ => throw new EDecodeException(s"Bad message: $req")
  }

  def handleCast(from: ETuple, req: ETuple): Unit = messageName(req) match {
    case "blink" => LedController.blink
  }

  def messageName(tuple: ETuple): String  = tuple.elementAt(0).asInstanceOf[EAtom].atomValue
  def reply(from: ETuple, response: EObject): Unit = {
    mbox.send(from.elementAt(0).asInstanceOf[EPid], new ETuple(Array(from.elementAt(1), response)))
  }

  override def run(): Unit = {
    while(isRunning()){
      Try(handle(mbox.receive().asInstanceOf[ETuple])) match {
        case Success(s) => s
        case Failure(e) =>
          logger.error(e.getMessage)
          logger.info("Unrecongised message, ignoring.")
      }
    }
  }
  override def shutDown(): Unit = {
    mbox.close()
    node.close()
  }
}

case class Config(enode: String = "no@nohost", cookie: String = "empty", self: String = "__manager__no@nohost", procName: String = "manager_java_server")

object Config{
  val parser = new scopt.OptionParser[Config]("led-notify-scala"){
    head("rpi led notifier", "1.x")
    opt[String]('n',"enode") required() valueName("<some@host>") action{ (x,c) =>
      c.copy(enode=x) } text("Erlang node")
    opt[String]('s',"self") required() valueName("<__manager__some@host>") action{ (x,c) =>
      c.copy(self=x) } text("Erlang node cookie")
    opt[String]('c',"cookie") required() valueName("<node-cookie>") action{ (x,c) =>
      c.copy(cookie=x) } required() text("Erlang node cookie")
    opt[String]('p',"procName") required() valueName("manager_java_server") action{ (x,c) =>
      c.copy(procName=x) } text("Registered proc name")
  }
}
object LedNotifier extends App {
  import Config.parser

  parser.parse(args, Config()) match {
    case Some(config) =>
      (for {
          s <- Try(new ENode("__blinks_blinks@127.0.0.1", "monster"))
          m <- Try(s.createMbox("blinks_java_server"))
        } yield new Server(s, m)).map(_.startAsync().awaitRunning()) match {
          case Success(_) => println("READY")
          case Failure(err) => println(s"Server failed to start with message:\n\t$err.getMessage\nProbably you did not start the epmd. Java nodes don't start epmd on their own as erlang nodes do.")
        }
    case None =>
      println("Invalid startup parameters")
      System.exit(-1)
  }
}
