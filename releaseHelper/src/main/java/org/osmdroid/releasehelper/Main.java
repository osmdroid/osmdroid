/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmdroid.releasehelper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Scm;
import org.apache.maven.model.Site;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

/**
 *
 * @author AO
 */
public class Main {

    public static void main(String[] args) throws Exception {
        //load gradle properties
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(new File("gradle.properties"));
        props.load(fis);
        fis.close();

        fis = new FileInputStream(new File("local.properties"));
        props.load(fis);
        fis.close();

        List<String> settingsGradle = FileUtils.readLines(new File("settings.gradle"), "UTF-8");

        //clean our target
        File target = new File("target");
        FileUtils.deleteQuietly(target);
        target.mkdirs();

        //gather all osmdroid dependencies
        String groupId = props.getProperty("pom.groupId");
        String version = props.getProperty("pom.version");

        String userHomeDir = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            userHomeDir = System.getenv("USERPROFILE");
        } else {
            userHomeDir = System.getenv("HOME");
        }
        String m2 = userHomeDir + File.separator + ".m2" + File.separator + "repository";

        String groupHome = m2 + File.separator + groupId.replace(".", File.separator);

        //locate all modules in the gradle project that we need to publish
        List<String> modules = findModules(false);

        //remove any commented out or disabled aars
        trim(modules, settingsGradle);

        //copy all those modules to our target folder
        copyToTarget(target, modules, groupHome, version);

        //inject our 'extra' javadoc content when needed
        //note: mcafee sometimes flags this operation, should be ignorable
        injectJavadocJars(target, version, modules);

        //fix the poms
        fixPoms(target, props);
        //sign everything with gpg
        signFiles(target, props);
        //hash everything
        hashFiles(target);

        //push
        push(target, props);

        //TODO bump pom and android version code numbers
        //write back out the properties file, preserving the comments
        /*File file = new File("gradle.properties");

        PropertiesConfiguration config = new PropertiesConfiguration();
        PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout(config);
        layout.load(new InputStreamReader(new FileInputStream(file)));

        layout.save(new FileWriter(file, false));
         */
        
        //copy just the apks in this case
        modules = findModules(true);

        //remove any commented out or disabled aars
        trim(modules, settingsGradle);

        //copy all those modules to our target folder
        for (String module: modules) {
            String folder = props.getProperty("pom.version").contains("-SNAPSHOT") ? "debug":"release";
            File[] files = new File(module + "/build/outputs/apk/" + folder).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".apk");
                }
            });
            if (files!=null) {
                for (File f: files) {
                    FileUtils.copyFileToDirectory(f, new File("target"));
                }
            }
        }
        //this gets uploaded to github
        makeDistZip(props);

    }

    private static List<String> findModules(boolean apksOnly) throws Exception {
        List<String> r = new ArrayList<>();
        File cwd = new File(".");
        File[] list = cwd.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        if (list != null) {
            for (File f : list) {
                File[] files = f.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return "build.gradle".equals(name);
                    }
                });
                if (files != null) {
                    for (File b : files) {
                        String content = FileUtils.readFileToString(b, "UTF-8");
                        if (apksOnly) {
                            if (content.contains("com.android.application")) {
                                r.add(f.getName());
                            }
                        } else {
                            if (content.contains("com.android.library")) {
                                r.add(f.getName());
                            } else if (content.contains("java")) {
                                r.add(f.getName());
                            } else if (content.contains("war")) {
                                r.add(f.getName());
                            }
                        }
                    }
                }
            }
        }
        if (r.isEmpty()) {
            printError();
            throw new Exception("failed to find any modules to publish");
        }
        return r;
    }

    private static void trim(List<String> modules, List<String> settingsGradle) {

        List<String> removeme = new ArrayList<>();
        for (String s : modules) {
            if (!settingsGradle.contains("include ':" + s + "'")) {
                removeme.add(s);
            }
        }
        modules.removeAll(removeme);
    }

    private static void copyToTarget(File target, List<String> modules, String groupHome, String version) throws IOException {
        for (String s : modules) {
            System.out.println(s + " copy");
            File src = new File(groupHome, s);
            src = new File(src, version);
            File[] files = src.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().equals("maven-metadata-local.xml")) {
                        continue;
                    }
                    if (f.getName().endsWith(".module")) {
                        continue;
                    }
                    FileUtils.copyFileToDirectory(f, target);
                }
            }
        }
    }

    private static void injectJavadocJars(File target, String version, List<String> modules) throws Exception {

        File[] javadocs = target.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().contains("javadoc.jar");
            }
        });

        for (String module : modules) {
            if (hasDocFiles(module + "/src/main/java/")) {
                System.out.println(module + " has doc-files, injecting");
                File temp = new File(target, "tmp");
                temp.mkdirs();
                File src = new File(target, module + "-" + version + "-javadoc.jar");
                unzipFolder(src, temp);

                copyDocFiles(new File(module + "/src/main/java/"), temp);

                zipFolder(temp, new File("temp.jar"));
                src.delete();
                FileUtils.moveFile(new File("temp.jar"), src);
                FileUtils.deleteQuietly(temp);
            }
        }

    }

    private static boolean hasDocFiles(String module) {
        File f = new File(module);
        File[] folders = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.isDirectory());

            }
        });
        if (folders != null) {
            for (File folder : folders) {
                if (folder.getName().equals("doc-files")) {
                    return true;
                }

                boolean val = hasDocFiles(folder.getAbsolutePath());
                if (val) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void unzipFolder(File archiveFile, File zipDestinationFolder) throws Exception {
        File targetDir = zipDestinationFolder;
        try (ArchiveInputStream i = new JarArchiveInputStream(new FileInputStream(archiveFile))) {
            ArchiveEntry entry = null;
            while ((entry = i.getNextEntry()) != null) {
                if (!i.canReadEntryData(entry)) {
                    // log something?
                    continue;
                }
                String name = targetDir + "/" + entry;

                File f = new File(name);
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        printError();
                        printError();
                        throw new IOException("failed to create directory " + parent);
                    }
                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
            }
        }
    }

    private static void copyDocFiles(File src, File temp) throws IOException {
        //System.out.println("inject " + src.getAbsolutePath() + " from " + temp);
        if (src.getName().equals("doc-files") && src.isDirectory()) {
            temp.mkdirs();
            FileUtils.copyDirectory(src, temp);
        }
        File[] folders = src.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory();
            }
        });
        if (folders == null) {
            return;
        }
        for (File f : folders) {
            copyDocFiles(f, new File(temp, f.getName()));
        }
    }

    private static void zipFolder(File sourceDirectory, File destinationJar) throws Exception {
        Collection<File> filesToArchive = new ArrayList<File>();

        buildFileList(filesToArchive, sourceDirectory);
        System.out.println("Compressing " + filesToArchive.size() + " files");
        try (ArchiveOutputStream o = new JarArchiveOutputStream(new FileOutputStream(destinationJar))) {

            for (File f : filesToArchive) {
                // maybe skip directories for formats like AR that don't store directories
                String entryName = f.getAbsolutePath().replace(sourceDirectory.getAbsolutePath(), "");
                entryName = entryName.replace("\\", "/");
                if (entryName.startsWith("/")) {
                    entryName = entryName.substring(1);
                }
                ArchiveEntry entry = o.createArchiveEntry(f, entryName);
                // potentially add more flags to entry
                o.putArchiveEntry(entry);
                if (f.isFile()) {
                    try (InputStream i = Files.newInputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
                o.closeArchiveEntry();
            }
            o.finish();
        }
    }

    private static void makeDistZip(Properties props) throws Exception {

        System.out.println("making dist zip");

        try (ArchiveOutputStream o = new JarArchiveOutputStream(new FileOutputStream(new File("osmdroid-dist-" + props.getProperty("pom.version") + ".zip")))) {
            File[] apks = new File("target").listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".apk");
                }
            });
            for (File f : apks) {
                // maybe skip directories for formats like AR that don't store directories
                String entryName = "apk/" + f.getName();
                entryName = entryName.replace("\\", "/");
                if (entryName.startsWith("/")) {
                    entryName = entryName.substring(1);
                }
                ArchiveEntry entry = o.createArchiveEntry(f, entryName);
                // potentially add more flags to entry
                o.putArchiveEntry(entry);
                if (f.isFile()) {
                    try (InputStream i = Files.newInputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
                o.closeArchiveEntry();
            }

            File[] aar = new File("target").listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".aar");
                }
            });
            for (File f : aar) {
                // maybe skip directories for formats like AR that don't store directories
                String entryName = "aar/" + f.getName();
                entryName = entryName.replace("\\", "/");
                if (entryName.startsWith("/")) {
                    entryName = entryName.substring(1);
                }
                ArchiveEntry entry = o.createArchiveEntry(f, entryName);
                // potentially add more flags to entry
                o.putArchiveEntry(entry);
                if (f.isFile()) {
                    try (InputStream i = Files.newInputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
                o.closeArchiveEntry();
            }
            File[] libs = new File("target").listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar") || name.endsWith(".war");
                }
            });
            for (File f : libs) {
                // maybe skip directories for formats like AR that don't store directories
                String entryName = "libs/" + f.getName();
                entryName = entryName.replace("\\", "/");
                if (entryName.startsWith("/")) {
                    entryName = entryName.substring(1);
                }
                ArchiveEntry entry = o.createArchiveEntry(f, entryName);
                // potentially add more flags to entry
                o.putArchiveEntry(entry);
                if (f.isFile()) {
                    try (InputStream i = Files.newInputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
                o.closeArchiveEntry();
            }

            File[] zip = new File("target").listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".zip");
                }
            });
            for (File f : zip) {
                // maybe skip directories for formats like AR that don't store directories
                String entryName = "distributions/" + f.getName();
                entryName = entryName.replace("\\", "/");
                if (entryName.startsWith("/")) {
                    entryName = entryName.substring(1);
                }
                ArchiveEntry entry = o.createArchiveEntry(f, entryName);
                // potentially add more flags to entry
                o.putArchiveEntry(entry);
                if (f.isFile()) {
                    try (InputStream i = Files.newInputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
                o.closeArchiveEntry();
            }

            o.finish();
        }
    }

    private static void buildFileList(Collection<File> filesToArchive, File sourceDirectory) {
        File[] files = sourceDirectory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    buildFileList(filesToArchive, f);
                } else if (f.isHidden()) {
                    continue;
                } else {
                    filesToArchive.add(f);
                }
            }
        }
    }

    private static void hashFiles(File target) throws Exception {
        System.out.println("hashing files");

        for (File f : target.listFiles()) {
            FileInputStream fis = new FileInputStream(f);
            String hash = DigestUtils.sha512Hex(fis);
            fis.close();

            FileUtils.write(new File(f + ".sha512"), hash, "UTF-8");

            fis = new FileInputStream(f);
            hash = DigestUtils.md5Hex(fis);
            fis.close();

            FileUtils.write(new File(f + ".md5"), hash, "UTF-8");

            fis = new FileInputStream(f);
            hash = DigestUtils.sha1Hex(fis);
            fis.close();

            FileUtils.write(new File(f + ".sha1"), hash, "UTF-8");
        }

    }

    private static void fixPoms(File target, Properties props) throws Exception {
        File[] poms = target.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".pom");
            }
        });
        if (poms != null) {
            for (File pom : poms) {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader(pom));
                model.setName(model.getArtifactId());
                model.setUrl(props.getProperty("pom.url"));
                model.setInceptionYear(props.getProperty("pom.inceptionYear"));
                model.setOrganization(new Organization());
                model.getOrganization().setName(props.getProperty("pom.organization.name"));
                model.getOrganization().setUrl(props.getProperty("pom.organization.url"));
                model.setIssueManagement(new IssueManagement());
                model.getIssueManagement().setUrl(props.getProperty("pom.issueManagement.url"));
                model.getIssueManagement().setSystem(props.getProperty("pom.issueManagement.system"));
                model.setCiManagement(new CiManagement());
                model.getCiManagement().setSystem(props.getProperty("pom.ciManagement.system"));
                model.getCiManagement().setUrl(props.getProperty("pom.ciManagement.url"));

                model.setScm(new Scm());
                model.getScm().setUrl(props.getProperty("pom.scm.url"));
                model.getScm().setConnection(props.getProperty("pom.scm.connection"));
                model.getScm().setDeveloperConnection(props.getProperty("pom.scm.developerConnection"));

                model.setDistributionManagement(new DistributionManagement());
                model.getDistributionManagement().setSite(new Site());
                model.getDistributionManagement().getSite().setId(props.getProperty("pom.distributionManagement.site.id"));
                model.getDistributionManagement().getSite().setUrl(props.getProperty("pom.distributionManagement.site.url"));

                model.setDevelopers(new ArrayList<>());
                int offset = 0;
                while (props.containsKey("pom.developers.developer." + offset + ".id")) {
                    Developer d = new Developer();
                    d.setId(props.getProperty("pom.developers.developer." + offset + ".id"));
                    d.setName(props.getProperty("pom.developers.developer." + offset + ".name"));
                    d.setRoles(new ArrayList<>());
                    int offset2 = 0;
                    while (props.containsKey("pom.developers.developer." + offset + ".role." + offset2)) {
                        d.getRoles().add(props.getProperty("pom.developers.developer." + offset + ".role." + offset2));
                        offset2++;
                    }
                    model.getDevelopers().add(d);
                    offset++;
                }

                if (model.getDependencies() == null) {
                    model.setDependencies(new ArrayList<>());
                }
                if (model.getPackaging() == null) {
                    model.setPackaging("jar");
                }

                if ("aar".equalsIgnoreCase(model.getPackaging())
                        || "apk".equalsIgnoreCase(model.getPackaging())
                        || "war".equalsIgnoreCase(model.getPackaging())) {
                    //locate the build.gradle file for this module
                    String artifactId = model.getArtifactId();
                    File gradle = new File(artifactId + "/build.gradle");
                    List<String> lines = FileUtils.readLines(gradle, "UTF-8");
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        line = line.trim();
                        if (line.startsWith("//")) {
                            continue;
                        }
                        if (line.startsWith("testImplementation")) {
                            //ignore it for now
                        }
                        if (line.startsWith("api")) {
                            line = line.replaceFirst("api ", "").trim();
                            if (line.startsWith("project(")) {
                                addProjectDependency(props, line, model.getDependencies());

                            } else {

                                line = line.replace("'", "");
                                line = line.replace("\"", "");
                                line = line.trim();
                                if (line.startsWith("(")) {
                                    line = line.substring(1);
                                }
                                Dependency d = new Dependency();

                                if (line.contains("group:") && line.contains("name:") && line.contains("version")) {
                                    String[] parts = line.replace("implementation", "").replace("api", "").
                                            trim().split(",");
                                    String g = null;
                                    String a = null;
                                    String v = null;
                                    for (String s : parts) {
                                        if (s.contains("group:")) {
                                            g = s.replace("group:", "").trim();
                                        }
                                        if (s.contains("name:")) {
                                            a = s.replace("name:", "").trim();
                                        }
                                        if (s.contains("version:")) {
                                            v = s.replace("version:", "").trim();
                                        }
                                    }
                                    if (g != null && a != null && v != null) {
                                        d.setGroupId(g);
                                        d.setArtifactId(a);
                                        d.setVersion(v);
                                        if (v.contains(")")) {
                                            d.setVersion(v.substring(0, v.indexOf(")")));
                                        }
                                        model.getDependencies().add(d);
                                    }
                                } else {
                                    String[] parts = line.split(":");
                                    d.setGroupId(parts[0]);
                                    d.setArtifactId(parts[1]);
                                    d.setVersion(parts[2]);
                                    if (parts[2].contains(")")) {
                                        d.setVersion(parts[2].substring(0, parts[2].indexOf(")")));
                                    }
                                    model.getDependencies().add(d);
                                }
                                if (line.endsWith("{")) {
                                    i++;
                                    line = lines.get(i).trim();
                                    while (!line.startsWith("}")) {
                                        if (line.contains("exclude")) {
                                            line = line.replaceFirst("exclude", "");
                                            String[] parts2 = line.trim().split("\\,");
                                            String group = null;
                                            String artifact = null;
                                            for (String s : parts2) {
                                                if (s.trim().startsWith("group:")) {
                                                    group = s.trim().replace("group:", "").replace("'", "").replace("\"", "").trim();
                                                }
                                                if (s.trim().startsWith("module:")) {
                                                    artifact = s.trim().replace("module:", "").replace("'", "").replace("\"", "").trim();
                                                }
                                            }
                                            if (group != null && artifact != null) {

                                                model.getDependencies().add(d);
                                                if (d.getExclusions() == null) {
                                                    d.setExclusions(new ArrayList<>());
                                                }
                                                Exclusion e = new Exclusion();
                                                e.setGroupId(group);
                                                e.setArtifactId(artifactId);
                                                d.getExclusions().add(e);
                                            }
                                        }
                                        //exclusions
                                        i++;
                                        line = lines.get(i).trim();
                                    }

                                }
                            }

                        }
                        if (line.startsWith("implementation")) {
                            line = line.replaceFirst("implementation ", "").trim();
                            if (line.startsWith("project(")) {
                                addProjectDependency(props, line, model.getDependencies());
                            } else {
                                line = line.replace("'", "");
                                line = line.replace("\"", "");
                                line = line.trim();
                                if (line.startsWith("(")) {
                                    line = line.substring(1);
                                }
                                Dependency d = new Dependency();

                                if (line.contains("group:") && line.contains("name:") && line.contains("version")) {
                                    String[] parts = line.replace("implementation", "").replace("api", "").
                                            trim().split(",");
                                    String g = null;
                                    String a = null;
                                    String v = null;
                                    for (String s : parts) {
                                        if (s.contains("group:")) {
                                            g = s.replace("group:", "").trim();
                                        }
                                        if (s.contains("name:")) {
                                            a = s.replace("name:", "").trim();
                                        }
                                        if (s.contains("version:")) {
                                            v = s.replace("version:", "").trim();
                                        }
                                    }
                                    if (g != null && a != null && v != null) {
                                        d.setGroupId(g);
                                        d.setArtifactId(a);
                                        d.setVersion(v);
                                        if (v.contains(")")) {
                                            d.setVersion(v.substring(0, v.indexOf(")")));
                                        }
                                        model.getDependencies().add(d);
                                    }
                                } else {
                                    String[] parts = line.split(":");
                                    d.setGroupId(parts[0]);
                                    d.setArtifactId(parts[1]);
                                    d.setVersion(parts[2]);
                                    if (parts[2].contains(")")) {
                                        d.setVersion(parts[2].substring(0, parts[2].indexOf(")")));
                                    }
                                    model.getDependencies().add(d);
                                }
                                if (line.endsWith("{")) {
                                    i++;
                                    line = lines.get(i).trim();
                                    while (!line.startsWith("}")) {
                                        if (line.contains("exclude")) {
                                            line = line.replaceFirst("exclude", "");
                                            String[] parts2 = line.trim().split("\\,");
                                            String group = null;
                                            String artifact = null;
                                            for (String s : parts2) {
                                                if (s.trim().startsWith("group:")) {
                                                    group = s.trim().replace("group:", "").replace("'", "").replace("\"", "").trim();
                                                }
                                                if (s.trim().startsWith("module:")) {
                                                    artifact = s.trim().replace("module:", "").replace("'", "").replace("\"", "").trim();
                                                }
                                            }
                                            if (group != null && artifact != null) {

                                                model.getDependencies().add(d);
                                                if (d.getExclusions() == null) {
                                                    d.setExclusions(new ArrayList<>());
                                                }
                                                Exclusion e = new Exclusion();
                                                e.setGroupId(group);
                                                e.setArtifactId(artifactId);
                                                d.getExclusions().add(e);
                                            }
                                        }
                                        //exclusions
                                        i++;
                                        line = lines.get(i).trim();
                                    }

                                }

                            }
                        }
                        // if (line.startsWith("compile")) {
                        //probably won't see this again
                        //}
                    }
                }

                MavenXpp3Writer writer = new MavenXpp3Writer();
                writer.write(new FileOutputStream(pom), model);
            }
        }
    }

    private static void signFiles(File target, Properties props) throws Exception {
        System.out.println("signing files");
        for (File f : target.listFiles()) {
            ProcessBuilder p = new ProcessBuilder(
                    props.getProperty("GPG_PATH"),
                    "-a",
                    "--output",
                    f.getAbsolutePath() + ".asc",
                    "--detach-sig",
                    f.getAbsolutePath()
            );

            Process proc = p.start();
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            proc.waitFor();
            System.out.println("Signing exit code for " + f.getName() + " was " + proc.exitValue());
            if (proc.exitValue() != 0) {
                printError();
                throw new Exception("signing failed for " + f.getAbsolutePath());
            }
        }
    }

    private static void addProjectDependency(Properties props, String line, List<Dependency> dependencies) {
        Dependency d = new Dependency();
        d.setGroupId(props.getProperty("pom.groupId"));
        d.setVersion(props.getProperty("pom.version"));
        String artifact = line.replace("project(':", "");
        artifact = artifact.replace("')", "");
        d.setArtifactId(artifact);
        dependencies.add(d);
    }

    private static void copyApksForDist(Properties props) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    static class StreamGobbler extends Thread {

        InputStream is;

        // reads everything from is until empty. 
        StreamGobbler(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * uploads a file to the given url borrowed from
     * <a href="http://stackoverflow.com/questions/11420971/send-a-file-with-a-put-request-using-a-httpurlconnection">here</a>
     *
     * @param input
     * @param urlDestination
     * @param contentType
     * @return
     */
    static void uploadFile(File input, String urlDestination, String contentType) throws Exception {
        System.out.println(urlDestination);

        try {
            URL url = new URL(urlDestination);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Content-Length", String.valueOf(input.length()));
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.connect();
            FileInputStream streamFileInputStream = new FileInputStream(input);
            BufferedInputStream streamFileBufferedInputStream = new BufferedInputStream(streamFileInputStream);
            OutputStream outputStream = connection.getOutputStream();
            byte[] streamFileBytes = new byte[4096];
            int bytesRead = 0;
            int totalBytesRead = 0;

            while ((bytesRead = streamFileBufferedInputStream.read(streamFileBytes)) > 0) {
                outputStream.write(streamFileBytes, 0, bytesRead);

                totalBytesRead += bytesRead;
                // notifyListenersOnProgress((double)totalBytesRead / (double)input.length());
            }

            outputStream.flush();
            outputStream.close();

            System.out.println("Upload " + urlDestination + " : " + connection.getResponseCode() + ": " + connection.getResponseMessage());
            if (connection.getResponseCode() >= 300) {
                StreamGobbler g = new StreamGobbler(connection.getInputStream());
                g.start();
            }
            connection.disconnect();

            //logger.debug("Wrote " + totalBytesRead + " bytes of " + input.length() + ", ratio: " + (double) totalBytesRead / (double) input.length());
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) {
                throw new Exception("unexcepted response code. Published failed. " + connection.getResponseCode() + ": " + connection.getResponseMessage());
            }
        } catch (Exception ex) {
            printError();
            throw new Exception("unexcepted exception. Published failed. " + ex.getMessage(), ex);
        }

    }

    static void printError() {
        System.out.println(
                "     _.-^^---....,,--\n"
                + " _--                  --_\n"
                + "<                        >)\n"
                + "|                         |\n"
                + " \\._                   _./\n"
                + "    ```--. . , ; .--'''\n"
                + "          | |   |\n"
                + "       .-=||  | |=-.\n"
                + "       `-=#\\$%&%\\$#=-'\n"
                + "          | ;  :|\n"
                + " _____.,-#%&\\$@%#&#~,._____ "
        );
    }

    private static void push(File target, Properties props) throws Exception {
        System.out.println("publishing to nexus repo");
        String password = props.getProperty("NEXUS_PASSWORD");
        if (mightBeEncrypted(password)) {
            password = tryDecrypt(password);
        }
        final String pwd = password;

        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty("NEXUS_USERNAME"),
                        pwd.toCharArray());
            }
        });

        String targetRepositoryUrl = props.getProperty("pom.version").contains("-SNAPSHOT")
                ? props.getProperty("SNAPSHOT_REPOSITORY_URL")
                : props.getProperty("RELEASE_REPOSITORY_URL");
        if (!targetRepositoryUrl.endsWith("/")) {
            targetRepositoryUrl = targetRepositoryUrl = "/";
        }
        String groupurl = props.getProperty("pom.groupId").replace(".", "/");
        File[] files = target.listFiles();

        for (File f : files) {
            String artifactId = f.getName().substring(0, f.getName().indexOf("-" + props.getProperty("pom.version")));
            if (f.getName().endsWith(".pom")) {
                uploadFile(f, targetRepositoryUrl + groupurl + "/" + artifactId + "/" + props.getProperty("pom.version")
                        + "/" + f.getName(), "text/xml; charset=utf-8");
            } else if (f.getName().endsWith(".md5") || f.getName().endsWith(".sha1") || f.getName().endsWith(".sha512")) {
                uploadFile(f, targetRepositoryUrl + groupurl + "/" + artifactId + "/" + props.getProperty("pom.version")
                        + "/" + f.getName(), "text/plain; charset=utf-8");
            } else {
                uploadFile(f, targetRepositoryUrl + groupurl + "/" + artifactId + "/" + props.getProperty("pom.version")
                        + "/" + f.getName(), "application/octet-stream; charset=utf-8");
            }
        }
    }

// crypto support
    static byte[] hexToBytes(String s) {
        //return s.getBytes();
        return hexToBytes(s.toCharArray());
    }

    /**
     * gets the master key
     *
     * @return
     */
    static String loadKey() throws FileNotFoundException, IOException {
        String users_home = System.getProperty("user.home");
        users_home = users_home.replace("\\", "/"); // to support all platforms.
        users_home = users_home + "/.gradle/";

        File gradlehome = new File(users_home);
        //check for existing master key
        File keys = new File(gradlehome.getAbsolutePath() + "/fury-keys.properties");

        if (keys.exists()) {
            //defined in rooDir/local.properties file
            Properties properties = new Properties();

            properties.load(new FileInputStream(keys));
            if (properties.containsKey("FURY_MASTER_PASSWORD")) {
                return properties.getProperty("FURY_MASTER_PASSWORD");
            }
        }

        return "";
    }

    static String tryDecrypt(String ciphertext) {
        //we're using {cipher} so trim off the first and last char
        String textToDecrypt = ciphertext.substring(1, ciphertext.length() - 1);
        try {
            String text = decrypt(textToDecrypt, loadKey());
            return text;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ciphertext;
    }

    static String decrypt(String ciphertext, String key) throws Exception {
        byte[] raw = hexToBytes(key); //
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher.doFinal(hexToBytes(ciphertext));
        return new String(original);
    }

    static byte[] hexToBytes(char[] hex) {
        int length = hex.length / 2;
        byte[] raw = new byte[length];
        for (int i = 0; i < length; i++) {
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            int value = (high << 4) | low;
            if (value > 127) {
                value -= 256;
            }
            raw[i] = (byte) value;
        }
        return raw;
    }

    static boolean mightBeEncrypted(String text) {
        if (text == null) {
            return false;
        }
        if (text.length() == 0) {
            return false;
        }
        if (text.startsWith("{") && text.endsWith("}")) {
            return true;
        }
        return false;
    }
}
