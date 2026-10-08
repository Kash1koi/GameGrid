"""
GameGrid controller - Raspberry Pi Pico W (MicroPython)

  Pico -> Pi 4 : button states, one line every REPORT_MS (10 Hz), e.g. "0,1,0,0,0,0,0,0,0"
                 The motor buzzes on every press (BUZZ_ON_PRESS).
                 Replies to typed commands start with "#", e.g. "# ok"
  Pi 4 -> Pico : newline-terminated text commands to drive the DRV2605L haptic driver

Save this on the Pico as main.py.

Commands (case-insensitive):
  short | medium | long       timed buzz (durations set in TIMED below)
  click | soft | sharp | tick | double | triple | fuzz | pulse | alert | alertlong
                              built-in DRV2605L effects (see EFFECTS below)
  buzz <ms> [0-255]           custom buzz: duration in ms, optional intensity
  fx <id|wMS> ...             up to 8 raw DRV2605L effect IDs (1-123); wMS = wait,
                              e.g.  fx 1 w100 1 w100 14
  stop                        stop any vibration
  ping                        replies "# pong"
  list                        replies with all preset names
"""

import sys
import time
import select
import machine

# ---------------------------------------------------------------------------
# Hardware config (from the GameGrid schematic)
# ---------------------------------------------------------------------------
BUTTON_PINS = [0, 1, 2, 3, 4, 5, 6, 7, 8]  # SW1..SW9 -> GP0..GP8, wired to GND
I2C_ID = 0
SDA_PIN = 16                               # GP16 (pin 21)
SCL_PIN = 17                               # GP17 (pin 22)

MOTOR_TYPE = "ERM"      # "ERM" (coin / eccentric mass) or "LRA"
BUZZ_ON_PRESS = "short" # buzz the motor on every button press; None to disable
                        # (any preset name: short, medium, long, click, tick, ...)
REPORT_MS = 100         # print exactly one button line every 100 ms (10 Hz)
DEBUG_MESSAGES = False  # True = also print "# ..." status lines (boot message etc.)
MAX_BUZZ_MS = 3000      # safety cap for the buzz command

# Timed buzz presets (milliseconds)
TIMED = {
    "short": 100,
    "medium": 250,
    "long": 600,
}

# Effect presets -> DRV2605L waveform library IDs
EFFECTS = {
    "click": [1],        # Strong click 100%
    "soft": [7],         # Soft bump 100%
    "sharp": [4],        # Sharp click 100%
    "tick": [24],        # Sharp tick 100%
    "double": [10],      # Double click 100%
    "triple": [12],      # Triple click 100%
    "fuzz": [13],        # Soft fuzz 60%
    "pulse": [52],       # Pulsing strong 100%
    "alert": [15],       # 750 ms alert
    "alertlong": [16],   # 1000 ms alert
}

# ---------------------------------------------------------------------------
# DRV2605L driver (minimal)
# ---------------------------------------------------------------------------
DRV_ADDR = 0x5A
REG_MODE = 0x01
REG_RTP = 0x02
REG_LIBRARY = 0x03
REG_WAVESEQ1 = 0x04
REG_GO = 0x0C
REG_FEEDBACK = 0x1A
REG_CONTROL3 = 0x1D

MODE_INTTRIG = 0x00   # play waveform sequencer on GO
MODE_RTP = 0x05       # real-time playback (direct intensity)


class DRV2605L:
    def __init__(self, i2c, lra=False):
        self.i2c = i2c
        self.lra = lra
        self.ok = False
        self._rtp_end = None

    def _write(self, reg, val):
        self.i2c.writeto_mem(DRV_ADDR, reg, bytes([val & 0xFF]))

    def _read(self, reg):
        return self.i2c.readfrom_mem(DRV_ADDR, reg, 1)[0]

    def begin(self):
        try:
            if DRV_ADDR not in self.i2c.scan():
                self.ok = False
                return False

            self._write(REG_MODE, MODE_INTTRIG)   # leave standby
            self._write(REG_RTP, 0)
            self._write(REG_WAVESEQ1, 1)
            self._write(REG_WAVESEQ1 + 1, 0)

            feedback = self._read(REG_FEEDBACK)
            control3 = self._read(REG_CONTROL3) | 0x08   # RTP value is unsigned 0-255

            if self.lra:
                self._write(REG_FEEDBACK, feedback | 0x80)   # N_ERM_LRA = LRA
                self._write(REG_LIBRARY, 6)                  # LRA library
            else:
                self._write(REG_FEEDBACK, feedback & 0x7F)   # ERM
                control3 |= 0x20                             # ERM open loop
                self._write(REG_LIBRARY, 1)                  # ERM open-loop library

            self._write(REG_CONTROL3, control3)
            self.ok = True
        except OSError:
            self.ok = False
        return self.ok

    def stop(self):
        self._rtp_end = None
        self._write(REG_GO, 0)
        self._write(REG_RTP, 0)
        self._write(REG_MODE, MODE_INTTRIG)

    def play(self, effects):
        """Play up to 8 waveform IDs (1-123) or waits (0x80 | ms/10)."""
        self.stop()
        seq = list(effects)[:8]
        for i, e in enumerate(seq):
            self._write(REG_WAVESEQ1 + i, e)
        if len(seq) < 8:
            self._write(REG_WAVESEQ1 + len(seq), 0)   # end marker
        self._write(REG_GO, 1)

    def buzz(self, ms, intensity=255):
        """Non-blocking timed buzz; update() ends it."""
        self._write(REG_GO, 0)
        self._write(REG_MODE, MODE_RTP)
        self._write(REG_RTP, intensity)
        self._rtp_end = time.ticks_add(time.ticks_ms(), ms)

    def update(self):
        if self._rtp_end is not None and time.ticks_diff(time.ticks_ms(), self._rtp_end) >= 0:
            self.stop()


# ---------------------------------------------------------------------------
# Command handling
# ---------------------------------------------------------------------------
def reply(msg):
    """Status/reply line. Only printed for typed commands, or when DEBUG_MESSAGES is on."""
    print("# " + msg)


def handle(line, haptic):
    line = line.strip().lower()
    if not line or line[0] == "#":
        return                      # ignore blanks and our own echoed replies
    if line[0] in "01" and "," in line:
        return                      # ignore echoed button-state lines

    parts = line.split()
    cmd, args = parts[0], parts[1:]

    try:
        if cmd == "ping":
            reply("pong")
        elif cmd == "list":
            reply("presets: " + " ".join(sorted(list(TIMED) + list(EFFECTS))))
        elif not haptic.ok:
            reply("err haptic driver not found")
        elif cmd == "stop":
            haptic.stop()
            reply("ok")
        elif cmd == "buzz":
            ms = max(1, min(int(args[0]), MAX_BUZZ_MS))
            intensity = max(0, min(int(args[1]), 255)) if len(args) > 1 else 255
            haptic.buzz(ms, intensity)
            reply("ok")
        elif cmd == "fx":
            ids = []
            for tok in args[:8]:
                if tok[0] == "w":
                    ids.append(0x80 | max(1, min(int(tok[1:]) // 10, 127)))
                else:
                    n = int(tok)
                    if not 1 <= n <= 123:
                        raise ValueError("effect id 1-123")
                    ids.append(n)
            if not ids:
                raise ValueError("fx needs ids")
            haptic.play(ids)
            reply("ok")
        elif cmd in TIMED:
            haptic.buzz(TIMED[cmd])
            reply("ok")
        elif cmd in EFFECTS:
            haptic.play(EFFECTS[cmd])
            reply("ok")
        else:
            reply("err unknown command: " + cmd)
    except (ValueError, IndexError):
        reply("err bad arguments for " + cmd)
    except OSError:
        reply("err i2c failure")


# ---------------------------------------------------------------------------
# Setup
# ---------------------------------------------------------------------------
buttons = [machine.Pin(p, machine.Pin.IN, machine.Pin.PULL_UP) for p in BUTTON_PINS]

i2c = machine.I2C(I2C_ID, sda=machine.Pin(SDA_PIN), scl=machine.Pin(SCL_PIN), freq=400000)
haptic = DRV2605L(i2c, lra=(MOTOR_TYPE == "LRA"))
if haptic.begin():
    if DEBUG_MESSAGES:
        reply("DRV2605L ready (" + MOTOR_TYPE + ")")
elif DEBUG_MESSAGES:
    reply("DRV2605L NOT found - check wiring/power")

poll = select.poll()
poll.register(sys.stdin, select.POLLIN)

def buzz_preset(name):
    """Fire a preset (timed buzz or built-in effect) without ever crashing the loop."""
    if not haptic.ok:
        return
    try:
        if name in TIMED:
            haptic.buzz(TIMED[name])
        elif name in EFFECTS:
            haptic.play(EFFECTS[name])
    except OSError:
        pass


rx = ""
prev_scan = None
last_report = time.ticks_ms()

# ---------------------------------------------------------------------------
# Main loop
# ---------------------------------------------------------------------------
while True:
    # 1 = pressed (buttons pull to GND), 0 = released
    states = [1 if b.value() == 0 else 0 for b in buttons]
    now = time.ticks_ms()

    # Buzz when any button goes from released -> pressed (fast scan, so no lag)
    if BUZZ_ON_PRESS and prev_scan is not None:
        for cur, prev in zip(states, prev_scan):
            if cur and not prev:
                buzz_preset(BUZZ_ON_PRESS)
                break
    prev_scan = states

    # Output: exactly one line every REPORT_MS, e.g. "0,1,0,0,0,0,0,0,0"
    if time.ticks_diff(now, last_report) >= REPORT_MS:
        print(",".join(str(s) for s in states))
        last_report = now

    # Read any commands coming from the Pi 4
    n = 0
    while n < 64 and poll.poll(0):
        ch = sys.stdin.read(1)
        n += 1
        if ch in ("\n", "\r"):
            if rx:
                handle(rx, haptic)
                rx = ""
        elif len(rx) < 80:
            rx += ch

    haptic.update()
    time.sleep_ms(5)
