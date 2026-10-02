//import button;
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
        button[] buttons = {b1, b2, b3, b4, b5, b6, b7, b8, b9};

        //PicoReader pico = new PicoReader();
        //Thread t = new Thread(pico);
        //t.setDaemon(true);
        //t.start();
        //while loop

        //get serial imputs array
            //cycle through and check for pressed buttons (1 in the array)
            //update button states based on serial input (if false than outuput that its bad input)
            //check for win condition after updating button states
            //check if the is full
            //reset or restart the game if it has ended


        // seral data will be array of 9 values as well
        //if(!tik.update(buttons[2])){
            //display red dot on the screen
        //}else{
        //    tik.checkWin();
        //}

        b1.setPressed(true);

    }
}
