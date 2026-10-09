import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {
    private final PicoReader pico = new PicoReader();
    private final List<Screen> games = new ArrayList<>();
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final boolean fullscreen;
    private JFrame frame;
    private Screen menu;
    private Screen current;

    public App(boolean fullscreen) {
        this.fullscreen = fullscreen;
        root.setBackground(Color.BLACK);
    }

    public void addGame(Screen game) {
        games.add(game);
    }

    public void show(Screen s) {
        current = s;
        s.onShow();
        SwingUtilities.invokeLater(() -> cards.show(root, s.getTitle()));
    }

    public void showMenu() {
        show(menu);
    }

    public void run() {
        menu = new MenuScreen(this, games);

        SwingUtilities.invokeLater(() -> {
            root.add(menu.getView(), menu.getTitle());
            for (Screen g : games) {
                root.add(g.getView(), g.getTitle());
            }
            buildFrame();
        });

        Thread t = new Thread(pico);
        t.setDaemon(true);
        t.start();

        showMenu();
        int[] previous = new int[10];

        while (true) {
            int[] data = pico.getLatest();

            if (data.length >= 9) {
                int count = 0;
                int index = -1;
                for (int i = 0; i < 9; i++) {
                    if (data[i] == 1 && previous[i] == 0) {   // new press only
                        count++;
                        index = i;
                    }
                }
                // optional 10th "back" button
                boolean back = data.length > 9 && data[9] == 1 && previous[9] == 0;
                previous = Arrays.copyOf(data, 10);

                if (back) {
                    showMenu();
                } else if (count == 1) {
                    current.onPress(index);
                }
            }

            current.onTick();
            pause(20);
        }
    }

    private void buildFrame() {
        frame = new JFrame("Games");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(root);

        if (fullscreen) {
            frame.setUndecorated(true);
            frame.setAlwaysOnTop(true);
            frame.getRootPane().registerKeyboardAction(
                    e -> System.exit(0),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
            GraphicsDevice screen = GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (screen.isFullScreenSupported()) {
                screen.setFullScreenWindow(frame);
            } else {
                frame.setBounds(screen.getDefaultConfiguration().getBounds());
            }
        } else {
            frame.setSize(400, 440);
            frame.setLocationRelativeTo(null);
        }
        frame.setVisible(true);
    }

    private static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}