/********************************************************************************
 * Copyright (c) 2012-2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 ********************************************************************************/
package org.eclipse.winery.repository.backend.filebased;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.winery.common.RepositoryFileReference;
import org.eclipse.winery.common.Util;
import org.eclipse.winery.common.configuration.Environments;
import org.eclipse.winery.common.ids.GenericId;
import org.eclipse.winery.common.ids.Namespace;
import org.eclipse.winery.common.ids.XmlId;
import org.eclipse.winery.common.ids.admin.AccountabilityId;
import org.eclipse.winery.common.ids.admin.NamespacesId;
import org.eclipse.winery.common.ids.definitions.DefinitionsChildId;
import org.eclipse.winery.common.ids.elements.ToscaElementId;
import org.eclipse.winery.common.version.VersionUtils;
import org.eclipse.winery.common.version.WineryVersion;
import org.eclipse.winery.model.tosca.Definitions;
import org.eclipse.winery.model.tosca.HasIdInIdOrNameField;
import org.eclipse.winery.repository.Constants;
import org.eclipse.winery.repository.backend.AbstractRepository;
import org.eclipse.winery.repository.backend.AccountabilityConfigurationManager;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.backend.IRepositoryAdministration;
import org.eclipse.winery.repository.backend.NamespaceManager;
import org.eclipse.winery.repository.backend.RepositoryFactory;
import org.eclipse.winery.repository.backend.constants.MediaTypes;
import org.eclipse.winery.repository.backend.xsd.RepositoryBasedXsdImportManager;
import org.eclipse.winery.repository.backend.xsd.XsdImportManager;
import org.eclipse.winery.common.configuration.FileBasedRepositoryConfiguration;
import org.eclipse.winery.repository.exceptions.WineryRepositoryException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tika.mime.MediaType;
import org.eclipse.jgit.dircache.InvalidPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When it comes to a storage of plain files, we use Java 7's nio internally. Therefore, we intend to expose the stream
 * types offered by java.nio.Files: BufferedReader/BufferedWriter
 */
public class FilebasedRepository extends AbstractRepository implements IRepositoryAdministration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilebasedRepository.class);

    protected final Path repositoryRoot;

    // convenience variables to have a clean code
    private final FileSystem fileSystem;

    private final FileSystemProvider provider;

    /**
     * @param fileBasedRepositoryConfiguration configuration of the filebased repository. The contained repositoryPath
     *                                         may be null
     */
    public FilebasedRepository(FileBasedRepositoryConfiguration fileBasedRepositoryConfiguration) {
        Objects.requireNonNull(fileBasedRepositoryConfiguration);
        this.repositoryRoot = getRepositoryRoot(fileBasedRepositoryConfiguration);
        this.fileSystem = this.repositoryRoot.getFileSystem();
        this.provider = this.fileSystem.provider();
        LOGGER.debug("Repository root: {}", this.repositoryRoot);
    }

    public static Path getRepositoryRoot(FileBasedRepositoryConfiguration fileBasedRepositoryConfiguration) {
        if (fileBasedRepositoryConfiguration.getRepositoryPath().isPresent()) {
            return makeAbsoluteAndCreateRepositoryPath(fileBasedRepositoryConfiguration.getRepositoryPath().get());
        } else {
            Path newPath = determineAndCreateRepositoryPath();
            Environments.setRepositoryRoot(newPath.toString());
            return newPath;
        }
    }

    private Path makeAbsolute(Path relativePath) {
        return this.repositoryRoot.resolve(relativePath);
    }

    /**
     * @return the currently configured repository root
     */
    public Path getRepositoryRoot() {
        return repositoryRoot;
    }

    @Override
    public boolean flagAsExisting(GenericId id) {
        Path path = this.id2AbsolutePath(id);
        try {
            FileUtils.createDirectory(path);
        } catch (IOException e) {
            FilebasedRepository.LOGGER.debug(e.toString());
            return false;
        }
        return true;
    }

    private Path id2AbsolutePath(GenericId id) {
        Path relativePath = this.fileSystem.getPath(Util.getPathInsideRepo(id));
        return this.makeAbsolute(relativePath);
    }

    /**
     * Converts the given reference to an absolute path of the underlying FileSystem
     */
    public Path ref2AbsolutePath(RepositoryFileReference ref) {
        Path resultPath = this.id2AbsolutePath(ref.getParent());
        final Optional<Path> subDirectory = ref.getSubDirectory();
        if (subDirectory.isPresent()) {
            resultPath = resultPath.resolve(subDirectory.get());
        }
        return resultPath.resolve(ref.getFileName());
    }

    protected static Path determineAndCreateRepositoryPath() {
        Path repositoryPath;
        if (SystemUtils.IS_OS_WINDOWS) {
            if (Files.exists(Constants.GLOBAL_REPO_PATH_WINDOWS)) {
                repositoryPath = Constants.GLOBAL_REPO_PATH_WINDOWS;
            } else {
                repositoryPath = createDefaultRepositoryPath();
            }
        } else {
            repositoryPath = createDefaultRepositoryPath();
        }
        return repositoryPath;
    }

    protected static Path makeAbsoluteAndCreateRepositoryPath(final Path configuredRepositoryPath) {
        Objects.requireNonNull(configuredRepositoryPath);
        Path repositoryPath = configuredRepositoryPath.toAbsolutePath().normalize();
        try {
            org.apache.commons.io.FileUtils.forceMkdir(repositoryPath.toFile());
        } catch (IOException e) {
            FilebasedRepository.LOGGER.error("Could not create repository directory", e);
        }
        return repositoryPath;
    }

    public static File getDefaultRepositoryFilePath() {
        return new File(org.apache.commons.io.FileUtils.getUserDirectory(), Constants.DEFAULT_REPO_NAME);
    }

    private static Path createDefaultRepositoryPath() {
        File repo = null;
        boolean operationalFileSystemAccess;
        try {
            repo = FilebasedRepository.getDefaultRepositoryFilePath();
            operationalFileSystemAccess = true;
        } catch (NullPointerException e) {
            // it seems, we run at a system, where we do not have any filesystem
            // access
            operationalFileSystemAccess = false;
        }

        // operationalFileSystemAccess = false;

        Path repositoryPath;
        if (operationalFileSystemAccess) {
            try {
                org.apache.commons.io.FileUtils.forceMkdir(repo);
            } catch (IOException e) {
                FilebasedRepository.LOGGER.error("Could not create directory", e);
            }
            repositoryPath = repo.toPath();
        } else {
            // we do not have access to the file system
            throw new IllegalStateException("No write access to file system");
        }

        return repositoryPath;
    }

    @Override
    public void forceDelete(RepositoryFileReference ref) throws IOException {
        Path relativePath = this.fileSystem.getPath(BackendUtils.getPathInsideRepo(ref));
        Path fileToDelete = this.makeAbsolute(relativePath);
        try {
            this.provider.delete(fileToDelete);
            // Quick hack for deletion of the mime type information
            // Alternative: superclass: protected void deleteMimeTypeInformation(RepositoryFileReference ref) throws IOException
            // However, this would again call this method, where we would have to check for the extension, too.
            // Therefore, we directly delete the information file
            Path mimeTypeFile = fileToDelete.getParent().resolve(ref.getFileName() + Constants.SUFFIX_MIMETYPE);
            this.provider.delete(mimeTypeFile);
        } catch (IOException e) {
            if (!(e instanceof NoSuchFileException)) {
                // only if file did exist and something else went wrong: complain :)
                // (otherwise, silently ignore the error)
                FilebasedRepository.LOGGER.debug("Could not delete file", e);
                throw e;
            }
        }
    }

    @Override
    public void forceDelete(GenericId id) {
        FileUtils.forceDelete(this.id2AbsolutePath(id));
    }

    @Override
    public void rename(DefinitionsChildId oldId, DefinitionsChildId newId) throws IOException {
        this.duplicate(oldId, newId, true);
    }

    @Override
    public void duplicate(DefinitionsChildId from, DefinitionsChildId newId) throws IOException {
        this.duplicate(from, newId, false);
    }

    private void duplicate(DefinitionsChildId oldId, DefinitionsChildId newId, boolean moveOnly) throws IOException {
        Objects.requireNonNull(oldId);
        Objects.requireNonNull(newId);

        if (oldId.equals(newId)) {
            // we do not do anything - even not throwing an error
            return;
        }

        Definitions definitions = this.getDefinitions(oldId);

        RepositoryFileReference oldRef = BackendUtils.getRefOfDefinitions(oldId);
        RepositoryFileReference newRef = BackendUtils.getRefOfDefinitions(newId);

        // oldRef points to the definitions file,
        // getParent() returns the directory
        // we need toFile(), because we rely on FileUtils.moveDirectoryToDirectory
        File oldDir = this.id2AbsolutePath(oldRef.getParent()).toFile();
        File newDir = this.id2AbsolutePath(newRef.getParent()).toFile();

        if (moveOnly) {
            org.apache.commons.io.FileUtils.moveDirectory(oldDir, newDir);
        } else {
            org.apache.commons.io.FileUtils.copyDirectory(oldDir, newDir);
        }

        // Update definitions and store it

        // This also updates the definitions of componentInstanceResource
        BackendUtils.updateWrapperDefinitions(newId, definitions);

        // This works, because the definitions object here is the same as the definitions object treated at copyIdToFields
        // newId has to be passed, because the id is final at AbstractComponentInstanceResource
        BackendUtils.copyIdToFields((HasIdInIdOrNameField) definitions.getElement(), newId);

        try {
            BackendUtils.persist(definitions, newRef, MediaTypes.MEDIATYPE_TOSCA_DEFINITIONS);
        } catch (InvalidPathException e) {
            LOGGER.debug("Invalid path during write", e);
            // QUICK FIX
            // Somewhere, the first letter is deleted --> /odetypes/http%3A%2F%2Fwww.example.org%2F05/
            // We just ignore it for now
        }
    }

    public void forceDelete(Class<? extends DefinitionsChildId> definitionsChildIdClazz, Namespace namespace) {
        // instantiate new definitions child id with "ID" as id
        // this is used to get the absolute path
        DefinitionsChildId id = BackendUtils.getDefinitionsChildId(definitionsChildIdClazz, namespace.getEncoded(), "ID", true);

        Path path = this.id2AbsolutePath(id);

        // do not delete the id, delete the complete namespace
        // patrent folder is the namespace folder
        path = path.getParent();
        FileUtils.forceDelete(path);
    }

    @Override
    public boolean exists(GenericId id) {
        Path absolutePath = this.id2AbsolutePath(id);
        return Files.exists(absolutePath);
    }

    @Override
    public void putContentToFile(RepositoryFileReference ref, String content, MediaType mediaType) throws IOException {
        if (mediaType == null) {
            // quick hack for storing mime type called this method
            assert (ref.getFileName().endsWith(Constants.SUFFIX_MIMETYPE));
            // we do not need to store the mime type of the file containing the mime type information
        } else {
            this.setMimeType(ref, mediaType);
        }
        Path path = this.ref2AbsolutePath(ref);
        FileUtils.createDirectory(path.getParent());
        Files.write(path, content.getBytes());
    }

    @Override
    public void putContentToFile(RepositoryFileReference ref, InputStream inputStream, MediaType mediaType) throws IOException {
        if (mediaType == null) {
            // quick hack for storing mime type called this method
            assert (ref.getFileName().endsWith(Constants.SUFFIX_MIMETYPE));
            // we do not need to store the mime type of the file containing the mime type information
        } else {
            this.setMimeType(ref, mediaType);
        }
        Path targetPath = this.ref2AbsolutePath(ref);
        // ensure that parent directory exists
        FileUtils.createDirectory(targetPath.getParent());

        try {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IllegalStateException e) {
            FilebasedRepository.LOGGER.debug("Guessing that stream with length 0 is to be written to a file", e);
            // copy throws an "java.lang.IllegalStateException: Stream already closed" if the InputStream contains 0 bytes
            // For instance, this case happens if SugarCE-6.4.2.zip.removed is tried to be uploaded
            // We work around the Java7 issue and create an empty file
            if (Files.exists(targetPath)) {
                // semantics of putContentToFile: existing content is replaced without notification
                Files.delete(targetPath);
            }
            Files.createFile(targetPath);
        }
    }

    @Override
    public boolean exists(RepositoryFileReference ref) {
        return Files.exists(this.ref2AbsolutePath(ref));
    }

    @Override
    public <T extends DefinitionsChildId> SortedSet<T> getAllDefinitionsChildIds(Class<T> idClass) {
        return getDefinitionsChildIds(idClass, false);
    }

    public <T extends DefinitionsChildId> SortedSet<T> getStableDefinitionsChildIdsOnly(Class<T> idClass) {
        return getDefinitionsChildIds(idClass, true);
    }

    private <T extends DefinitionsChildId> SortedSet<T> getDefinitionsChildIds(Class<T> idClass, boolean omitDevelopmentVersions) {
        SortedSet<T> res = new TreeSet<>();
        String rootPathFragment = Util.getRootPathFragment(idClass);
        Path dir = this.repositoryRoot.resolve(rootPathFragment);
        if (!Files.exists(dir)) {
            // return empty list if no ids are available
            return res;
        }
        assert (Files.isDirectory(dir));

        final OnlyNonHiddenDirectories onhdf = new OnlyNonHiddenDirectories();

        // list all directories contained in this directory
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, onhdf)) {
            for (Path nsP : ds) {
                // the current path is the namespace
                Namespace ns = new Namespace(nsP.getFileName().toString(), true);
                try (DirectoryStream<Path> idDS = Files.newDirectoryStream(nsP, onhdf)) {
                    for (Path idP : idDS) {
                        XmlId xmlId = new XmlId(idP.getFileName().toString(), true);
                        if (omitDevelopmentVersions) {
                            WineryVersion version = VersionUtils.getVersion(xmlId.getDecoded());

                            if (version.toString().length() > 0 && version.getWorkInProgressVersion() > 0) {
                                continue;
                            }
                        }
                        Constructor<T> constructor;
                        try {
                            constructor = idClass.getConstructor(Namespace.class, XmlId.class);
                        } catch (Exception e) {
                            FilebasedRepository.LOGGER.debug("Internal error at determining id constructor", e);
                            // abort everything, return invalid result
                            return res;
                        }
                        T id;
                        try {
                            id = constructor.newInstance(ns, xmlId);
                        } catch (InstantiationException
                            | IllegalAccessException
                            | IllegalArgumentException
                            | InvocationTargetException e) {
                            FilebasedRepository.LOGGER.debug("Internal error at invocation of id constructor", e);
                            // abort everything, return invalid result
                            return res;
                        }
                        res.add(id);
                    }
                }
            }
        } catch (IOException e) {
            FilebasedRepository.LOGGER.debug("Cannot close ds", e);
        }

        return res;
    }

    @Override
    public SortedSet<RepositoryFileReference> getContainedFiles(GenericId id) {
        Path dir = this.id2AbsolutePath(id);
        SortedSet<RepositoryFileReference> res = new TreeSet<>();
        if (!Files.exists(dir)) {
            return res;
        }
        assert (Files.isDirectory(dir));

        final OnlyNonHiddenFiles onlyNonHiddenFiles = new OnlyNonHiddenFiles();
        try {
            Files.walk(dir).filter(f -> {
                try {
                    return onlyNonHiddenFiles.accept(f);
                } catch (IOException e) {
                    LOGGER.debug("Error during crawling", e);
                    return false;
                }
            }).map(f -> {
                final Path relativePath = dir.relativize(f.getParent());
                if (relativePath.toString().isEmpty()) {
                    return new RepositoryFileReference(id, f.getFileName().toString());
                } else {
                    return new RepositoryFileReference(id, relativePath, f.getFileName().toString());
                }
            }).forEach(ref -> res.add(ref));
        } catch (IOException e1) {
            LOGGER.debug("Error during crawling", e1);
        }

        return res;
    }

    @Override
    public Configuration getConfiguration(RepositoryFileReference ref) {
        Path path = this.ref2AbsolutePath(ref);

        PropertiesConfiguration configuration = new PropertiesConfiguration();
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path, Charset.defaultCharset())) {
                configuration.read(r);
            } catch (ConfigurationException | IOException e) {
                FilebasedRepository.LOGGER.error("Could not read config file", e);
                throw new IllegalStateException("Could not read config file", e);
            }
        }

        configuration.addEventListener(ConfigurationEvent.ANY, new AutoSaveListener(path, configuration));

        // We do NOT implement reloading as the configuration is only accessed
        // in JAX-RS resources, which are created on a per-request basis

        return configuration;
    }

    /**
     * @return null if an error occurred
     */
    @Override
    public Date getLastUpdate(RepositoryFileReference ref) {
        Path path = this.ref2AbsolutePath(ref);
        Date res;
        if (Files.exists(path)) {
            FileTime lastModifiedTime;
            try {
                lastModifiedTime = Files.getLastModifiedTime(path);
                res = new Date(lastModifiedTime.toMillis());
            } catch (IOException e) {
                FilebasedRepository.LOGGER.debug(e.getMessage(), e);
                res = null;
            }
        } else {
            // this branch is taken if the resource directory exists, but the
            // configuration itself does not exist.
            // For instance, this happens if icons are manually put for a node
            // type, but no color configuration is made.
            res = Constants.LASTMODIFIEDDATE_FOR_404;
        }
        return res;
    }

    @Override
    public <T extends ToscaElementId> SortedSet<T> getNestedIds(GenericId ref, Class<T> idClass) {
        Path dir = this.id2AbsolutePath(ref);
        SortedSet<T> res = new TreeSet<>();
        if (!Files.exists(dir)) {
            // the id has been generated by the exporter without existance test.
            // This test is done here.
            return res;
        }
        assert (Files.isDirectory(dir));
        // list all directories contained in this directory
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, new OnlyNonHiddenDirectories())) {
            for (Path p : ds) {
                XmlId xmlId = new XmlId(p.getFileName().toString(), true);
                @SuppressWarnings("unchecked")
                Constructor<T>[] constructors = (Constructor<T>[]) idClass.getConstructors();
                assert (constructors.length == 1);
                Constructor<T> constructor = constructors[0];
                assert (constructor.getParameterTypes().length == 2);
                T id;
                try {
                    id = constructor.newInstance(ref, xmlId);
                } catch (InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                    FilebasedRepository.LOGGER.debug("Internal error at invocation of id constructor", e);
                    // abort everything, return invalid result
                    return res;
                }
                res.add(id);
            }
        } catch (IOException e) {
            FilebasedRepository.LOGGER.debug("Cannot close ds", e);
        }
        return res;
    }

    @Override
    public Collection<Namespace> getUsedNamespaces() {
        return getNamespaces(DefinitionsChildId.ALL_TOSCA_COMPONENT_ID_CLASSES);
    }

    @Override
    public Collection<Namespace> getComponentsNamespaces(Class<? extends DefinitionsChildId> clazz) {
        Collection<Class<? extends DefinitionsChildId>> list = new ArrayList<>();
        list.add(clazz);
        return getNamespaces(list);
    }

    @Override
    public NamespaceManager getNamespaceManager() {
        NamespaceManager manager;
        RepositoryFileReference ref = BackendUtils.getRefOfJsonConfiguration(new NamespacesId());
        manager = new JsonBasedNamespaceManager(ref2AbsolutePath(ref).toFile());
        Configuration configuration = this.getConfiguration(new NamespacesId());

        if (!configuration.isEmpty()) {
            ConfigurationBasedNamespaceManager old = new ConfigurationBasedNamespaceManager(configuration);
            manager.replaceAll(old.getAllNamespaces());
            try {
                forceDelete(BackendUtils.getRefOfConfiguration(new NamespacesId()));
            } catch (IOException e) {
                LOGGER.error("Could not remove old namespace configuration.", e);
            }
        }

        return manager;
    }

    @Override
    public AccountabilityConfigurationManager getAccountabilityConfigurationManager() {
        RepositoryFileReference repoRef = BackendUtils.getRefOfConfiguration(new AccountabilityId());
        RepositoryFileReference keystoreRef = new RepositoryFileReference(new AccountabilityId(), "CustomKeystore.json");
        RepositoryFileReference defaultKeystoreRef = new RepositoryFileReference(new AccountabilityId(), "DefaultKeystore.json");

        return AccountabilityConfigurationManager.getInstance(ref2AbsolutePath(repoRef).toFile(), 
            ref2AbsolutePath(keystoreRef).toFile(), ref2AbsolutePath(defaultKeystoreRef).toFile());
    }

    @Override
    public XsdImportManager getXsdImportManager() {
        return new RepositoryBasedXsdImportManager();
    }

    private Collection<Namespace> getNamespaces(Collection<Class<? extends DefinitionsChildId>> definitionsChildIds) {
        // we use a HashSet to avoid reporting duplicate namespaces
        Collection<Namespace> res = new HashSet<>();

        for (Class<? extends DefinitionsChildId> id : definitionsChildIds) {
            String rootPathFragment = Util.getRootPathFragment(id);
            Path dir = this.repositoryRoot.resolve(rootPathFragment);
            if (!Files.exists(dir)) {
                continue;
            }
            assert (Files.isDirectory(dir));

            final OnlyNonHiddenDirectories onhdf = new OnlyNonHiddenDirectories();

            // list all directories contained in this directory
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, onhdf)) {
                for (Path nsP : ds) {
                    // the current path is the namespace
                    Namespace ns = new Namespace(nsP.getFileName().toString(), true);
                    res.add(ns);
                }
            } catch (IOException e) {
                FilebasedRepository.LOGGER.debug("Cannot close ds", e);
            }
        }
        return res;
    }

    public Collection<? extends DefinitionsChildId> getAllIdsInNamespace(Class<? extends DefinitionsChildId> clazz, Namespace namespace) {
        Collection<DefinitionsChildId> result = new HashSet<>();
        String rootPathFragment = Util.getRootPathFragment(clazz);
        Path dir = this.repositoryRoot.resolve(rootPathFragment);
        dir = dir.resolve(namespace.getEncoded());
        if (Files.exists(dir) && Files.isDirectory(dir)) {

            DirectoryStream<Path> directoryStream = null;
            try {
                directoryStream = Files.newDirectoryStream(dir);

                for (Path path : directoryStream) {
                    Constructor<? extends DefinitionsChildId> constructor = null;

                    constructor = clazz.getConstructor(String.class, String.class, boolean.class);

                    DefinitionsChildId definitionsChildId = constructor.newInstance(namespace.getDecoded(), path.getFileName().toString(), false);
                    result.add(definitionsChildId);
                }
                directoryStream.close();
            } catch (IOException e) {
                FilebasedRepository.LOGGER.debug("Cannot close ds", e);
            } catch (NoSuchMethodException e) {
                FilebasedRepository.LOGGER.debug("Cannot find constructor", e);
            } catch (InstantiationException e) {
                FilebasedRepository.LOGGER.debug("Cannot instantiate object", e);
            } catch (IllegalAccessException e) {
                FilebasedRepository.LOGGER.debug("IllegalAccessException", e);
            } catch (InvocationTargetException e) {
                FilebasedRepository.LOGGER.debug("InvocationTargetException", e);
            }
        }
        return result;
    }

    @Override
    public void doDump(OutputStream out) throws IOException {
        final ZipOutputStream zout = new ZipOutputStream(out);
        final int cutLength = this.repositoryRoot.toString().length() + 1;

        Files.walkFileTree(this.repositoryRoot, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.endsWith(".git")) {
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String name = file.toString().substring(cutLength);
                ZipEntry ze = new ZipEntry(name);
                try {
                    ze.setTime(Files.getLastModifiedTime(file).toMillis());
                    ze.setSize(Files.size(file));
                    zout.putNextEntry(ze);
                    Files.copy(file, zout);
                    zout.closeEntry();
                } catch (IOException e) {
                    FilebasedRepository.LOGGER.debug(e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        zout.close();
    }

    /**
     * Removes all files and dirs except the .git directory
     */
    @Override
    public void doClear() {
        try {
            DirectoryStream.Filter<Path> noGitDirFilter = entry -> !(entry.getFileName().toString().equals(".git"));

            try (DirectoryStream<Path> ds = Files.newDirectoryStream(this.repositoryRoot, noGitDirFilter)) {
                for (Path p : ds) {
                    FileUtils.forceDelete(p);
                }
            }
        } catch (IOException e) {
            FilebasedRepository.LOGGER.error(e.getMessage());
        }
    }

    /**
     * Removes the repository completely, even with the .git directory
     */
    public void forceClear() {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(this.repositoryRoot)) {
            for (Path p : ds) {
                FileUtils.forceDelete(p);
            }
        } catch (IOException e) {
            FilebasedRepository.LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void doImport(InputStream in) {
        ZipInputStream zis = new ZipInputStream(in);
        ZipEntry entry;
        try {
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path path = this.repositoryRoot.resolve(entry.getName());
                    try {
                        FileUtils.createDirectory(path.getParent());
                        try {
                            Files.copy(zis, path, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            FilebasedRepository.LOGGER.error("Files.copy did not succeed", e);
                        }
                    } catch (IOException e) {
                        FilebasedRepository.LOGGER.error("Directory creation did not succeed", e);
                    }
                }
            }
        } catch (IOException e) {
            FilebasedRepository.LOGGER.error("Unzipping did not succeed", e);
        }
    }

    @Override
    public long getSize(RepositoryFileReference ref) throws IOException {
        return Files.size(this.ref2AbsolutePath(ref));
    }

    @Override
    public FileTime getLastModifiedTime(RepositoryFileReference ref) throws IOException {
        Path path = this.ref2AbsolutePath(ref);
        return Files.getLastModifiedTime(path);
    }

    @Override
    public InputStream newInputStream(RepositoryFileReference ref) throws IOException {
        Path path = this.ref2AbsolutePath(ref);
        return Files.newInputStream(path);
    }

    @Override
    public void getZippedContents(final GenericId id, OutputStream out) throws WineryRepositoryException {
        Objects.requireNonNull(id);
        Objects.requireNonNull(out);

        SortedSet<RepositoryFileReference> containedFiles = this.getContainedFiles(id);

        try (final ZipOutputStream zos = new ZipOutputStream(out)) {
            for (RepositoryFileReference ref : containedFiles) {
                ZipEntry zipArchiveEntry;
                final Optional<Path> subDirectory = ref.getSubDirectory();
                if (subDirectory.isPresent()) {
                    zipArchiveEntry = new ZipEntry(subDirectory.get().resolve(ref.getFileName()).toString());
                } else {
                    zipArchiveEntry = new ZipEntry(ref.getFileName());
                }
                zos.putNextEntry(zipArchiveEntry);
                try (InputStream is = RepositoryFactory.getRepository().newInputStream(ref)) {
                    IOUtils.copy(is, zos);
                }
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new WineryRepositoryException("I/O exception during export", e);
        }
    }
}
