import java.io.IOException;

/**
 * Created by sarp on 4/06/15.
 */
public class MonoSplit {

    public static void main(String[] args) throws IOException {
        System.out.println("Running the command on " + args[0]);
        LanguageParser rails = new ParseRails(args[0]);
        rails.getUriControllerAction().forEach((uri, controller) -> System.out.println("URI: " + uri + ": Controller: " + controller));
    }
}
