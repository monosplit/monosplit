import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by sarp on 8/06/15.
 */
public class ProjectCopier {
    private File newDir;
    private Set<String> includeController;
    private boolean removeContained;
    private String ip = "0.0.0.0";
    private Integer port = 3000;

    ProjectCopier(String directory, String newSubDir, Set<String> includeController, boolean removeContained) throws IOException {
        this.includeController = includeController;
        newDir = new File(directory, newSubDir);
        this.removeContained = removeContained;
        FileUtils.copyDirectory(new File(directory), newDir);
    }

    public ProjectCopier applyProjectSettings() {
        removeControllers();
        return this;
    }

    public ProjectCopier setIP(String ip) {
        this.ip = ip;
        return this;
    }

    public ProjectCopier setPort(Integer port) {
        this.port = port;
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
        if(includeController.contains(fileNameWithoutConvention) ^ removeContained) file.delete();
    }

}
