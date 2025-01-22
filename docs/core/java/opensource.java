import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectScanner {
    private final Set<String> knownOpenSourceLicenses;
    private final Set<String> manifestPatterns;
    private final String gitDir;

    public ProjectScanner() {
        // Initialize known open source licenses
        knownOpenSourceLicenses = new HashSet<>(Arrays.asList(
            "MIT", "Apache", "GPL", "LGPL", "BSD", "Mozilla Public License",
            "Common Development and Distribution License", "Eclipse Public License",
            "ISC", "Artistic License", "GNU", "Creative Commons"
        ));

        // Initialize manifest file patterns
        manifestPatterns = new HashSet<>(Arrays.asList(
            "package.json", "setup.py", "requirements.txt", "pom.xml",
            "build.gradle", "Cargo.toml", "go.mod", "composer.json", "Gemfile"
        ));

        gitDir = ".git";
    }

    public static class ProjectInfo {
        private final String path;
        private final String name;
        private final List<String> projectTypes;
        private final String license;
        private final boolean isConfirmedOpenSource;
        private final String openSourceStatus;
        private final List<String> openSourceIndicators;

        public ProjectInfo(String path, String name, List<String> projectTypes, 
                         String license, boolean isConfirmedOpenSource, 
                         String openSourceStatus, List<String> openSourceIndicators) {
            this.path = path;
            this.name = name;
            this.projectTypes = projectTypes;
            this.license = license;
            this.isConfirmedOpenSource = isConfirmedOpenSource;
            this.openSourceStatus = openSourceStatus;
            this.openSourceIndicators = openSourceIndicators;
        }

        // Getters
        public String getPath() { return path; }
        public String getName() { return name; }
        public List<String> getProjectTypes() { return projectTypes; }
        public String getLicense() { return license; }
        public boolean isConfirmedOpenSource() { return isConfirmedOpenSource; }
        public String getOpenSourceStatus() { return openSourceStatus; }
        public List<String> getOpenSourceIndicators() { return openSourceIndicators; }
    }

    private OpenSourceAnalysis analyzeOpenSourceStatus(Path dirPath) {
        List<String> indicators = new ArrayList<>();
        boolean hasValidLicense = false;
        String detectedLicense = "Unknown";

        // Check for license file and its content
        Optional<String> licenseContent = findAndReadLicenseFile(dirPath);
        if (licenseContent.isPresent()) {
            detectedLicense = identifyLicense(licenseContent.get());
            if (isKnownOpenSourceLicense(detectedLicense)) {
                hasValidLicense = true;
                indicators.add("Valid open source license: " + detectedLicense);
            }
        }

        // Check for public repository indicators
        if (Files.exists(dirPath.resolve(".git"))) {
            // Look for remote URL in git config
            try {
                Path gitConfigPath = dirPath.resolve(".git/config");
                if (Files.exists(gitConfigPath)) {
                    String gitConfig = Files.readString(gitConfigPath);
                    if (gitConfig.contains("github.com") || 
                        gitConfig.contains("gitlab.com") || 
                        gitConfig.contains("bitbucket.org")) {
                        indicators.add("Public repository reference found");
                    }
                }
            } catch (IOException e) {
                // Handle silently
            }
        }

        // Check for CONTRIBUTING.md or similar files
        if (Files.exists(dirPath.resolve("CONTRIBUTING.md")) || 
            Files.exists(dirPath.resolve("CONTRIBUTING"))) {
            indicators.add("Contributing guidelines present");
        }

        // Check package.json for public access
        try {
            Path packageJsonPath = dirPath.resolve("package.json");
            if (Files.exists(packageJsonPath)) {
                String packageJson = Files.readString(packageJsonPath);
                if (packageJson.contains("\"private\": false") || 
                    !packageJson.contains("\"private\"")) {
                    indicators.add("Package marked as public");
                }
            }
        } catch (IOException e) {
            // Handle silently
        }

        // Determine overall status
        String status;
        boolean isConfirmedOpenSource = false;
        if (hasValidLicense && !indicators.isEmpty()) {
            status = "Confirmed Open Source";
            isConfirmedOpenSource = true;
        } else if (hasValidLicense) {
            status = "Likely Open Source (has license)";
        } else if (!indicators.isEmpty()) {
            status = "Potential Open Source (has indicators)";
        } else {
            status = "Uncertain";
        }

        return new OpenSourceAnalysis(detectedLicense, isConfirmedOpenSource, status, indicators);
    }

    private Optional<String> findAndReadLicenseFile(Path dirPath) {
        String[] licenseFiles = {"LICENSE", "LICENSE.md", "LICENSE.txt", "COPYING", "COPYING.md"};
        for (String licenseFile : licenseFiles) {
            Path licensePath = dirPath.resolve(licenseFile);
            if (Files.exists(licensePath)) {
                try {
                    return Optional.of(Files.readString(licensePath));
                } catch (IOException e) {
                    // Continue to next file
                }
            }
        }
        return Optional.empty();
    }

    private String identifyLicense(String content) {
        content = content.toLowerCase();
        for (String license : knownOpenSourceLicenses) {
            if (content.contains(license.toLowerCase())) {
                return license;
            }
        }
        return "Unknown";
    }

    private boolean isKnownOpenSourceLicense(String license) {
        return knownOpenSourceLicenses.contains(license);
    }

    private static class OpenSourceAnalysis {
        final String license;
        final boolean isConfirmedOpenSource;
        final String status;
        final List<String> indicators;

        OpenSourceAnalysis(String license, boolean isConfirmedOpenSource, 
                         String status, List<String> indicators) {
            this.license = license;
            this.isConfirmedOpenSource = isConfirmedOpenSource;
            this.status = status;
            this.indicators = indicators;
        }
    }

    public List<ProjectInfo> scanDirectory(String rootPath, int maxDepth) {
        List<ProjectInfo> projects = new ArrayList<>();
        Path normalizedRoot = Paths.get(rootPath).normalize().toAbsolutePath();

        try {
            Files.walk(normalizedRoot, maxDepth)
                    .filter(Files::isDirectory)
                    .forEach(dirPath -> {
                        if (isPotentialProject(dirPath)) {
                            OpenSourceAnalysis analysis = analyzeOpenSourceStatus(dirPath);
                            int depth = normalizedRoot.relativize(dirPath).getNameCount();
                            
                            ProjectInfo project = new ProjectInfo(
                                dirPath.toString(),
                                dirPath.getFileName().toString(),
                                getProjectTypes(dirPath),
                                analysis.license,
                                analysis.isConfirmedOpenSource,
                                analysis.status,
                                analysis.indicators
                            );
                            projects.add(project);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error walking directory tree: " + e.getMessage());
        }

        return projects;
    }

    private boolean isPotentialProject(Path dirPath) {
        try {
            return Files.list(dirPath)
                    .map(path -> path.getFileName().toString())
                    .anyMatch(name -> name.startsWith("LICENSE") || 
                                    manifestPatterns.contains(name) || 
                                    name.equals(gitDir));
        } catch (IOException e) {
            return false;
        }
    }

    private List<String> getProjectTypes(Path dirPath) {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("package.json", "Node.js");
        mappings.put("setup.py", "Python");
        mappings.put("requirements.txt", "Python");
        mappings.put("pom.xml", "Java/Maven");
        mappings.put("build.gradle", "Java/Gradle");
        mappings.put("Cargo.toml", "Rust");
        mappings.put("go.mod", "Go");
        mappings.put("composer.json", "PHP");
        mappings.put("Gemfile", "Ruby");

        try {
            return Files.list(dirPath)
                    .map(path -> path.getFileName().toString())
                    .filter(mappings::containsKey)
                    .map(mappings::get)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public String generateReport(List<ProjectInfo> projects) {
        StringBuilder report = new StringBuilder();
        report.append("Open Source Project Analysis Report\n");
        report.append("==================================\n\n");

        for (ProjectInfo project : projects) {
            report.append("Project: ").append(project.getName()).append("\n");
            report.append("Path: ").append(project.getPath()).append("\n");
            report.append("Type(s): ").append(
                project.getProjectTypes().isEmpty() ? "Unknown" : 
                String.join(", ", project.getProjectTypes())
            ).append("\n");
            report.append("License: ").append(project.getLicense()).append("\n");
            report.append("Open Source Status: ").append(project.getOpenSourceStatus()).append("\n");
            
            if (!project.getOpenSourceIndicators().isEmpty()) {
                report.append("Open Source Indicators:\n");
                for (String indicator : project.getOpenSourceIndicators()) {
                    report.append("  - ").append(indicator).append("\n");
                }
            }
            report.append("------------------------------\n\n");
        }

        return report.toString();
    }

    public void saveReport(List<ProjectInfo> projects, String outputFile) {
        String report = generateReport(projects);
        try {
            Files.writeString(Paths.get(outputFile), report);
        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ProjectScanner scanner = new ProjectScanner();
        String currentDir = System.getProperty("user.dir");
        
        System.out.println("Scanning directory: " + currentDir);
        List<ProjectInfo> projects = scanner.scanDirectory(currentDir, 3);
        
        String report = scanner.generateReport(projects);
        System.out.println(report);
        
        scanner.saveReport(projects, "opensource_projects_report.txt");
    }
}