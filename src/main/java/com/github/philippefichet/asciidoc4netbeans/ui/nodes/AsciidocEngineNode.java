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
package com.github.philippefichet.asciidoc4netbeans.ui.nodes;

import com.github.philippefichet.asciidoc4netbeans.AsciidocEngine;
import com.github.philippefichet.asciidoc4netbeans.AsciidocEngineStatus;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class AsciidocEngineNode extends AbstractNode {

    private static final String ICON_STOP = "com/github/philippefichet/asciidoc4netbeans/resources/asciidoc_icon_stop.png"; // NOI18N
    private static final String ICON_INIT = "com/github/philippefichet/asciidoc4netbeans/resources/asciidoc_icon_loading.png"; // NOI18N
    private static final String ICON_START = "com/github/philippefichet/asciidoc4netbeans/resources/asciidoc_icon_start.png"; // NOI18N

    private static AsciidocEngineNode node;
    
    public AsciidocEngineNode() {
        super(Children.LEAF);
        setName(NbBundle.getMessage(AsciidocEngineNode.class, "Asciidoc_Root_Node_Name"));
        setDisplayName(getName());
        setShortDescription(NbBundle.getMessage(AsciidocEngineNode.class, "Asciidoc_Root_Node_Short_Description"));
        setIconBaseWithExtension(ICON_STOP);
        Lookup.getDefault().lookup(AsciidocEngine.class)
        .addEphemeralListener(this::refresh);

    }

    public void refresh(AsciidocEngineStatus status) {
        if (status == AsciidocEngineStatus.NOT_STARTED) {
            setIconBaseWithExtension(ICON_STOP);
        }
        if (status == AsciidocEngineStatus.START_INITIALIZED) {
            setIconBaseWithExtension(ICON_INIT);
        }
        if (status == AsciidocEngineStatus.STARTED) {
            setIconBaseWithExtension(ICON_START);
        }
    }
}
