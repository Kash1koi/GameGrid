import javax.swing.*;
import java.awt.*;

public class PlaceholderScreen implements Screen {
    private final App app;
    private final String title;
    private final JPanel view = new JPanel(new GridBagLayout());
    private long returnAt = 0;

    public PlaceholderScreen(App app, String title) {
        this.app = app;
        this.title = title;

        view.setBackground(Color.BLACK);
        JLabel label = new JLabel(title + " - coming soon", SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        label.setForeground(Color.WHITE);
        view.add(label);
    }

    @Override public String getTitle() { return title; }
    @Override public JComponent getView() { return view; }

    @Override
    public void onShow() {
        returnAt = System.currentTimeMillis() + 3000;   // back to menu after 3 s
    }

    @Override
    public void onPress(int index) {
        app.showMenu();   // any press returns to the menu right away
    }

    @Override
    public void onTick() {
        if (System.currentTimeMillis() >= returnAt) {
            app.showMenu();
        }
    }
}