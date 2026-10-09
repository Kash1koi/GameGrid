public class main {
    public static void main(String[] args) {
        // same grid positions as before: x = column, y = row
        button[] buttons = new button[9];
        for (int i = 0; i < 9; i++) {
            buttons[i] = new button(i % 3, i / 3);
        }

        PicoReader pico = new PicoReader();
        Thread t = new Thread(pico);
        t.setDaemon(true);
        t.start();

        TikGui gui = new TikGui(false); // change to true for fullscreen
        tik toe = new tik();
        gui.showBoard(toe.getGrid());
        gui.setStatus("X's turn");

        int[] previous = new int[9];

        while (true) {
            int[] data = pico.getLatest();

            if (data.length == 9) {
                int pressedCount = 0;
                int pressedIndex = -1;

                for (int i = 0; i < 9; i++) {
                    if (data[i] == 1 && previous[i] == 0) {
                        pressedCount++;
                        pressedIndex = i;
                    }
                }
                previous = data.clone();

                if (pressedCount > 1) {
                    gui.setStatus("One button at a time! " + tik.turn + "'s turn");
                } else if (pressedCount == 1) {
                    System.out.println("Press detected on button index " + pressedIndex);
                    if (toe.update(buttons[pressedIndex])) {
                        gui.showBoard(toe.getGrid());
                        toe.printGrid();
                        boolean over = false;
                        if (toe.checkWin()) {
                            String winner = tik.turn.equals("X") ? "O" : "X";
                            gui.setStatus(winner + " wins!");
                            over = true;
                        } else if (toe.isGridFull()) {
                            gui.setStatus("It's a draw!");
                            over = true;
                        } else {
                            gui.setStatus(tik.turn + "'s turn");
                        }

                        if (over) {
                            pause(4000);
                            toe.clearGrid();
                            tik.turn = "X";
                            gui.showBoard(toe.getGrid());
                            gui.setStatus("X's turn");
                            // ignore anything held down during the pause
                            int[] now = pico.getLatest();
                            previous = (now.length == 9) ? now.clone() : new int[9];
                        }
                    } else {
                        gui.setStatus("Square taken! " + tik.turn + "'s turn");
                    }
                }
            }

            pause(20);
        }
    }

    private static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}