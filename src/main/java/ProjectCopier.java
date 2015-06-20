import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sarp on 8/06/15.
 */
public class ProjectCopier {
    private File newDir;
    private Set<String> includeController;
    private boolean isRemainingServices;
    private String ip = "0.0.0.0";
    private String endPointURI;
    private Short port = 0;
    private Set<String> mandatoryFiles = new HashSet<>(Arrays.asList("application_controller.rb")); //Temporary, build it with Language Parser

    ProjectCopier(String directory, String newSubDir, Set<String> includeController, boolean isRemainingServices) throws IOException {
        this.includeController = includeController;
        newDir = new File(directory, newSubDir);
        this.isRemainingServices = isRemainingServices;
        FileUtils.copyDirectory(new File(directory), newDir);
    }

    public String getEndPointURI() {
        return endPointURI;
    }

    public boolean isRemainingServices() {
        return isRemainingServices;
    }

    public String getIp() {
        return ip;
    }

    public Short getPort() {
        return port;
    }

    public ProjectCopier applyProjectSettings() {
        removeControllers();
        return this;
    }

    public ProjectCopier setIP(String ip) {
        this.ip = ip;
        return this;
    }

    public ProjectCopier setPort(Short port) {
        this.port = port;
        return this;
    }

    public ProjectCopier setEndPoint(String uri) {
        this.endPointURI = uri;
        return this;
    }

    public ProjectCopier runProjectCommand() throws IOException {
        String commandContent = new String(Files.readAllBytes(Paths.get("command.sh")));
        commandContent = replaceIPAndPort(commandContent);
        CommandRunner commandRunner = new CommandRunner(commandContent);
        commandRunner.runCommandLine(newDir.getAbsolutePath());
        return this;
    }

    private String replaceIPAndPort(String command) {
        return command.replace("${monosplit.ip}", ip).replace("${monosplit.port}", String.valueOf(port));
    }

    private void removeControllers() {
        File controllerDir = new File(newDir, "app/controllers");
        Arrays.asList(controllerDir.listFiles()).forEach((file) -> checkAndRemoveFile(file));
    }

    private void checkAndRemoveFile(File file) {
        String fileNameWithoutConvention = FileNameUtils.getAlphaNumericPath(file.getName());
        if((!includeController.contains(fileNameWithoutConvention) ^ isRemainingServices)
                && !mandatoryFiles.contains(file.getName())) file.delete();
    }

}
