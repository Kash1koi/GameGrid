import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PicoReader {
    public static void main(String[] args) throws Exception {
        SerialPort port = SerialPort.getCommPort("/dev/ttyACM0");
        port.setBaudRate(115200);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        if (!port.openPort()) {
            System.out.println("Could not open port");
            return;
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(port.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                String[] parts = line.trim().split(",");
                int[] values = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    values[i] = Integer.parseInt(parts[i]);
                }
                // use values[] as your inputs here
                System.out.println(java.util.Arrays.toString(values));
            } catch (NumberFormatException e) {
                // skip partial/garbled lines
            }
        }
        port.closePort();
    }
}
