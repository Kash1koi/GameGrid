import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuScreen implements Screen {
    private final App app;
    private final List<Screen> games;
    private final JPanel view = new JPanel();

    public MenuScreen(App app, List<Screen> games) {
        this.app = app;
        this.games = games;

        view.setBackground(Color.BLACK);
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        view.add(label("Choose a game", 36, Color.GREEN));
        view.add(Box.createVerticalStrut(20));
        for (int i = 0; i < games.size() && i < 9; i++) {
            view.add(label((i + 1) + ".  " + games.get(i).getTitle(), 28,
                    new Color(80, 170, 255)));
            view.add(Box.createVerticalStrut(10));
        }
        view.add(Box.createVerticalGlue());
        view.add(label("Press a numbered button to play", 18, Color.GRAY));
    }

    private JLabel label(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    @Override public String getTitle() { return "Menu"; }
    @Override public JComponent getView() { return view; }
    @Override public void onShow() {}

    @Override
    public void onPress(int index) {
        if (index < games.size()) {
            app.show(games.get(index));
        }
    }
}