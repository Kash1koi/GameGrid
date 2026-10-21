import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SimonScreen implements Screen {
    private static final int MAX_ROUNDS = 12;
    private static final long FLASH_MS = 1000;       // how long each color stays lit
    private static final long GAP_MS = 300;          // dark pause between flashes
    private static final long PRESS_FLASH_MS = 250;  // feedback when you press a button

    private enum State { SHOWING, INPUT, RESULT }

    // one color per grid square, same order as the buttons (top-left to bottom-right)
    private static final Color[] COLORS = {
        new Color(230, 60, 60),    // red
        new Color(255, 150, 30),   // orange
        new Color(240, 220, 50),   // yellow
        new Color(70, 200, 90),    // green
        new Color(50, 200, 190),   // teal
        new Color(70, 130, 255),   // blue
        new Color(160, 90, 230),   // purple
        new Color(240, 110, 190),  // pink
        new Color(235, 235, 235)   // white
    };

    private final App app;
    private final Random rnd = new Random();
    private final JPanel view = new JPanel(new BorderLayout());
    private final JLabel[] cells = new JLabel[9];
    private final JLabel status = new JLabel("", SwingConstants.CENTER);

    private final int[] sequence = new int[MAX_ROUNDS];
    private State state = State.RESULT;
    private int round;        // how many colors the player must repeat this round
    private int showIndex;    // which color of the sequence is being shown
    private boolean lit;      // true while a color is currently lit
    private long phaseEnd;
    private int inputIndex;   // how many the player has entered correctly this round
    private boolean pressLit;
    private long pressEnd;
    private long returnAt;

    public SimonScreen(App app) {
        this.app = app;

        view.setBackground(Color.BLACK);
        JPanel board = new JPanel(new GridLayout(3, 3, 8, 8));
        board.setBackground(Color.BLACK);
        for (int i = 0; i < 9; i++) {
            JLabel cell = new JLabel("", SwingConstants.CENTER);
            cell.setOpaque(true);
            cell.setBackground(dim(COLORS[i]));
            cells[i] = cell;
            board.add(cell);
        }

        status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        status.setOpaque(true);
        status.setBackground(Color.BLACK);
        status.setForeground(Color.WHITE);
        status.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        view.add(board, BorderLayout.CENTER);
        view.add(status, BorderLayout.NORTH);
    }

    @Override public String getTitle() { return "Simon Says"; }
    @Override public JComponent getView() { return view; }

    @Override
    public void onShow() {
        for (int i = 0; i < MAX_ROUNDS; i++) {
            sequence[i] = rnd.nextInt(9);
        }
        round = 1;
        pressLit = false;
        setLit(-1);
        startShowing(1200);
    }

    @Override
    public void onPress(int index) {
        if (state != State.INPUT) return;   // ignore presses while colors are showing

        long now = System.currentTimeMillis();
        setLit(index);
        pressLit = true;
        pressEnd = now + PRESS_FLASH_MS;

        if (index != sequence[inputIndex]) {
            state = State.RESULT;
            returnAt = now + 4000;
            setStatus("Wrong! You got " + (round - 1) + " in a row");
            return;
        }

        inputIndex++;
        if (inputIndex < round) {
            setStatus("Your turn: " + inputIndex + " of " + round);
        } else if (round == MAX_ROUNDS) {
            state = State.RESULT;
            returnAt = now + 5000;
            pressLit = false;
            setLit(-2);   // light every square to celebrate
            setStatus("You win! " + MAX_ROUNDS + " in a row!");
        } else {
            round++;
            startShowing(1200);
        }
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();

        if (pressLit && now >= pressEnd) {
            pressLit = false;
            setLit(-1);
        }

        if (state == State.SHOWING) {
            if (now < phaseEnd) return;

            if (!lit) {
                if (showIndex >= round) {
                    state = State.INPUT;
                    inputIndex = 0;
                    setStatus("Your turn: 0 of " + round);
                    return;
                }
                setLit(sequence[showIndex]);
                lit = true;
                phaseEnd = now + FLASH_MS;
            } else {
                setLit(-1);
                lit = false;
                showIndex++;
                phaseEnd = now + GAP_MS;
            }
        } else if (state == State.RESULT && now >= returnAt) {
            app.showMenu();
        }
    }

    private void startShowing(long delayMs) {
        state = State.SHOWING;
        showIndex = 0;
        lit = false;
        phaseEnd = System.currentTimeMillis() + delayMs;
        setStatus("Round " + round + " of " + MAX_ROUNDS + " - watch");
    }

    /** idx 0-8 lights that square, -1 lights none, -2 lights all. */
    private void setLit(int idx) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 9; i++) {
                boolean on = (idx == -2) || (idx == i);
                cells[i].setBackground(on ? COLORS[i] : dim(COLORS[i]));
            }
            view.revalidate();
            view.repaint();
            Toolkit.getDefaultToolkit().sync();
        });
    }

    private void setStatus(String text) {
        SwingUtilities.invokeLater(() -> {
            status.setText(text);
            status.repaint();
            Toolkit.getDefaultToolkit().sync();
        });
    }

    private static Color dim(Color c) {
        return new Color(c.getRed() / 4, c.getGreen() / 4, c.getBlue() / 4);
    }
}