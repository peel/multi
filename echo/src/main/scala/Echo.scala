import scala.util.{Failure, Success, Try}

import com.ericsson.otp.erlang.{
  OtpEpmd,
  OtpErlangAtom => EAtom,
  OtpErlangDecodeException => EDecodeException,
  OtpErlangObject => EObject,
  OtpErlangPid => EPid,
  OtpErlangString => EString,
  OtpErlangTuple => ETuple,
  OtpMbox => EMbox,
  OtpNode => ENode
}

case class Config(msg: String = "echo", processName: String = "mbox", selfAddr: String = "echo@127.0.0.1", cookie: String = "cookie" )

object Config{
  val parser = new scopt.OptionParser[Config]("echo"){
    head("echo", "1.x")
    opt[String]('m',"msg") required() valueName("string") action{ (x,c) =>
      c.copy(msg=x) } text("String message to echo")
    opt[String]('p',"process-name") required() valueName("<mbox>") action{ (x,c) =>
      c.copy(processName=x) } text("Java application process name")
    opt[String]('s',"self-name") required() valueName("<echo@localhost>") action{ (x,c) =>
      c.copy(selfAddr=x) } text("Java node name")
    opt[String]('c',"cookie") required() valueName("<cookie>") action{ (x,c) =>
      c.copy(cookie=x) } required() text("Erlang node cookie")
  }
}
object Echo extends App {
  import Config.parser

  parser.parse(args, Config()) match {
    case Some(config) => for{
        s <- Try(new ENode(config.selfAddr, config.cookie))
        m <- Try(s.createMbox(config.processName))
      } yield {
        Try(m.receive().asInstanceOf[ETuple]).map{msg =>
          msg.elementAt(1).asInstanceOf[ETuple].elementAt(0).asInstanceOf[EAtom].atomValue match {
            case "stop" => System.exit(0)
            case _ => m.send(msg.elementAt(0).asInstanceOf[EPid], new ETuple(Array(msg.elementAt(1).asInstanceOf[ETuple].elementAt(0), new EString("and hello to you"))))
          }
        }.getOrElse(println("TANGO DOWN"))
        m.close()
        s.close()
    }
    case None =>
      System.exit(-1)
  }
}
