import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sarp on 8/06/15.
 */
public class FileNameUtils {
    public static String getAlphaNumericPath(String path) {
        Matcher nonAlphaNumeric = Pattern.compile("[^0-9a-zA-Z]+").matcher(path);
        if (nonAlphaNumeric.find()) {
            return path.substring(0, nonAlphaNumeric.start());
        }
        return path;
    }
}
