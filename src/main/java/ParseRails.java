import java.io.*;
import java.util.*;

/**
 * Rails rake implementation
 */
public class ParseRails extends LanguageParser {

    ParseRails(String directory) throws IOException {
        super(directory);
    }

    @Override
    void parseLanguage(String directory) throws IOException {
        parseRakeRoutes(directory);
    }

    private void parseRakeRoutes(String directory) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "rake", "routes");
        processBuilder.directory(new File(directory));
        Process process = processBuilder.start();
        printStream(process.getErrorStream(), true); //ToDo configure error logger and make this warning
        stringToMap(printStream(process.getInputStream(), false));
    }

    private void stringToMap(List<String> routeLines) {
        routeLines.remove(0);//First line is informing
        for (String route : routeLines ) {
            List<String> tokenizedRoute = tokenizeString(route);
            Collections.reverse(tokenizedRoute);
            super.getUriControllerAction().put(tokenizedRoute.get(1), tokenizedRoute.get(0));
        }
    }

    private List<String> tokenizeString(String str) {
        StringTokenizer st = new StringTokenizer(str);
        List<String> stringList = new ArrayList<>();
        while (st.hasMoreTokens()) {
            stringList.add(st.nextToken());
        }
        return stringList;
    }

    private List<String> printStream(InputStream is, boolean print) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        List<String> routeLines = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            routeLines.add(line);
            if (print) System.out.println(line);
        }
        return routeLines;
    }
}
