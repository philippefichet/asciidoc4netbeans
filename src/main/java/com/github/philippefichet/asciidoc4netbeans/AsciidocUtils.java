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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class AsciidocUtils {

    private static final Logger LOG = Logger.getLogger(AsciidocUtils.class.getName());

    private AsciidocUtils() {
    }

    /**
     * Copied from org.openide.util.ImageUtilities#isDarkLaF()
     * @return true if current LAF is a dark LAF, false otherwise
     */
    public static boolean isDarkLaF() {
        return UIManager.getBoolean("nb.dark.theme"); //NOI18N 
    }

    /**
     * Load default loading HTML page for web browser
     * @return default loading HTML page for web browser
     */
    public static String readLoadingPage() {
        try (
            InputStream loading = AsciidocUtils.class.getResourceAsStream("/com/github/philippefichet/asciidoc4netbeans/loading.html");
            BufferedInputStream bufferedInputStream = new BufferedInputStream(loading);
        ) {
            return new String(bufferedInputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch(IOException ioe) {
            LOG.warning("Error while read default loading HTML page");
            return "Loading ...";
        }
    }
}
