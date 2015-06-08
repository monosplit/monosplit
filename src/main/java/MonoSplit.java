import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by sarp on 4/06/15.
 */
public class MonoSplit {

    public static void main(String[] args) throws IOException {
        System.out.println("Running the command on " + args[0]);
        LanguageParser rails = new ParseRails(args[0]);
        ServiceSplitter serviceSplitter = new ServiceSplitter(rails);
        //serviceSplitter.getSortedUri().forEach((uri) -> System.out.println(uri));
        String firstUri = serviceSplitter.getSortedUri().get(0);
        List<String> controllers = serviceSplitter.getControllersFromURI(firstUri, rails);
        //controllers.forEach((con) -> System.out.println(con));
        Set<String> controllerFiles = serviceSplitter.getControllerFiles(controllers);
        controllerFiles.forEach((con) -> System.out.println(con));
        ProjectCopier firstService = new ProjectCopier(args[0], "../app1", controllerFiles, true).apply();
        ProjectCopier lastService = new ProjectCopier(args[0], "../app0", controllerFiles, false).apply();
    }
}