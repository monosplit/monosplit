import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Map<String, String> getRemainingControllersFromURI(String uri, LanguageParser parsedRoute) {
        Set<String> usedControllers = parsedRoute.getUriControllerAction().entrySet().parallelStream()
                .filter((entry) -> entry.getKey().substring(1).startsWith(uri))
                .map(entry -> FileNameUtils.getAlphaNumericPath(entry.getValue()))
                .collect(Collectors.toSet());

        return parsedRoute.getUriControllerAction().entrySet().parallelStream()
                .filter((entry) -> usedControllers.contains(FileNameUtils.getAlphaNumericPath(entry.getValue())) && !entry.getKey().substring(1).startsWith(uri))
                .collect(Collectors.toMap(e -> FileNameUtils.getAlphaNumericPath(e.getKey().substring(1)), e -> FileNameUtils.getAlphaNumericPath(e.getValue())));
    }

    public Set<String> getControllerFiles(List<String> controllers) {
        Set<String> controllerFiles = new HashSet<>();
        controllers.forEach((controller) -> controllerFiles.add(FileNameUtils.getAlphaNumericPath(controller)));
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
        return FileNameUtils.getAlphaNumericPath(uriArr[1]);
    }
}
