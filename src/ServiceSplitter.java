import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sarp on 7/06/15.
 */
public class ServiceSplitter {

    private List<String> sortedUri;

    public ServiceSplitter(LanguageParser parsedRoute) {
        Map<String, Integer> uriCount = new HashMap<>();
        parsedRoute.getUriControllerAction().forEach((uri, controller) -> countURI(splitURI(uri), uriCount));
        sortedUri = new ArrayList<>();
        uriCount.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEach((uri) -> sortedUri.add(uri.getKey()));
    }

    public List<String> getControllersFromURI(String uri, LanguageParser parsedRoute) {
        List<String> controllers = new ArrayList<>();
        parsedRoute.getUriControllerAction().forEach((path, controller) -> addControllerToList(controllers, controller, compareURIs(uri, path)));
        return controllers;
    }

    public Set<String> getControllerFiles(List<String> controllers) {
        Set<String> controllerFiles = new HashSet<>();
        controllers.forEach((controller) -> controllerFiles.add(getAlphaNumericPath(controller)));
        return controllerFiles;
    }

    public List<String> getSortedUri() {
        return sortedUri;
    }

    private void addControllerToList(List<String> controllers, String controller, boolean shouldAdd) {
        if (shouldAdd) controllers.add(controller);
    }

    private boolean compareURIs(String uri, String uriToSplit) {
        return uri.equals(splitURI(uriToSplit));
    }

    private void countURI(String uri, Map<String, Integer> uriCount) {
        if (uri == null) return;
        Integer count = uriCount.get(uri);
        count = count == null ? 1 : ++count;
        uriCount.put(uri, count);
    }

    private String splitURI(String uri) {
        String[] uriArr = uri.split("/");
        if (uriArr.length < 1) return null;
        return getAlphaNumericPath(uriArr[1]);
    }

    private String getAlphaNumericPath(String path) {
        Matcher nonAlphaNumeric = Pattern.compile("\\W+").matcher(path);
        if (nonAlphaNumeric.find()) {
            return path.substring(0, nonAlphaNumeric.start());
        }
        return path;
    }
}
