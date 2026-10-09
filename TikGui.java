import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

public class TikGui {
    private JFrame frame;
    private JLabel[][] cells = new JLabel[3][3];
    private JLabel status;

    public TikGui(boolean fullscreen) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Tic Tac Toe");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JPanel board = new JPanel(new GridLayout(3, 3, 4, 4));
            board.setBackground(Color.DARK_GRAY);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    JLabel cell = new JLabel("", SwingConstants.CENTER);
                    cell.setOpaque(true);
                    cell.setBackground(Color.WHITE);
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
                    for (JLabel[] r : cells)
                        for (JLabel c : r)
                            c.setFont(f);
                }
            });

            status = new JLabel("Starting...", SwingConstants.CENTER);
            status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            status.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            frame.add(board, BorderLayout.CENTER);
            frame.add(status, BorderLayout.SOUTH);

            if (fullscreen) {
                frame.setUndecorated(true);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                // press Escape to quit when fullscreen
                frame.getRootPane().registerKeyboardAction(
                        e -> System.exit(0),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
            } else {
                frame.setSize(500, 580);
                frame.setLocationRelativeTo(null);
            }
            frame.setVisible(true);
        });
    }

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
                    cells[r][c].setForeground(copy[r][c].equals("X") ? Color.BLUE : Color.RED);
                }
            }
            frame.getContentPane().revalidate();
            frame.getContentPane().repaint();
            Toolkit.getDefaultToolkit().sync();
        });
    }

    // Change the message under the board
    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> {
            status.setText(text);
            status.repaint();
            Toolkit.getDefaultToolkit().sync();
        });
    }
}