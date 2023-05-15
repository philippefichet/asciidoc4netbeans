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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class AsciidocEngineTest {
    
    public AsciidocEngineTest() {
    }

    @Test
    public void convert() throws IOException {
        AsciidocEngine asciidocEngine = new AsciidocEngine(getClass().getClassLoader());
        asciidocEngine.startAsciidocEngine();
        asciidocEngine.waitingInitialization();
        File outputDir = new File("./target/asciidoc4netbeans");
        outputDir.mkdirs();
        String html = asciidocEngine.convert(
            new String(Files.readAllBytes(Paths.get("./src/test/resouces/plantuml.adoc")), StandardCharsets.UTF_8),
            new File("."),
            outputDir,
            "classic",
            false
        );
        Document parse = Jsoup.parse(html);
        Elements umlElement = parse.select("img[src=\"testing-1-plantuml.png\"]");
        Assertions.assertThat( umlElement)
            .withFailMessage("PlantUML detection failed.")
            .hasSize(1);
    }
}
