import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SimonScreen implements Screen {
    private static final int MAX_ROUNDS = 12;
    private static final long FLASH_MS = 1000;       // how long a square stays dimmed
    private static final long GAP_MS = 300;          // bright pause between dims
    private static final long PRESS_FLASH_MS = 250;  // feedback when you press a square
    private static final int DIM_DIVISOR = 4;        // bigger number = darker dim

    private enum State { SHOWING, INPUT, RESULT }

    // one color per grid square, same order as the buttons (top-left to bottom-right)
    private static final Color[] COLORS = {
        new Color(243, 246, 244),    // white
        new Color(203, 243, 187),   // green
        new Color(255, 162, 89),   // orange
        new Color(46, 51, 50),    // black
        new Color(255, 233, 169),   // yellow
        new Color(255, 86, 86),   // red
        new Color(170, 173, 172),   // grey
        new Color(146, 238, 255),  // blue
        new Color(255, 190, 251)   // purple
    };

    private final App app;
    private final Random rnd = new Random();
    private final JPanel view = new JPanel(new BorderLayout());
    private final JLabel[] cells = new JLabel[9];
    private final JLabel status = new JLabel("", SwingConstants.CENTER);

    private final int[] sequence = new int[MAX_ROUNDS];
    private State state = State.RESULT;
    private int round;        // how many squares the player must repeat this round
    private int showIndex;    // which square of the sequence is being shown
    private boolean dimmed;   // true while a square is currently dimmed
    private long phaseEnd;
    private int inputIndex;   // how many the player has entered correctly this round
    private boolean pressDim;
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
            cell.setBackground(COLORS[i]);   // bright by default
            cells[i] = cell;
            app.onTap(cell, i);
            board.add(cell);
        }

        status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        status.setOpaque(true);
        status.setBackground(Color.BLACK);
        status.setForeground(Color.WHITE);
        status.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        view.add(board, BorderLayout.CENTER);
        view.add(status, BorderLayout.NORTH);
        view.add(app.backButton(), BorderLayout.SOUTH);
    }

    @Override public String getTitle() { return "Simon Says"; }
    @Override public JComponent getView() { return view; }

    @Override
    public void onShow() {
        for (int i = 0; i < MAX_ROUNDS; i++) {
            sequence[i] = rnd.nextInt(9);
        }
        round = 1;
        pressDim = false;
        setDim(-1);
        startShowing(1200);
    }

    @Override
    public void onPress(int index) {
        if (state != State.INPUT) return;   // ignore presses while squares are showing

        long now = System.currentTimeMillis();
        setDim(index);                       // the pressed square dims briefly
        pressDim = true;
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
            setStatus("You win! " + MAX_ROUNDS + " in a row!");
        } else {
            round++;
            startShowing(1200);
        }
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();

        if (pressDim && now >= pressEnd) {
            pressDim = false;
            setDim(-1);
        }

        if (state == State.SHOWING) {
            if (now < phaseEnd) return;

            if (!dimmed) {
                if (showIndex >= round) {
                    state = State.INPUT;
                    inputIndex = 0;
                    setStatus("Your turn: 0 of " + round);
                    return;
                }
                setDim(sequence[showIndex]);   // dim the next square in the sequence
                dimmed = true;
                phaseEnd = now + FLASH_MS;
            } else {
                setDim(-1);                    // back to all bright
                dimmed = false;
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
        dimmed = false;
        phaseEnd = System.currentTimeMillis() + delayMs;
        setStatus("Round " + round + " of " + MAX_ROUNDS + " - watch");
    }

    /** idx 0-8 dims that square (all others stay bright), -1 leaves everything bright. */
    private void setDim(int idx) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 9; i++) {
                cells[i].setBackground(i == idx ? dim(COLORS[i]) : COLORS[i]);
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
        return new Color(c.getRed() / DIM_DIVISOR,
                         c.getGreen() / DIM_DIVISOR,
                         c.getBlue() / DIM_DIVISOR);
    }
}