/**
 * Created by sarp on 20/06/15.
 */
public class Config {
    private String projectPath;
    private String copyFolderPrefix;
    private String ipAddress;
    private Short basePortNumber;

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getCopyFolderPrefix() {
        return copyFolderPrefix;
    }

    public void setCopyFolderPrefix(String copyFolderPrefix) {
        this.copyFolderPrefix = copyFolderPrefix;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Short getBasePortNumber() {
        return basePortNumber;
    }

    public void setBasePortNumber(Short basePortNumber) {
        this.basePortNumber = basePortNumber;
    }
}
