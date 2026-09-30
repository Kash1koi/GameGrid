import machine
import time

# Define the 9 GPIO pins connected to your buttons
# Assuming buttons are wired between the GPIO pin and GND
pin_numbers = [2, 3, 4, 5, 6, 7, 8, 9, 10]

# Initialize pins with internal pull-up resistors
buttons = [machine.Pin(p, machine.Pin.IN, machine.Pin.PULL_UP) for p in pin_numbers]

while True:
    # Read states (0 = pressed if wired to GND, 1 = unpressed)
    # We invert it here so 1 = pressed, 0 = unpressed for easier Java logic
    states = [str(1 if btn.value() == 0 else 0) for btn in buttons]
    
    # Send data as: "0,1,0,0,0,0,0,0,0" followed by a newline
    print(",".join(states))
    
    # Poll at 20Hz to prevent flooding the serial buffer
    time.sleep(0.05)
