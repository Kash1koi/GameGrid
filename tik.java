public class tik {
    private String[][] grid = new String[3][3];
    public static String turn = "X";
    public tik() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                grid[row][col] = "";
            }
        }
    }
    public String[][] getGrid() {
        return grid;
    }
    public void setGrid(int row, int col, String value) {
        grid[row][col] = value;
    }
    public static void changeTurn() {
        turn = turn.equals("X") ? "O" : "X";
    }
    public void clearGrid() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                grid[row][col] = "";
            }
        }
    }
    public boolean update(button b) {
        if (grid[b.getX()][b.getY()].equals("")) {
            grid[b.getX()][b.getY()] = turn;
            changeTurn();
            return true;
        }
        return false;
    }
    public boolean checkWin() {
        // Check rows
        for (int row = 0; row < 3; row++) {
            if (!grid[row][0].equals("") && grid[row][0].equals(grid[row][1]) && grid[row][1].equals(grid[row][2])) {
                return true;
            }
        }
        // Check columns
        for (int col = 0; col < 3; col++) {
            if (!grid[0][col].equals("") && grid[0][col].equals(grid[1][col]) && grid[1][col].equals(grid[2][col])) {
                return true;
            }
        }
        // Check diagonals
        if (!grid[0][0].equals("") && grid[0][0].equals(grid[1][1]) && grid[1][1].equals(grid[2][2])) {
            return true;
        }
        if (!grid[0][2].equals("") && grid[0][2].equals(grid[1][1]) && grid[1][1].equals(grid[2][0])) {
            return true;
        }
        return false;
    }
    public boolean isGridFull() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (grid[row][col].equals("")) {
                    return false;
                }
            }
        }
        return true;
    }
}
