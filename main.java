public class main {
    public static void main(String[] args) {
        App app = new App(true);   // true = fullscreen
        app.addGame(new TicTacToeScreen(app));
        app.run();
    }
}