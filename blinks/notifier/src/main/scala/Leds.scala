import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinState.{LOW => OFF}
import com.pi4j.io.gpio.PinState.{HIGH => ON}
import com.pi4j.io.gpio.RaspiPin
import com.pi4j.io.gpio.GpioPinDigitalOutput

object Gpio {

  private val gpio = GpioFactory.getInstance();

  lazy val leds = List(
      gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "red", OFF),
      gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "green", OFF),
      gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "blue", OFF)
  )
}

