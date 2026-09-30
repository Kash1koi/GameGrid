import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Scanner;

/**
 * GameGrid - Real-Time Serial Monitor
 * Reads 3x3 button matrix state from Raspberry Pi Pico via USB serial (jSerialComm)
 * and updates GUI grid display in real-time.
 */
public class SerialDisplay {
    public static void main(String[] args) {
        // 1. Locate available serial ports
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            System.out.println("Error: No serial ports found. Ensure Pico is connected via USB.");
            return;
        }

        // Select the first available port (typically /dev/ttyACM0 on Raspberry Pi)
        SerialPort comPort = ports[0];
        comPort.setBaudRate(115200);

        if (!comPort.openPort()) {
            System.out.println("Error: Failed to open serial port " + comPort.getSystemPortName());
            return;
        }

        System.out.println("Successfully connected to: " + comPort.getSystemPortName());

        // 2. Set up the 3x3 GUI grid using Swing
        JFrame frame = new JFrame("GameGrid - Real-Time Serial Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new GridLayout(3, 3, 10, 10));

        JButton[] gridButtons = new JButton[9];
        for (int i = 0; i < 9; i++) {
            gridButtons[i] = new JButton("Button " + (i + 1));
            gridButtons[i].setFont(new Font("Arial", Font.BOLD, 22));
            gridButtons[i].setBackground(Color.LIGHT_GRAY);
            gridButtons[i].setOpaque(true);
            gridButtons[i].setBorderPainted(false);
            frame.add(gridButtons[i]);
        }

        frame.setVisible(true);

        // 3. Background thread to continuously read serial data packets
        new Thread(() -> {
            InputStream in = comPort.getInputStream();
            Scanner scanner = new Scanner(in);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                // Split comma-separated string packet: e.g. "0,1,0,0,0,0,0,0,0"
                String[] tokens = line.split(",");
                if (tokens.length == 9) {
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < 9; i++) {
                            boolean isPressed = tokens[i].trim().equals("1");
                            if (isPressed) {
                                gridButtons[i].setBackground(Color.GREEN);
                                gridButtons[i].setText("PRESSED (" + (i + 1) + ")");
                            } else {
                                gridButtons[i].setBackground(Color.LIGHT_GRAY);
                                gridButtons[i].setText("Button " + (i + 1));
                            }
                        }
                    });
                }
            }
            comPort.closePort();
        }).start();
    }
}
