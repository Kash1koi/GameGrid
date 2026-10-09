public class main {
    public static void main(String[] args) {
        App app = new App(false);   // true = fullscreen
        app.addGame(new TicTacToeScreen(app));
        app.run();
    }
}