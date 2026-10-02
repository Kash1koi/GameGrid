import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PicoReader implements Runnable {
    private volatile int[] latest = new int[0];

    public int[] getLatest() {
        return latest;
    }

    @Override
    public void run() {
        SerialPort port = SerialPort.getCommPort("/dev/ttyACM0");
        port.setBaudRate(115200);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        if (!port.openPort()) {
            System.out.println("Could not open port");
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(port.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.trim().split(",");
                    int[] values = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        values[i] = Integer.parseInt(parts[i]);
                    }
                    latest = values;  // replaces the old array each time
                } catch (NumberFormatException e) {
                    // skip partial lines
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            port.closePort();
        }
    }
}