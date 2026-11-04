import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class App {
    // special codes sent through the touch queue
    private static final int BACK = -1;
    private static final int SHUTDOWN = -2;
    private static final int QUIT = -3;

    private final PicoReader pico = new PicoReader();
    private final List<Screen> games = new ArrayList<>();
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final ConcurrentLinkedQueue<Integer> touchQueue = new ConcurrentLinkedQueue<>();
    private final boolean fullscreen;
    private JFrame frame;
    private MenuScreen menu;
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

    /** Make a component act like button `index` (0-8) when tapped. */
    public void onTap(JComponent c, int index) {
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                touchQueue.add(index);
            }
        });
    }

    /** A "< Menu" strip that returns to the menu when tapped. */
    public JComponent backButton() {
        JLabel b = new JLabel("< Menu", SwingConstants.CENTER);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        b.setForeground(Color.GRAY);
        b.setOpaque(true);
        b.setBackground(Color.BLACK);
        b.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                touchQueue.add(BACK);
            }
        });
        return b;
    }

    public void requestShutdown() {
        touchQueue.add(SHUTDOWN);
    }

    public void requestQuit() {
        touchQueue.add(QUIT);
    }

    private void shutdownPi() {
        try {
            new ProcessBuilder("sudo", "-n", "/usr/sbin/shutdown", "-h", "now").inheritIO().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        long comboStart = 0;   // timer for holding buttons 6 + 9 (shutdown)
        long quitStart = 0;    // timer for holding buttons 7 + 8 (quit)
        boolean shuttingDown = false;

        while (true) {
            int[] data = pico.getLatest();

            // hold buttons 6 + 9 on the menu for 2 seconds: shut down
            boolean combo = data.length >= 9 && data[5] == 1 && data[8] == 1 && current == menu;
            if (combo) {
                long now = System.currentTimeMillis();
                if (comboStart == 0) {
                    comboStart = now;
                    menu.setHint("Keep holding to shut down...");
                } else if (!shuttingDown && now - comboStart >= 2000) {
                    shuttingDown = true;
                    menu.setHint("Shutting down...");
                    shutdownPi();
                }
            } else if (comboStart != 0) {
                comboStart = 0;
                if (!shuttingDown) {
                    menu.setHint(MenuScreen.DEFAULT_HINT);
                }
            }

            // hold buttons 7 + 8 on any screen for 2 seconds: quit the program
            boolean quitCombo = data.length >= 9 && data[6] == 1 && data[7] == 1;
            if (quitCombo) {
                long now = System.currentTimeMillis();
                if (quitStart == 0) {
                    quitStart = now;
                } else if (now - quitStart >= 2000) {
                    System.exit(0);
                }
            } else {
                quitStart = 0;
            }

            // new presses from the Pico buttons
            if (data.length >= 9) {
                int count = 0;
                int index = -1;
                for (int i = 0; i < 9; i++) {
                    if (data[i] == 1 && previous[i] == 0) { // new press only
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

            // taps from the touch screen
            Integer tap;
            while ((tap = touchQueue.poll()) != null) {
                if (tap == BACK) {
                    showMenu();
                } else if (tap == QUIT) {
                    System.exit(0);
                } else if (tap == SHUTDOWN) {
                    if (!shuttingDown) {
                        shuttingDown = true;
                        menu.setHint("Shutting down...");
                        shutdownPi();
                    }
                } else {
                    current.onPress(tap);
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