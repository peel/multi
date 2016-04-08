import com.pi4j.wiringpi.SoftPwm
import com.pi4j.wiringpi.Gpio._
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinState.{LOW => OFF, HIGH => ON}

object LedController {
  def blinkSay: String = "blink"
  def blink: Unit = {
    val PIN_TO_PWM = 2

    com.pi4j.wiringpi.Gpio.wiringPiSetup()
    SoftPwm.softPwmCreate(PIN_TO_PWM, 0, 100)

    val times = 1 to 4
    val brightness = 1 to 100

    for {
      t <- times
      b <- brightness
    } yield smooth(b)

    for {
      t <- times
      b <- brightness.reverse
    } yield smooth(b)

    def smooth(brightness: Int) = {
      SoftPwm.softPwmWrite(PIN_TO_PWM,brightness)
      delay(100)
    }
  }
}
