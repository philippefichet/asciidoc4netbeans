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
package com.github.philippefichet.asciidoc4netbeans.server;

import com.github.philippefichet.asciidoc4netbeans.AsciidocEngine;
import com.github.philippefichet.asciidoc4netbeans.AsciidocEngineUtils;
import com.github.philippefichet.asciidoc4netbeans.AsciidocUtils;
import io.jooby.AssetHandler;
import io.jooby.AssetSource;
import io.jooby.ExecutionMode;
import io.jooby.Jooby;
import io.jooby.MediaType;
import io.jooby.Server;
import io.jooby.ServerOptions;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.codec.binary.Hex;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@ServiceProvider(service = HttpServer.class)
public class HttpServer {

    private static final RequestProcessor RP = new RequestProcessor(HttpServer.class);
    private final Map<String, HttpServerContextConfiguration> contextConfigurations = new ConcurrentHashMap<>();
    private final Map<String, RequestProcessor.Task> rps = new ConcurrentHashMap<>();
    private final URI baseURI;
    
    public HttpServer() throws IOException {
        Jooby joobyApp = Jooby.createApp(new String[] {}, ExecutionMode.DEFAULT, Jooby.class);
        joobyApp.setServerOptions(new ServerOptions()
            .setPort(0)
        );
        initAssets(joobyApp);
        initContexts(joobyApp);
        Server start = joobyApp.start();
        baseURI = URI.create("http://localhost:" + start.getOptions().getPort());
    }
    private void initAssets(Jooby joobyApp)
    {
        // Theme static file
        AssetSource asciidocThemes = AssetSource.create(getClass().getClassLoader(), "/com/github/philippefichet/asciidoc4netbeans/themes/");
        joobyApp.assets(
            "/themes/*",
            new AssetHandler(asciidocThemes)
            .setETag(true)
            .setLastModified(true)
        );
    }

    @SuppressWarnings({
        "java:S1075" // Remove this hard-coded path-delimiter. Disabled because is required to work
    })
    private void initContexts(Jooby joobyApp)
    {
        joobyApp.get("/*/*", ctx -> {
            String path = ctx.getRequestPath();
            for (Map.Entry<String, HttpServerContextConfiguration> entry : contextConfigurations.entrySet()) {
                String contextName = "/" + entry.getKey();
                if (path.startsWith(contextName)) {
                    String pathWithoutContext = path.substring(contextName.length());
                    HttpServerContextConfiguration contextConfiguration = entry.getValue();
                    Optional<String> contentByPath = contextConfiguration.getContentByPath(pathWithoutContext);
                    if (contentByPath.isPresent()) {
                        ctx.setResponseType(MediaType.HTML);
                        return ctx.send(contentByPath.get());
                    } else if(
                        pathWithoutContext.endsWith(AsciidocUtils.ADOC_EXTENSION) &&
                        Paths.get(contextConfiguration.getSourcePath().toString(), pathWithoutContext).toFile().exists()
                        || pathWithoutContext.endsWith(AsciidocUtils.HTML_EXTENSION) &&
                        Paths.get(contextConfiguration.getSourcePath().toString(), pathWithoutContext.replace(AsciidocUtils.HTML_EXTENSION, AsciidocUtils.ADOC_EXTENSION)).toFile().exists()
                    ) {
                        Path resolve = Paths.get(
                            contextConfiguration.getSourcePath().toString(),
                                pathWithoutContext.endsWith(AsciidocUtils.HTML_EXTENSION)
                                ? pathWithoutContext.replace(AsciidocUtils.HTML_EXTENSION, AsciidocUtils.ADOC_EXTENSION)
                                : pathWithoutContext
                            );
                        String absolutePath = resolve.toFile().getAbsolutePath();
                        RequestProcessor.Task get = rps.get(absolutePath);
                        if (get == null) {
                            AsciidocEngine asciidocEngine = Lookup.getDefault().lookup(AsciidocEngine.class);
                            rps.put(absolutePath, RP.post(() -> {
                                try {
                                    contextConfiguration.updateContentByPath(
                                        pathWithoutContext,
                                        asciidocEngine.convert(
                                            Files.readString(resolve),
                                            contextConfiguration.getSourcePath().toFile(),
                                            AsciidocEngineUtils.findBestOutputDirectory(resolve.toFile()),
                                            AsciidocUtils.getCurrentTheme(),
                                            AsciidocUtils.isDarkLaF()
                                        )
                                    );
                                } catch (Exception ex) {
                                    Exceptions.printStackTrace(ex);
                                } finally {
                                    rps.remove(absolutePath);
                                }
                            }));
                        }
                        // Loading page
                        ctx.setResponseType(MediaType.HTML);
                        return ctx.send(AsciidocUtils.readLoadingPage());
                    } else if (contextConfiguration.getResourcePaths() != null) {
                        for (Path resourcePath : contextConfiguration.getResourcePaths()) {
                            try {
                                Path resolve = resourcePath.resolve(path.substring(contextName.length()));
                                File toFile = resolve.toFile();
                                if (toFile.isFile() && toFile.exists()) {
                                    // dynamic AssetHandler for caching static files
                                    AssetHandler assetHandler = new AssetHandler(AssetSource.create(resolve));
                                    return assetHandler.apply(ctx);
                                }
                            } catch(InvalidPathException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
            return ctx;
        });
    }

    public URI getBaseURI()
    {
        return baseURI;
    }

    public void updateContext(
        Path sourcePath,
        List<Path> resourcePaths,
        String path,
        String content
    )
    {
        // Simple overrive change configuration
        String contextName = toContext(sourcePath);
        HttpServerContextConfiguration config = contextConfigurations.get(contextName);
        if (config == null) {
            config = new HttpServerContextConfiguration(resourcePaths, sourcePath);
            contextConfigurations.put(toContext(sourcePath), config);
        }
        config.updateContentByPath(path, content);
    }

    public void removeContext(Path sourcePath)
    {
        contextConfigurations.remove(toContext(sourcePath));
    }

    @SuppressWarnings({
        "java:S4790" // Using weak hashing algorithms is security-sensitive, disabled because is not a sensitive context
    })
    public static String toContext(Path sourcePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourcePath.toFile().getAbsolutePath().getBytes());
            return Hex.encodeHexString(md.digest());
        } catch (NoSuchAlgorithmException ex) {
            Exceptions.printStackTrace(ex);
            return "nocontext";
        }
    }
}
