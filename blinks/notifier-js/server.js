var erlastic = require('node_erlastic');
var ws281x = require('rpi-ws281x-native');

function handle(term, from, state, done) {
    if (term == "blink") return done("noreply",blink());
    if (term == "say") return done("reply","not blinked");
    throw new Error("unexpected request");
}

function blink(){
  var NUM_LEDS = parseInt(process.argv[2], 10) || 8,
      pixelData = new Uint32Array(NUM_LEDS);
  var brightness = 128;

  ws281x.init(NUM_LEDS);

  var lightsOff = function () {
    for (var i = 0; i < NUM_LEDS; i++) {
      pixelData[i] = color(0, 0, 0);
    }
    ws281x.render(pixelData);
    ws281x.reset();
  };

  // ---- animation-loop
  var offset = 0;
  var times = 0;
  var animation = setInterval(function () {
      for (var i = 0; i < NUM_LEDS; i++) {
          pixelData[i] = wheel(((i * 256 / NUM_LEDS) + offset) % 256);
      }

      offset = (offset + 1) % 256;
      ws281x.render(pixelData);
      if (++times === 64) {
          clearInterval(animation);
          lightsOff();
      }
  }, 1000 / 30);

  // generate rainbow colors accross 0-255 positions.
  function wheel(pos) {
    pos = 255 - pos;
    if (pos < 85) { return color(255 - pos * 3, 0, pos * 3); }
    else if (pos < 170) { pos -= 85; return color(0, pos * 3, 255 - pos * 3); }
    else { pos -= 170; return color(pos * 3, 255 - pos * 3, 0); }
  }

  // generate integer from RGB value
  function color(r, g, b) {
    r = r * brightness / 255;
    g = g * brightness / 255;
    b = b * brightness / 255;
    return ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
  }
}

erlastic.server(handle);
