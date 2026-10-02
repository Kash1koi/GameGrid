public class button {
    private int x; //corresponds to the button is in (column index)
    private int y; //corresponds to the row of the button in array format
    private boolean pressed = false;
    public button(int x,int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public boolean isPressed() {
        return pressed;
    }
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }
}
