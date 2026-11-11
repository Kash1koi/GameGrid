import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MenuScreen implements Screen {
    public static final String DEFAULT_HINT = "Tap a game or press its number";

    private static final Color BG = Color.BLACK;
    private static final Color TILE_BG = new Color(28, 30, 38);
    private static final Color ACCENT = new Color(80, 170, 255);
    private static final Color DANGER = new Color(150, 45, 45);
    private static final Color DANGER_ARMED = new Color(230, 40, 40);
    private static final int KEY_SIZE = 40;   // size of each number square in the controller diagram

    private final App app;
    private final List<Screen> games;
    private final JPanel view = new JPanel(new BorderLayout());
    private final JLabel hint = new JLabel(DEFAULT_HINT, SwingConstants.CENTER);
    private final JLabel shutdownBtn;
    private final Timer disarmTimer;
    private boolean shutdownArmed = false;

    public MenuScreen(App app, List<Screen> games) {
        this.app = app;
        this.games = games;

        disarmTimer = new Timer(3000, e -> disarm());
        disarmTimer.setRepeats(false);

        view.setBackground(BG);
        view.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // title
        JLabel title = new JLabel("Choose a game", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
        title.setForeground(Color.GREEN);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        view.add(title, BorderLayout.NORTH);

        // game tiles, two per row
        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setBackground(BG);
        for (int i = 0; i < games.size() && i < 9; i++) {
            grid.add(makeTile(i, games.get(i).getTitle()));
        }
        view.add(grid, BorderLayout.CENTER);

        // bottom: hint line, then the two buttons
        hint.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        hint.setForeground(Color.GRAY);
        hint.setBorder(BorderFactory.createEmptyBorder(12, 0, 10, 0));

        shutdownBtn = makeButton("Shut down", DANGER);
        JLabel closeBtn = makeButton("Close app", new Color(70, 74, 86));

        shutdownBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (shutdownArmed) {
                    disarm();
                    app.requestShutdown();
                } else {
                    shutdownArmed = true;
                    shutdownBtn.setText("Tap again to confirm");
                    shutdownBtn.setBackground(DANGER_ARMED);
                    disarmTimer.restart();
                }
            }
        });
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                app.requestQuit();
            }
        });

        JPanel buttons = new JPanel(new GridLayout(1, 2, 14, 0));
        buttons.setBackground(BG);
        buttons.add(shutdownBtn);
        buttons.add(closeBtn);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG);
        bottom.add(hint, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.CENTER);
        view.add(bottom, BorderLayout.SOUTH);

        // right side: controller diagram and instructions
        view.add(makeInstructions(), BorderLayout.EAST);
    }

    /** The "how to play" panel: a 3x3 diagram of the controller plus a short explanation. */
    private JPanel makeInstructions() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(BG);
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(60, 62, 74)),
                BorderFactory.createEmptyBorder(0, 20, 0, 0)));
        side.setPreferredSize(new Dimension(230, 0));

        JLabel heading = new JLabel("Controller");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3x3 diagram, laid out like the physical buttons
        JPanel keys = new JPanel(new GridLayout(3, 3, 6, 6));
        keys.setBackground(BG);
        for (int i = 0; i < 9; i++) {
            boolean hasGame = i < games.size();
            JLabel key = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            key.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            key.setOpaque(true);
            key.setForeground(hasGame ? Color.BLACK : Color.GRAY);
            key.setBackground(hasGame ? ACCENT : new Color(40, 42, 52));
            keys.add(key);
        }
        Dimension keyBox = new Dimension(3 * KEY_SIZE + 12, 3 * KEY_SIZE + 12);
        keys.setPreferredSize(keyBox);
        keys.setMaximumSize(keyBox);
        keys.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text = new JLabel("<html><div style='width:190px; text-align:center'>"
                + "Match the number on a game with the same number on the controller, "
                + "then press that button to play."
                + "<br><br>Or just tap the game on the screen."
                + "</div></html>");
        text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        text.setForeground(Color.LIGHT_GRAY);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        side.add(Box.createVerticalGlue());
        side.add(heading);
        side.add(Box.createVerticalStrut(10));
        side.add(keys);
        side.add(Box.createVerticalStrut(12));
        side.add(text);
        side.add(Box.createVerticalGlue());
        return side;
    }

    private JPanel makeTile(int index, String name) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(TILE_BG);
        tile.setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        JLabel num = new JLabel(String.valueOf(index + 1));
        num.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        num.setForeground(ACCENT);
        num.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(name);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        tile.add(Box.createVerticalGlue());
        tile.add(num);
        tile.add(label);
        tile.add(Box.createVerticalGlue());

        app.onTap(tile, index);   // tapping the tile starts that game
        return tile;
    }

    private JLabel makeButton(String text, Color bg) {
        JLabel b = new JLabel(text, SwingConstants.CENTER);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));
        return b;
    }

    /** Put the shutdown button back to normal. Must run on the Swing thread. */
    private void disarm() {
        shutdownArmed = false;
        disarmTimer.stop();
        shutdownBtn.setText("Shut down");
        shutdownBtn.setBackground(DANGER);
    }

    @Override public String getTitle() { return "Menu"; }
    @Override public JComponent getView() { return view; }

    public void setHint(String text) {
        SwingUtilities.invokeLater(() -> hint.setText(text));
    }

    @Override
    public void onShow() {
        SwingUtilities.invokeLater(this::disarm);
        setHint(DEFAULT_HINT);
    }

    @Override
    public void onPress(int index) {
        if (index < games.size()) {
            app.show(games.get(index));
        }
    }
}