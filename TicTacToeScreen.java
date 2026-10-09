import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class TicTacToeScreen implements Screen {
    private static final Color CELL_BG = Color.BLACK;
    private static final Color LINE_COLOR = Color.WHITE;
    private static final Color X_COLOR = new Color(80, 170, 255);
    private static final Color O_COLOR = new Color(255, 120, 80);

    private final App app;
    private final button[] buttons = new button[9];
    private final JPanel view = new JPanel(new BorderLayout());
    private final JLabel[][] cells = new JLabel[3][3];
    private final JLabel status = new JLabel("", SwingConstants.CENTER);

    private tik toe = new tik();
    private boolean over = false;
    private long returnAt = 0;

    public TicTacToeScreen(App app) {
        this.app = app;
        for (int i = 0; i < 9; i++) {
            buttons[i] = new button(i % 3, i / 3);   // x = column, y = row
        }

        view.setBackground(Color.BLACK);
        JPanel board = new JPanel(new GridLayout(3, 3, 10, 10));
        board.setBackground(LINE_COLOR);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JLabel cell = new JLabel("", SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBackground(CELL_BG);
                cells[row][col] = cell;
                board.add(cell);
            }
        }
        board.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int size = Math.min(board.getWidth(), board.getHeight()) / 5;
                Font f = new Font(Font.SANS_SERIF, Font.BOLD, Math.max(size, 12));
                for (JLabel[] r : cells) for (JLabel c : r) c.setFont(f);
            }
        });

        status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        status.setOpaque(true);
        status.setBackground(Color.BLACK);
        status.setForeground(Color.WHITE);
        status.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        view.add(board, BorderLayout.CENTER);
        view.add(status, BorderLayout.NORTH);
    }

    @Override public String getTitle() { return "Tic Tac Toe"; }
    @Override public JComponent getView() { return view; }

    @Override
    public void onShow() {
        toe = new tik();
        tik.turn = "X";
        over = false;
        draw();
        setStatus("X's turn");
    }

    @Override
    public void onPress(int index) {
        if (over) return;

        if (toe.update(buttons[index])) {
            draw();
            if (toe.checkWin()) {
                String winner = tik.turn.equals("X") ? "O" : "X";
                setStatus(winner + " wins!");
                finish();
            } else if (toe.isGridFull()) {
                setStatus("It's a draw!");
                finish();
            } else {
                setStatus(tik.turn + "'s turn");
            }
        } else {
            setStatus("Square taken! " + tik.turn + "'s turn");
        }
    }

    @Override
    public void onTick() {
        if (over && System.currentTimeMillis() >= returnAt) {
            app.showMenu();   // or: onShow(); to play again instead
        }
    }

    private void finish() {
        over = true;
        returnAt = System.currentTimeMillis() + 4000;
    }

    private void draw() {
        String[][] g = toe.getGrid();
        String[][] copy = new String[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                copy[r][c] = g[r][c];
            }
        }
        SwingUtilities.invokeLater(() -> {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    cells[r][c].setText(copy[r][c]);
                    cells[r][c].setForeground(copy[r][c].equals("X") ? X_COLOR : O_COLOR);
                }
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
}