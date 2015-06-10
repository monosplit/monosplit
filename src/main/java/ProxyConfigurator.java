import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by sarp on 10/06/15.
 */
public class ProxyConfigurator {
    private String proxyContent;

    ProxyConfigurator() throws IOException {
        proxyContent = new String(Files.readAllBytes(Paths.get("haproxy.cfg")));
    }

    public ProxyConfigurator addProjectToProxy(ProjectCopier projectCopier) {
        if (projectCopier.isRemainingServices()) {
            setDefaultBackEnd(projectCopier);
        } else {
            addEndPoints(projectCopier);
        }
        return this;
    }

    public void saveAndDeployProxy() throws IOException {
        FileUtils.writeStringToFile(Paths.get("generatedproxy.cfg").toFile(), proxyContent, "UTF-8");
        String commandContent = new String(Files.readAllBytes(Paths.get("proxydeploy.sh")));
        CommandRunner commandRunner = new CommandRunner(commandContent);
        commandRunner.runCommandLine(Paths.get("").toAbsolutePath().toString());
    }

    private void addEndPoints(ProjectCopier projectCopier) {
    }

    private void setDefaultBackEnd(ProjectCopier projectCopier) {
    }
}
