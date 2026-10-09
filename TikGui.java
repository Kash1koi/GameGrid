import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

public class TikGui {
    private JFrame frame;
    private JLabel[][] cells = new JLabel[3][3];
    private JLabel status;

    // inverted colors: black squares, white grid lines
    private static final Color CELL_BG = Color.BLACK;
    private static final Color LINE_COLOR = Color.WHITE;
    private static final Color X_COLOR = new Color(80, 170, 255);
    private static final Color O_COLOR = new Color(255, 120, 80);

    public TikGui(boolean fullscreen) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Tic Tac Toe");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.getContentPane().setBackground(Color.BLACK);

            JPanel board = new JPanel(new GridLayout(3, 3, 10, 10));
            board.setBackground(LINE_COLOR);   // shows through the gaps as grid lines
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    JLabel cell = new JLabel("", SwingConstants.CENTER);
                    cell.setOpaque(true);
                    cell.setBackground(CELL_BG);
                    cells[row][col] = cell;
                    board.add(cell);
                }
            }
            // scale the X/O size to the window
            board.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int size = Math.min(board.getWidth(), board.getHeight()) / 5;
                    Font f = new Font(Font.SANS_SERIF, Font.BOLD, Math.max(size, 12));
                    for (JLabel[] r : cells) for (JLabel c : r) c.setFont(f);
                }
            });

            status = new JLabel("Starting...", SwingConstants.CENTER);
            status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            status.setOpaque(true);
            status.setBackground(Color.BLACK);
            status.setForeground(Color.WHITE);
            status.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            frame.add(board, BorderLayout.CENTER);
            frame.add(status, BorderLayout.NORTH);

            if (fullscreen) {
                frame.setUndecorated(true);
                frame.setAlwaysOnTop(true);
                // press Escape to quit when fullscreen
                frame.getRootPane().registerKeyboardAction(
                        e -> System.exit(0),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);

                GraphicsDevice screen = GraphicsEnvironment
                        .getLocalGraphicsEnvironment().getDefaultScreenDevice();
                if (screen.isFullScreenSupported()) {
                    screen.setFullScreenWindow(frame);   // true fullscreen, covers the taskbar
                } else {
                    frame.setBounds(screen.getDefaultConfiguration().getBounds());
                }
            } else {
                frame.setSize(400, 440);
                frame.setLocationRelativeTo(null);
            }
            frame.setVisible(true);
            frame.revalidate();
            frame.repaint();
        });
    }

    /** Draw the board from the tik grid. Safe to call from any thread. */
    public void showBoard(String[][] grid) {
        String[][] copy = new String[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                copy[r][c] = grid[r][c];
            }
        }
        SwingUtilities.invokeLater(() -> {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    cells[r][c].setText(copy[r][c]);
                    cells[r][c].setForeground(copy[r][c].equals("X") ? X_COLOR : O_COLOR);
                }
            }
            frame.getContentPane().revalidate();
            frame.getContentPane().repaint();
            Toolkit.getDefaultToolkit().sync();
        });
    }

    /** Change the message at the top. Safe to call from any thread. */
    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> {
            status.setText(text);
            status.repaint();
            Toolkit.getDefaultToolkit().sync();
        });
    }
}