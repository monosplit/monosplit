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
 * Created by sarp on 4/06/15
 * This is psvm class takes the first argument as config.yaml file
 */
public class MonoSplit {

    public static void main(String[] args) throws IOException {
        MonoSplit monoSplit = new MonoSplit();
        Config projectConfig = monoSplit.setConfiguration(args[0]);
        CommandRunner commandRunner = new CommandRunner(projectConfig.getShellPath());

        System.out.println("Running the command on " + projectConfig.getProjectPath());
        LanguageParser rails = new ParseRails(projectConfig.getProjectPath());

        ServiceSplitter serviceSplitter = new ServiceSplitter(rails);

        List<ProjectCopier> mServices = monoSplit.splitServicesForTheGivenAmount(projectConfig.getMicroServiceAmount(), serviceSplitter, rails, projectConfig, commandRunner);

        Set<String> allUsedControllers = monoSplit.completelyUsedControllers(mServices, serviceSplitter, rails);
        mServices.add(monoSplit.configureDefaultService(projectConfig, allUsedControllers, commandRunner));

        ProxyConfigurator proxyConfigurator = new ProxyConfigurator(commandRunner);
        monoSplit.addAllServicesToProxy(proxyConfigurator, mServices);

        monoSplit.runAllMicroServices(mServices);
        proxyConfigurator.saveAndDeployProxy();
    }

    public void addAllServicesToProxy(ProxyConfigurator proxyConfigurator, List<ProjectCopier> mServices) {
        mServices.forEach(proxyConfigurator::addProjectToProxy);
    }


    public ProjectCopier configureDefaultService(Config projectConfig, Set<String> controllerFiles, CommandRunner commandRunner) throws IOException {
        return new ProjectCopier(projectConfig.getProjectPath(), projectConfig.getCopyFolderPrefix() + "0", controllerFiles, true, commandRunner)
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
            String endpointName = service.getEndPointURI();
            endpointName = endpointName == null ? "Remaining services" : endpointName;
            System.out.println("Running " + endpointName);
            service.applyProjectSettings().runProjectCommand();
        }
    }

    public List<ProjectCopier> splitServicesForTheGivenAmount(short amount, ServiceSplitter serviceSplitter, LanguageParser languageParser,
            Config projectConfig, CommandRunner commandRunner) throws IOException {
        List<ProjectCopier> copiedServices = new ArrayList<>();
        for (short i = 0; i < amount; i++) {
            String uri = serviceSplitter.getSortedUri().get(i);

            List<String> controllers = serviceSplitter.getControllersFromURI(uri, languageParser);
            Set<String> controllerFiles = serviceSplitter.getControllerFiles(controllers);

            copiedServices.add(new ProjectCopier(projectConfig.getProjectPath(), projectConfig.getCopyFolderPrefix() + String.valueOf(i + 1),
                    controllerFiles, false, commandRunner).setIP(projectConfig.getIpAddress()).setPort((short) (projectConfig.getBasePortNumber() + i + 1))
                    .setEndPoint(uri));

        }
        return copiedServices;
    }

    public Config setConfiguration(String yamlFile) throws IOException {
        Yaml yaml = new Yaml();
        return yaml.loadAs(new String(Files.readAllBytes(Paths.get(yamlFile))), Config.class);
    }
}
