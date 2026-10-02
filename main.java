import java.util.*;

public class main {
    public static void main(String[] args) {
        button b1 = new button(0, 0);
        button b2 = new button(1, 0);
        button b3 = new button(2, 0);
        button b4 = new button(0, 1);
        button b5 = new button(1, 1);
        button b6 = new button(2, 1);
        button b7 = new button(0, 2);
        button b8 = new button(1, 2);
        button b9 = new button(2, 2);
        button[] buttons = { b1, b2, b3, b4, b5, b6, b7, b8, b9 };

        PicoReader pico = new PicoReader();
        Thread t = new Thread(pico);
        t.setDaemon(true);
        t.start();
        tik toe = new tik();
        boolean gameEnded = false;
        int[] previous = new int[9];

        while (!gameEnded) {
            int[] data = pico.getLatest(); // newest array from the Pico

            if (data.length == 9) {
                int pressedCount = 0;
                int pressedIndex = -1;

                // only count buttons that just changed from 0 to 1
                for (int i = 0; i < 9; i++) {
                    if (data[i] == 1 && previous[i] == 0) {
                        pressedCount++;
                        pressedIndex = i;
                    }
                }
                previous = data.clone();

                if (pressedCount > 1) {
                    System.out.println("Multiple buttons pressed");
                } else if (pressedCount == 1) {
                    buttons[pressedIndex].setPressed(true);

                    if (toe.update(buttons[pressedIndex])) {
                        toe.printGrid();
                        if (toe.checkWin()) {
                            System.out.println("Winning move detected");
                            String winner = tik.turn.equals("X") ? "O" : "X";
                            System.out.println(winner + " wins!");
                            gameEnded = true;
                        } else if (toe.isGridFull()) {
                            System.out.println("Grid is full");
                            gameEnded = true;
                        }
                    } else {
                        System.out.println("Invalid button press");
                    }
                    resetButtons(buttons);
                }
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

    public static void resetButtons(button[] butts) {
        for (button b : butts) {
            b.setPressed(false);
        }
    }
}
