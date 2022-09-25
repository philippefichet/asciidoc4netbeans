/*
 * asciidoc4netbeans: asciidoctorj integration for Apache Netbeans
 * Copyright (C) 2023 Philippe FICHET.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.github.philippefichet.asciidoc4netbeans;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Utilities;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class AsciidocEngineUtils {
    private static final Logger LOG = Logger.getLogger(AsciidocEngineUtils.class.getName());

    private AsciidocEngineUtils() {
    }

    /**
     * Search a best path for base directory
     * @param asciidoc
     * @return 
     */
    public static File findBestBaseDirectory(File asciidoc)
    {
        Project owner = FileOwnerQuery.getOwner(asciidoc.toURI());
        if (owner != null) {
            return new File(owner.getProjectDirectory().getPath());
        }
        return asciidoc.getParentFile();
    }

    /**
     * Search and create the best path for output directorybased on {@link findBestBaseDirectory}.
     * @param asciidoc
     * @return 
     */
    public static File findBestOutputDirectory(File asciidoc)
    {
        File asciidoc4netbeansOutputFile = findBestOutputDirectoryWithoutMkdirs(asciidoc);
        asciidoc4netbeansOutputFile.mkdir();
        return asciidoc4netbeansOutputFile;
    }

    private static File findBestOutputDirectoryWithoutMkdirs(File asciidoc)
    {
        File findBestBaseDirectory = findBestBaseDirectory(asciidoc);
        for (File file : findBestBaseDirectory.listFiles()) {
            String name = file.getName();
            if ("target".equals(name)
                || "build".equals(name)) {
                return new File(file, "asciidoc4netbeans");
            }
            if ("pom.xml".equals(name)) {
                return new File(findBestBaseDirectory, "target/asciidoc4netbeans");
            }
            if ("build.gradle".equals(name)) {
                return new File(findBestBaseDirectory, "build/asciidoc4netbeans");
            }
        }
        return new File(findBestBaseDirectory, ".asciidoc4netbeans");
    }

    public static ClassLoader createClassLoaderFromNetbeansModule() {
        ModuleInfo findCodeNameBase = Modules.getDefault().findCodeNameBase("com.github.philippefichet.asciidoc4netbeans");
        if (findCodeNameBase instanceof org.netbeans.Module) {
            List<URL> resources = new ArrayList<>();
            org.netbeans.Module module = ((org.netbeans.Module)findCodeNameBase);
            List<File> jars = module.getAllJars();
            for (File jar : jars) {
                try {
                    resources.add(Utilities.toURI(jar).toURL());
                    LOG.info("Netbeans module class path with : " + Utilities.toURI(jar).toURL());
                } catch (MalformedURLException ex) {
                    throw new IllegalStateException("Malformed URL from module jar : " + jar.toString());
                }
            }
            return new URLClassLoader(resources.toArray(URL[]::new), findCodeNameBase.getClassLoader());
        }

        if (findCodeNameBase == null) {
            throw new IllegalStateException("Unable to create Asciidoc engine, not found netbean module");
        } else {
            throw new IllegalStateException("Unable to create Asciidoc engine on netbean module : " + findCodeNameBase.getClass().getCanonicalName());
        }
    }
}
