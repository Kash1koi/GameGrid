public class main {
    public static void main(String[] args) {
        App app = new App(true);
        app.addGame(new TicTacToeScreen(app));
        app.addGame(new SimonScreen(app));
        app.run();
    }
}