import scala.util.{Failure, Success, Try}

import com.ericsson.otp.erlang.{OtpErlangAtom => EAtom, OtpErlangDecodeException => EDecodeException, OtpErlangObject => EObject, OtpErlangPid => EPid, OtpErlangString => EString, OtpErlangTuple => ETuple, OtpMbox => EMbox, OtpNode => ENode}
import com.google.common.util.concurrent.AbstractExecutionThreadService
import com.typesafe.scalalogging.LazyLogging

class Server(node: ENode, mbox: EMbox) extends AbstractExecutionThreadService with LazyLogging{

  def handle(msg: ETuple): Unit = msg.elementAt(0).asInstanceOf[EAtom].atomValue match {
    //{$gen_cast, {<MESSAGE>}}
    case "$gen_cast" => handleCast(msg.elementAt(1).asInstanceOf[ETuple])
    //{$gen_call, {Pid, Ref}, {<MESSAGE>}}
    case "$gen_call" => handleCall(msg=msg.elementAt(2).asInstanceOf[ETuple],sender=msg.elementAt(1).asInstanceOf[ETuple])
    case _ => stopAsync()
  }

  def handleCall(msg: ETuple, sender: ETuple): Unit = msg.elementAt(0).asInstanceOf[EAtom].atomValue match {
    //{$gen_call, {Pid, Ref}, {:pid}} -> {Pid, {Ref, {:pid, self()}}}
    //            ^ FROM      ^ MSG
    case "pid" => mbox.send(sender.elementAt(0).asInstanceOf[EPid],
                            new ETuple(Array(sender.elementAt(1),
                                             new ETuple(Array(msg.elementAt(0),
                                                              mbox.self())))))
    //{$gen_call, {Pid, Ref}, {:say}} -> {Pid, {Ref, {:say, <MESSSAGE>}}}
    //            ^ FROM      ^ MSG
    case "say" => send(sender, new ETuple(Array(sender.elementAt(1),
                                                 new ETuple(Array(msg.elementAt(0),
                                                 new EString(LedController.blinkSay))))))
    case _ => throw new EDecodeException(s"Bad message format: $msg")
  }

  def handleCast(msg: ETuple): Unit = messageType(msg) match {
    //{$gen_cast, {:blink}}
    case "blink" => LedController.blink
    case _ => throw new EDecodeException(s"Bad message format: $msg")
  }

  def messageType(msg: ETuple): String  = msg.elementAt(0).asInstanceOf[EAtom].atomValue
  def send(to: ETuple, message: EObject): Unit = mbox.send(to.elementAt(0).asInstanceOf[EPid], message)

  override def run(): Unit = while(isRunning()){
    mbox.receive() match {
      case m: ETuple => handle(m)
      case _ =>
        println("Invalid call")
        System.exit(-1)
    }
  }

  override def shutDown(): Unit = {
    mbox.close()
    node.close()
  }
}

case class Config(processName: String = "mbox", selfAddr: String = "echo@127.0.0.1", cookie: String = "cookie", test: Boolean = false)

object Config{
  val parser = new scopt.OptionParser[Config]("echo"){
    head("echo", "1.x")
    opt[String]('p',"process-name") required() valueName("<mbox>") action{ (x,c) =>
      c.copy(processName=x) } text("Java application process name")
    opt[String]('s',"self-name") required() valueName("<echo@localhost>") action{ (x,c) =>
      c.copy(selfAddr=x) } text("Java node name")
    opt[String]('c',"cookie") required() valueName("<cookie>") action{ (x,c) =>
      c.copy(cookie=x) } required() text("Erlang node cookie")
    cmd("blink") hidden() action { (_, c) =>
      c.copy(test = true) } text("test leds w/o running a server") }
}
object LedNotifier extends App {
  import Config.parser

  parser.parse(args, Config()) match {
    case Some(config) if config.test => LedController.blink
    case Some(config) => (for{
      s <- Try(new ENode(config.selfAddr, config.cookie))
      m <- Try(s.createMbox(config.processName))
    } yield new Server(s, m)).map(_.startAsync().awaitRunning()) match {
        case Success(_) => println("READY")
        case Failure(err) => println(s"Server failed to start with message:\n\t$err.getMessage\nProbably you did not start the epmd. Java nodes don't start epmd on their own as erlang nodes do.")
      }
    case None =>
      println("Invalid startup parameters")
      System.exit(-1)
  }
}
