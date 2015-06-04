import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sarp on 4/06/15.
 */
public abstract class LanguageParser {

    private Map<String, String> uriControllerAction = new HashMap<>();

    LanguageParser(String directory) throws IOException {
        parseLanguage(directory);
    }

    abstract void parseLanguage(String directory) throws IOException;

    Map<String, String> getUriControllerAction() {
        return uriControllerAction;
    }
}
