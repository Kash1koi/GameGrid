import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PicoReader implements Runnable {
    private final String device;
    private volatile int[] latest = new int[0];

    public PicoReader() {
        this("/dev/ttyACM0");
    }

    public PicoReader(String device) {
        this.device = device;
    }

    public int[] getLatest() {
        return latest;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (!new File(device).exists()) {
                    System.out.println("Waiting for " + device + " ...");
                    Thread.sleep(2000);
                    continue;
                }
                try {
                    configurePort();
                    System.out.println("Reading from " + device);
                    readLoop();
                } catch (IOException e) {
                    System.out.println("Serial error: " + e.getMessage() + " - retrying...");
                    latest = new int[0];
                    Thread.sleep(2000);
                }
            }
        } catch (InterruptedException e) {
            // thread was asked to stop
        }
    }

    /** Put the tty in raw mode so the OS doesn't mangle the data. */
    private void configurePort() throws IOException, InterruptedException {
        new ProcessBuilder("stty", "-F", device, "115200", "raw", "-echo")
                .inheritIO()
                .start()
                .waitFor();
    }

    private void readLoop() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(device))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 9 || parts.length > 10) continue; // ignore partial lines

                try {
                    int[] values = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        values[i] = Integer.parseInt(parts[i].trim());
                    }
                    latest = transpose(values);
                } catch (NumberFormatException e) {
                    // skip garbled line
                }
            }
        }
        throw new IOException("Serial port closed (Pico unplugged?)");
    }

    /**
     * Swap rows and columns of the first 9 values (the 3x3 grid).
     * A 10th value, if present, stays where it is.
     */
    private static int[] transpose(int[] v) {
        int[] out = v.clone();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                out[row * 3 + col] = v[col * 3 + row];
            }
        }
        return out;
    }
}