import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sarp on 4/06/15.
 */
public class MonoSplit {

    public static void main(String[] args) throws IOException {
        MonoSplit monoSplit = new MonoSplit();
        Config projectConfig = monoSplit.setConfiguration(args[0]);

        System.out.println("Running the command on " + projectConfig.getProjectPath());
        LanguageParser rails = new ParseRails(projectConfig.getProjectPath());

        ServiceSplitter serviceSplitter = new ServiceSplitter(rails);

        List<ProjectCopier> mServices = monoSplit.splitServicesForTheGivenAmount((short) 1, serviceSplitter, rails, projectConfig);

        Set<String> allUsedControllers = monoSplit.completelyUsedControllers(mServices, serviceSplitter, rails);
        mServices.add(monoSplit.configureDefaultService(projectConfig, allUsedControllers));

        ProxyConfigurator proxyConfigurator = new ProxyConfigurator();
        monoSplit.addAllServicesToProxy(proxyConfigurator, mServices);

        monoSplit.runAllMicroServices(mServices);
        proxyConfigurator.saveAndDeployProxy();
    }

    public void addAllServicesToProxy(ProxyConfigurator proxyConfigurator, List<ProjectCopier> mServices) {
        mServices.forEach(proxyConfigurator::addProjectToProxy);
    }


    public ProjectCopier configureDefaultService(Config projectConfig, Set<String> controllerFiles) throws IOException {
        return new ProjectCopier(projectConfig.getProjectPath(), projectConfig.getProjectPath() + "0", controllerFiles, true)
                .setIP(projectConfig.getIpAddress()).setPort(projectConfig.getBasePortNumber());
    }

    public Set<String> completelyUsedControllers(List<ProjectCopier> mServices, ServiceSplitter serviceSplitter, LanguageParser rails) {
        Set<String> allControllers = mServices.parallelStream().flatMap(service -> service.getIncludeController().parallelStream()).collect(Collectors.toSet());
        for (ProjectCopier mService : mServices) {
            Map<String, Set<String>> remainingUriControllerFiles = serviceSplitter.getRemainingControllerFilesFromURI(mService.getEndPointURI(), rails);
            remainingUriControllerFiles.forEach((controller, uris) -> removeRemainingUris(mServices, uris, controller, allControllers));
        }
        return allControllers;
    }

    private void removeRemainingUris(List<ProjectCopier> mServices, Set<String> uris, String controller, Set<String> allControllers) {
        int remainingUriAmount = uris.size();
        for (String uri : uris) {
            if (isUriUsed(mServices, uri)) remainingUriAmount--;
        }
        if (remainingUriAmount != 0) allControllers.remove(controller);
    }

    private boolean isUriUsed(List<ProjectCopier> mServices, String uri) {
        for (ProjectCopier mService : mServices) {
            if (mService.getEndPointURI().equals(uri)) return true;
        }
        return false;
    }

    public void runAllMicroServices(List<ProjectCopier> services) throws IOException {
        for (ProjectCopier service : services) {
            service.applyProjectSettings().runProjectCommand();
        }
    }

    public List<ProjectCopier> splitServicesForTheGivenAmount(short amount, ServiceSplitter serviceSplitter,
                                               LanguageParser languageParser, Config projectConfig) throws IOException {
        List<ProjectCopier> copiedServices = new ArrayList<>();
        for (short i = 0; i < amount; i++) {
            String uri = serviceSplitter.getSortedUri().get(i);

            List<String> controllers = serviceSplitter.getControllersFromURI(uri, languageParser);
            Set<String> controllerFiles = serviceSplitter.getControllerFiles(controllers);

            copiedServices.add(new ProjectCopier(projectConfig.getProjectPath(), projectConfig.getCopyFolderPrefix() + String.valueOf(i + 1),
                    controllerFiles, false).setIP(projectConfig.getIpAddress()).setPort((short) (projectConfig.getBasePortNumber() + i + 1))
                    .setEndPoint(uri));

        }
        return copiedServices;
    }

    public Config setConfiguration(String yamlFile) throws IOException {
        Yaml yaml = new Yaml();
        return yaml.loadAs(new String(Files.readAllBytes(Paths.get(yamlFile))), Config.class);
    }
}