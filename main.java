public class main {
    public static void main(String[] args) {
        App app = new App(true);
        app.addGame(new TicTacToeScreen(app));
        app.addGame(new PlaceholderScreen(app, "Game 2"));
        app.addGame(new PlaceholderScreen(app, "Game 3"));
        app.run();
    }
}