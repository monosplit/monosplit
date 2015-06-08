import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by sarp on 8/06/15.
 */
public class ProjectCopier {
    private File newDir;
    private Set<String> includeController;
    private boolean removeContained;

    ProjectCopier(String directory, String newSubDir, Set<String> includeController, boolean removeContained) throws IOException {
        this.includeController = includeController;
        newDir = new File(directory, newSubDir);
        this.removeContained = removeContained;
        FileUtils.copyDirectory(new File(directory), newDir);
    }

    public ProjectCopier apply() {
        removeControllers();
        return this;
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
