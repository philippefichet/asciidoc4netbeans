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
import java.util.List;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class HttpServerContextConfiguration {
    private final List<Path> resourcePaths;
    private final String content;
    private final String contentPath;

    public HttpServerContextConfiguration(List<Path> resourcePaths, String content, String contentPath) {
        this.resourcePaths = resourcePaths;
        this.content = content;
        this.contentPath = contentPath;
    }

    public List<Path> getResourcePaths() {
        return resourcePaths;
    }

    public String getContent() {
        return content;
    }

    public String getContentPath() {
        return contentPath;
    }
}
