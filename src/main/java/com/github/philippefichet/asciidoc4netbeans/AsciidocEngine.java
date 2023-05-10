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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.jruby.exceptions.LoadError;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@ServiceProvider(service = AsciidocEngine.class)
public class AsciidocEngine {
    private static final Logger LOG = Logger.getLogger(AsciidocEngine.class.getName());

    private JRubyAsciidoctor asciidoctor;
    private AsciidocEngineStatus asciidocEngineStatus = AsciidocEngineStatus.NOT_STARTED;
    private final ClassLoader classloader;
    private final List<AsciidocEngineStatusListener> initializationListener = new ArrayList<>();

    /**
     * Constructor to create engine from Netbeans module
     * Used to register a ServiceProvider
     */
    public AsciidocEngine() {
        this(AsciidocEngineUtils.createClassLoaderFromNetbeansModule());
    }

    /**
     * Constructor to create engine from an arbitrary classloader
     * @param classloader class loader used to load AsciidoctorJ
     */
    public AsciidocEngine(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public AsciidocEngineStatus getStatus() {
        return asciidocEngineStatus;
    }

    /**
     * Add a status listener removed when engine is started
     * @param listener status listener removed when engine is started
     */
    public void addEphemeralListener(AsciidocEngineStatusListener listener) {
        initializationListener.add(listener);
        listener.statusChange(asciidocEngineStatus);
    }

    /**
     * Block thread unil engine is started
     */
    public void waitingInitialization() {
        while (asciidoctor == null) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                Logger.getLogger(AsciidocEngine.class.getName()).log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            }
        }
        asciidocEngineStatus = AsciidocEngineStatus.STARTED;
        while (!initializationListener.isEmpty()) {
            initializationListener.remove(0).statusChange(asciidocEngineStatus);
        }
    }

    private void createInternalEngine(JRubyAsciidoctor oldAsciidoctor) {
        if (oldAsciidoctor != null) {
            oldAsciidoctor.close();
        }
        asciidoctor = JRubyAsciidoctor.create(classloader);
        try {
            asciidoctor.requireLibrary("asciidoctor-diagram");
        } catch (LoadError error) {
            LOG.warning("Error while loarding \"asciidoctor-diagram\" : " + error.getMessage());
        }
    }
    
    public void startAsciidocEngine() {
        if (asciidocEngineStatus == AsciidocEngineStatus.NOT_STARTED) {
            asciidocEngineStatus = AsciidocEngineStatus.START_INITIALIZED;
            initializationListener.forEach(listener -> listener.statusChange(asciidocEngineStatus));
            createInternalEngine(asciidoctor);
        }
    }

    public String convert(String content, File baseDirFolder, File outputDirectory, String theme, boolean isDark) {
        if (asciidoctor == null && asciidocEngineStatus == AsciidocEngineStatus.STARTED) {
            return null;
        }
        startAsciidocEngine();
        waitingInitialization();
        return asciidoctor.convert(
            content,
            Options.builder()
                .backend("html5")
                .compact(false)
                .baseDir(baseDirFolder)
                .mkDirs(true)
                .headerFooter(true)
                .attributes(
                    Attributes.builder()
                    .attribute("outdir", outputDirectory.getAbsolutePath())
                    .attribute("linkcss")
                    .attribute("stylesheet", "/themes/" + theme + "/asciidoctor" + (isDark ? "-dark" : "") + ".css")
                    .attribute("source-highlighter", "highlight.js")
                    .attribute("highlightjs-theme", (isDark ? "androidstudio" : "default"))
                    .build()
                )
                .safe(SafeMode.SAFE)
                .build()
        );
    }
}
