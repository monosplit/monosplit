import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sarp on 4/06/15.
 */
public class MonoSplit {

    public static void main(String[] args) throws IOException {
        Yaml yaml = new Yaml();
        Config projectConfig = yaml.loadAs(new String(Files.readAllBytes(Paths.get(args[0]))), Config.class);
        System.out.println("Running the command on " + projectConfig.getProjectPath());
        LanguageParser rails = new ParseRails(projectConfig.getProjectPath());
        ServiceSplitter serviceSplitter = new ServiceSplitter(rails);
        //serviceSplitter.getSortedUri().forEach((uri) -> System.out.println(uri));
        String firstUri = serviceSplitter.getSortedUri().get(0);
        List<String> controllers = serviceSplitter.getControllersFromURI(firstUri, rails);
        //controllers.forEach((con) -> System.out.println(con));
        Set<String> controllerFiles = serviceSplitter.getControllerFiles(controllers);
       // controllerFiles.forEach((con) -> System.out.println(con));

        ProjectCopier firstService = new ProjectCopier(projectConfig.getProjectPath(), "../app1", controllerFiles, false).
                setIP("0.0.0.0").setPort(3001).setEndPoint(firstUri).applyProjectSettings();
        firstService.runProjectCommand();

        Map<String,String> remainingUriControllers = serviceSplitter.getRemainingControllersFromURI(firstUri, rails);
        remainingUriControllers.forEach((uri, controller) -> controllerFiles.remove(controller)); //Should check if uri is not used when multiple uri services are used

        ProjectCopier lastService = new ProjectCopier(projectConfig.getProjectPath(), "../app0", controllerFiles, true).setIP("0.0.0.0").setPort(3000).applyProjectSettings();
        lastService.runProjectCommand();

        ProxyConfigurator proxyConfigurator = new ProxyConfigurator().addProjectToProxy(firstService).addProjectToProxy(lastService);
        proxyConfigurator.saveAndDeployProxy();
    }
}