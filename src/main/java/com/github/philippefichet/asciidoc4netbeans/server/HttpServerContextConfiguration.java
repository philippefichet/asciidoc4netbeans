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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class HttpServerContextConfiguration {
    private final List<Path> resourcePaths;
    private final Map<String, String> contentByPath = new HashMap<>();
    private final Path sourcePath;

    public HttpServerContextConfiguration(
        List<Path> resourcePaths,
        Path sourcePath
    ) {
        this.resourcePaths = resourcePaths;
        this.sourcePath = sourcePath;
    }

    public List<Path> getResourcePaths() {
        return resourcePaths;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public Optional<String> getContentByPath(String path) {
        return Optional.ofNullable(contentByPath.get(path));
    }

    public void updateContentByPath(String path, String content)
    {
        contentByPath.put(path, content);
    }
}
