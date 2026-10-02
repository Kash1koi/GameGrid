package OtherCode;
import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Minimal Swing app: a window whose contents update in response to a button
 * press (and on a repeating timer, which is usually what you want on a Pi
 * that's driving an always-on display).
 *
 * Build & run:
 *   javac DashboardApp.java
 *   java DashboardApp
 *
 * Fullscreen / kiosk mode (for the Pi):
 *   java -DfullScreen=true DashboardApp
 */
public class DashboardApp extends JFrame {

    // --- State: keep all mutable app state in one place. -------------------
    private int counter = 0;
    private boolean armed = false;

    // --- Widgets we need to touch later. -----------------------------------
    private final JLabel countLabel  = new JLabel("0", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("IDLE", SwingConstants.CENTER);
    private final JLabel clockLabel  = new JLabel("--:--:--", SwingConstants.CENTER);
    private final JLabel hintLabel   = new JLabel(
            "SPACE / \u2191 count up   \u2193 count down   A arm   R reset   ESC quit",
            SwingConstants.CENTER);

    private static final DateTimeFormatter TIME_FMT =  
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public DashboardApp() {
        super("Pi Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        buildUi();
        installKeyBindings();
        startClock();
        render();               // draw the initial state

        if (Boolean.getBoolean("fullScreen")) {
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            // Hide the mouse pointer -- nice touch on a kiosk display.
            setCursor(getToolkit().createCustomCursor(
                    new java.awt.image.BufferedImage(1, 1,
                            java.awt.image.BufferedImage.TYPE_INT_ARGB),
                    new Point(0, 0), "blank"));
        } else {
            setSize(640, 400);
            setLocationRelativeTo(null);   // center on screen
        }
    }

    private void buildUi() {
        clockLabel.setFont(clockLabel.getFont().deriveFont(Font.PLAIN, 20f));
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.PLAIN, 13f));
        hintLabel.setForeground(Color.GRAY);
        add(clockLabel, BorderLayout.NORTH);

        // Center: the big readout.
        JPanel center = new JPanel(new GridLayout(2, 1, 8, 8));
        countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD, 96f));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 28f));
        center.add(countLabel);
        center.add(statusLabel);
        add(center, BorderLayout.CENTER);

        // Bottom: the buttons.
        JButton incrementBtn = bigButton("Press me");
        JButton toggleBtn    = bigButton("Arm / Disarm");
        JButton resetBtn     = bigButton("Reset");

        // Buttons and key bindings both call the SAME methods, so the two
        // input paths can never drift apart.
        incrementBtn.addActionListener(e -> doIncrement());
        toggleBtn.addActionListener(e -> doToggleArmed());
        resetBtn.addActionListener(e -> doReset());

        JPanel buttons = new JPanel(new GridLayout(1, 3, 12, 0));
        buttons.add(incrementBtn);
        buttons.add(toggleBtn);
        buttons.add(resetBtn);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.add(buttons, BorderLayout.CENTER);
        south.add(hintLabel, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    // --- The actions themselves. One method per thing the app can do. ------

    private void doIncrement() {
        counter++;
        render();
    }

    private void doDecrement() {
        counter--;
        render();
    }

    private void doToggleArmed() {
        armed = !armed;
        render();
    }

    private void doReset() {
        counter = 0;
        armed = false;
        render();
    }

    /**
     * KEYBOARD INPUT.
     *
     * Every JComponent carries two lookup tables:
     *
     *   InputMap  : KeyStroke  -> a name (any Object; a String by convention)
     *   ActionMap : that name  -> an Action to execute
     *
     * A keypress walks the focused component's maps, then its ancestors', then
     * every WHEN_IN_FOCUSED_WINDOW map in the window. We register on the root
     * pane with WHEN_IN_FOCUSED_WINDOW, which means: fire whenever this window
     * is active, no matter which widget currently holds focus. That matters
     * here because clicking a JButton gives that button focus -- a plain
     * KeyListener on the frame would go silent the moment you clicked
     * anything.
     */
    private void installKeyBindings() {
        JComponent root = getRootPane();
        InputMap  in  = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap act = root.getActionMap();

        bind(in, act, "SPACE",  "increment", this::doIncrement);
        bind(in, act, "UP",     "increment", this::doIncrement);
        bind(in, act, "DOWN",   "decrement", this::doDecrement);
        bind(in, act, "A",      "arm",       this::doToggleArmed);
        bind(in, act, "R",      "reset",     this::doReset);
        bind(in, act, "ESCAPE", "quit",      () -> dispose());

        // Modifier keys use the same string syntax:
        //   "control S", "shift F1", "alt ENTER", "typed x"
        // Or build one programmatically:
        //   KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)
        //
        // The default is key-PRESSED. For key-released, use "released SPACE".
    }

    /** Wires one keystroke to one zero-argument action. */
    private void bind(InputMap in, ActionMap act,
                      String keyStroke, String name, Runnable body) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        if (ks == null) {
            throw new IllegalArgumentException("Bad keystroke: " + keyStroke);
        }
        in.put(ks, name);                       // keystroke -> name
        act.put(name, new AbstractAction() {    // name -> action
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                body.run();
            }
        });
    }

    private JButton bigButton(String text) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 22f));
        b.setPreferredSize(new Dimension(160, 72));  // finger-sized for touch
        b.setFocusPainted(false);
        return b;
    }

    /**
     * Single place that maps state -> what's on screen. Every handler mutates
     * state and then calls this, so you never have half-updated UI.
     */
    private void render() {
        countLabel.setText(String.valueOf(counter));
        statusLabel.setText(armed ? "ARMED" : "IDLE");
        statusLabel.setForeground(armed ? new Color(0xC0392B) : new Color(0x2E7D32));
    }

    /**
     * javax.swing.Timer fires on the Event Dispatch Thread, so it's safe to
     * touch widgets directly from here. (java.util.Timer is NOT safe for this.)
     */
    private void startClock() {
        new Timer(1000, e -> clockLabel.setText(LocalTime.now().format(TIME_FMT)))
                .start();
    }

    /**
     * Example of updating the UI from a background thread -- e.g. a GPIO
     * reader or a sensor poll. Never touch Swing widgets off the EDT; hop back
     * onto it with invokeLater.
     */
    @SuppressWarnings("unused")
    private void updateFromBackgroundThread(int newValue) {
        SwingUtilities.invokeLater(() -> {
            counter = newValue;
            render();
        });
    }

    public static void main(String[] args) {
        // All Swing construction happens on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new DashboardApp().setVisible(true));
    }
}