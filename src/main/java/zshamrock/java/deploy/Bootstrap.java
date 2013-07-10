package zshamrock.java.deploy;

public class Bootstrap {
    public static void main(String[] args) {
        new GitHubHttpServer(new HandlerMapping());
    }
}
