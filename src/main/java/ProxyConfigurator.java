import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by sarp on 10/06/15.
 */
public class ProxyConfigurator {
    private String proxyContent;

    private static final String MONOSPLIT_BLOCK = "${monosplit.beginconfig}";

    private StringBuilder frontEndAddition = new StringBuilder();
    private StringBuilder backEndAddition = new StringBuilder();

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
        processAdditions();
        FileUtils.writeStringToFile(Paths.get("generatedproxy.cfg").toFile(), proxyContent, "UTF-8");
        String commandContent = new String(Files.readAllBytes(Paths.get("proxydeploy.sh")));
        CommandRunner commandRunner = new CommandRunner(commandContent);
        commandRunner.runCommandLine(Paths.get("").toAbsolutePath().toString());
    }

    private void processAdditions() {
        int blockIndex = proxyContent.indexOf(MONOSPLIT_BLOCK) + MONOSPLIT_BLOCK.length();
        StringBuilder content = new StringBuilder();
        content.append(proxyContent.substring(0, blockIndex));
        content.append(frontEndAddition);
        content.append(backEndAddition);
        content.append(proxyContent.substring(blockIndex));
        proxyContent = content.toString();
    }

    private void addEndPoints(ProjectCopier projectCopier) {
        frontEndAddition.append(System.getProperty("line.separator"));
        frontEndAddition.append("acl ");
        frontEndAddition.append(projectCopier.getEndPointURI());
        frontEndAddition.append(" path_beg -i /");
        frontEndAddition.append(projectCopier.getEndPointURI());
        frontEndAddition.append(System.getProperty("line.separator"));
        frontEndAddition.append("use_backend ");
        frontEndAddition.append(projectCopier.getEndPointURI());
        frontEndAddition.append("_backend if ");
        frontEndAddition.append(projectCopier.getEndPointURI());

        backEndAddition.append(System.getProperty("line.separator"));
        backEndAddition.append("backend ");
        backEndAddition.append(projectCopier.getEndPointURI());
        backEndAddition.append("_backend");
        backEndAddition.append(System.getProperty("line.separator"));
        backEndAddition.append("server ");
        backEndAddition.append(projectCopier.getEndPointURI());
        backEndAddition.append("serv ");
        backEndAddition.append(projectCopier.getIp());
        backEndAddition.append(":");
        backEndAddition.append(projectCopier.getPort());
        backEndAddition.append(" check");
    }

    private void setDefaultBackEnd(ProjectCopier projectCopier) {
        frontEndAddition.append(System.getProperty("line.separator"));
        frontEndAddition.append("default_backend remaining_backend");

        backEndAddition.append(System.getProperty("line.separator"));
        backEndAddition.append("backend remaining_backend");
        backEndAddition.append(System.getProperty("line.separator"));
        backEndAddition.append("server remainingserv ");
        backEndAddition.append(projectCopier.getIp());
        backEndAddition.append(":");
        backEndAddition.append(projectCopier.getPort());
        backEndAddition.append(" check");

    }
}
