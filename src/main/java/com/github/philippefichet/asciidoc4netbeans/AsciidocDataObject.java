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

import com.github.philippefichet.asciidoc4netbeans.csl.AsciidocLanguageConfig;
import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.textmate.lexer.api.GrammarRegistration;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 * 
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@Messages({
    "LBL_Asciidoc_LOADER=Files of Asciidoc"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Asciidoc_LOADER",
        mimeType = AsciidocLanguageConfig.MIME_TYPE,
        extension = {"adoc"}
)
@DataObject.Registration(
        mimeType = AsciidocLanguageConfig.MIME_TYPE,
        iconBase = AsciidocLanguageConfig.ICON_PATH,
        displayName = "#LBL_Asciidoc_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/" + AsciidocLanguageConfig.MIME_TYPE + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/" + AsciidocLanguageConfig.MIME_TYPE + "/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/" + AsciidocLanguageConfig.MIME_TYPE + "/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/" + AsciidocLanguageConfig.MIME_TYPE + "/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/" + AsciidocLanguageConfig.MIME_TYPE + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/" + AsciidocLanguageConfig.MIME_TYPE + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/" + AsciidocLanguageConfig.MIME_TYPE + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/" + AsciidocLanguageConfig.MIME_TYPE + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/" + AsciidocLanguageConfig.MIME_TYPE + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
@GrammarRegistration(mimeType = AsciidocLanguageConfig.MIME_TYPE, grammar = "asciidoc.tmLanguage.json")
public class AsciidocDataObject extends MultiDataObject {
    private final Lookup lookup;
    private final InstanceContent lookupContent;

    public AsciidocDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor(AsciidocLanguageConfig.MIME_TYPE, true);

        lookupContent = new InstanceContent();

        lookup = new ProxyLookup(getCookieSet().getLookup(), new AbstractLookup(lookupContent));
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @NbBundle.Messages("Source=&Source")
    @MultiViewElement.Registration(
            displayName="#Source",
            iconBase=AsciidocLanguageConfig.ICON_PATH,
            persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
            mimeType=AsciidocLanguageConfig.MIME_TYPE,
            preferredID="asciidoc.source",
            position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
}
