import javax.swing.JComponent;

public interface Screen {
    String getTitle();
    JComponent getView();
    void onShow();              // called each time this screen appears
    void onPress(int index);    // a button was pressed, index 0-8
    default void onTick() {}    // called ~50 times a second while showing
}