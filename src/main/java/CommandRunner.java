import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarp on 8/06/15.
 */
public class CommandRunner {

    private String shellPath;

    public CommandRunner(String shellPath) {
        this.shellPath = shellPath;
    }

    public void runCommandLine(String directory, String command) throws IOException {
        command = command == null ? "" : command;
        ProcessBuilder processBuilder = new ProcessBuilder(shellPath);
        processBuilder.directory(new File(directory));
        Process process = processBuilder.start();
        try (OutputStreamWriter osw = new OutputStreamWriter(process.getOutputStream())) {
            osw.write(command);
        }
        printStream(process.getErrorStream(), true); //ToDo configure error logger and make this warning
        printStream(process.getInputStream(), true);
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
