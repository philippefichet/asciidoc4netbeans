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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.openide.util.Exceptions;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class HttpServer {

    private final Map<String, HttpServerContextConfiguration> contextConfigurations = new ConcurrentHashMap<>();
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
    private void initContexts(Jooby joobyApp)
    {
        joobyApp.get("/*/*", ctx -> {
            String path = ctx.getRequestPath();
            for (Map.Entry<String, HttpServerContextConfiguration> entry : contextConfigurations.entrySet()) {
                String contextName = entry.getKey();
                if (ctx.getRequestPath().startsWith(contextName)) {
                    HttpServerContextConfiguration contextConfiguration = entry.getValue();
                    if (path.equals(contextName + contextConfiguration.getContentPath())) {
                        ctx.setResponseType(MediaType.HTML);
                        return ctx.send(contextConfiguration.getContent());
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

    public void addContext(String contextName, HttpServerContextConfiguration contextConfiguration)
    {
        // Simple overrive change configuration
        contextConfigurations.put(contextName, contextConfiguration);
    }

    public void removeContext(String contextName)
    {
        contextConfigurations.remove(contextName);
    }
}
