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
import com.github.philippefichet.asciidoc4netbeans.server.HttpServer;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * Multiview preview for asciidoc file
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@NbBundle.Messages("Preview=&Preview")
@MultiViewElement.Registration(
        displayName = "#Preview",
        iconBase = AsciidocLanguageConfig.ICON_PATH,
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        mimeType = AsciidocLanguageConfig.MIME_TYPE,
        preferredID = "asciidoc.preview",
        position = 100)
public class AsciidocPreviewMVElement implements MultiViewElement {

    private static final Logger LOG = Logger.getLogger(AsciidocPreviewMVElement.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(AsciidocPreviewMVElement.class);
    private final RequestProcessor.Task updateTask;
    private final Object lock = new Object();
    private final JToolBar toolbar = new JToolBar();
    private final Lookup context;
    private final DocumentListener sourceDocListener = new DocumentHandler();
    private final AsciidocPreviewPanel asciidocPreviewPanel;
    private final AsciidocDataObject asciidocDataObject;
    private final FileObject primaryFile;
    private final File primaryFileFile;
    private final HttpServer httpServer;
    private final List<Path> httpServerContextPaths = new ArrayList<>();
    private final File baseDirFolder;
    private final File outputDirFolder;
    private StyledDocument sourceDoc = null;
    private MultiViewElementCallback callback = null;
    

    public AsciidocPreviewMVElement(Lookup context)
    {
        this.context = context;
        asciidocDataObject = context.lookup(AsciidocDataObject.class);
        httpServer = Lookup.getDefault().lookup(HttpServer.class);
        primaryFile = asciidocDataObject.getPrimaryFile();
        primaryFileFile = FileUtil.toFile(primaryFile);
        baseDirFolder = AsciidocEngineUtils.findBestBaseDirectory(primaryFileFile);
        outputDirFolder = AsciidocEngineUtils.findBestOutputDirectory(primaryFileFile);
        httpServerContextPaths.add(baseDirFolder.toPath());
        httpServerContextPaths.add(outputDirFolder.toPath());
        this.updateTask = RP.create(this::doUpdatePreview);
        toolbar.setFloatable(false);
        toolbar.addSeparator();
        asciidocPreviewPanel = new AsciidocPreviewPanel(this::openFile);
    }

    private void openFile(File xref)
    {
        Object result = DialogDisplayer.getDefault()
            .notify(
                new DialogDescriptor(
                    "Do you want to open the \"" + xref.getName() + "\" file?",
                    "Open file",
                    true,
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.OK_OPTION,
                    (ActionEvent ae) -> {
                        // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                    })
            );

        if (result == NotifyDescriptor.OK_OPTION) {
            try {
                Lookup lookup = DataObject.find(FileUtil.toFileObject(xref)).getLookup();
                final EditorCookie ec = lookup.lookup(EditorCookie.class);
                if (ec == null) {
                    OpenCookie oc = lookup.lookup(OpenCookie.class);
                    oc.open();
                } else {
                    ec.open();
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public JComponent getVisualRepresentation() {
        return asciidocPreviewPanel;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return context;
    }

    @Override
    public void componentOpened() {
        // lancer le serveur
        LOG.info("componentOpened");
    }

    @Override
    public void componentClosed() {
        // couper le serveur
        LOG.info("componentClosed");
        httpServer.removeContext(baseDirFolder.toPath());
    }

    @Override
    public void componentShowing() {
        final EditorCookie ec = context.lookup(EditorCookie.class);
        if (ec != null) {
            RP.post(() -> {
                try {
                    final StyledDocument localSourceDoc = ec.openDocument();
                    setSourceDocument(localSourceDoc);
                    doUpdatePreview();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
    }

    @Override
    public void componentHidden() {
        LOG.info("componentHidden");
    }

    @Override
    public void componentActivated() {
        LOG.info("componentActivated");
    }

    @Override
    public void componentDeactivated() {
        LOG.info("componentDeactivated");
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    
    private StyledDocument getSourceDocument() {
        synchronized (lock) {
            return sourceDoc;
        }
    }

    private void setSourceDocument(final StyledDocument newSourceDoc) {
        synchronized (lock) {
            if (this.sourceDoc != null) {
                this.sourceDoc.removeDocumentListener(sourceDocListener);
            }
            this.sourceDoc = newSourceDoc;
            if (this.sourceDoc != null) {
                this.sourceDoc.addDocumentListener(sourceDocListener);
            }
        }
    }

    @SuppressWarnings({
        "java:S1075" // Remove this hard-coded path-delimiter. Disabled because is required to work
    })
    private void doUpdatePreview() {
        asciidocPreviewPanel.loading();
        final StyledDocument localSourceDoc = getSourceDocument();
        if (localSourceDoc != null) {
            final String previewText = renderPreview(localSourceDoc);
            String filePath = "/" + baseDirFolder.toPath().relativize(primaryFileFile.toPath()).toString();
            String filePathForURL = filePath.replace(File.separator, "/");
            httpServer.updateContext(
                baseDirFolder.toPath(),
                httpServerContextPaths,
                filePathForURL,
                previewText
            );
            httpServer.updateContext(
                baseDirFolder.toPath(),
                httpServerContextPaths,
                filePathForURL.replace(".adoc", ".html"),
                previewText
            );
            String contextName = HttpServer.toContext(baseDirFolder.toPath());
            URI baseURI = httpServer.getBaseURI().resolve("/" + contextName);
            asciidocPreviewPanel.load(
                baseURI,
                baseURI.resolve(contextName + filePathForURL),
                baseDirFolder
            );
            asciidocPreviewPanel.loaded();
        }
    }

    private void updatePreview() {
        updateTask.schedule(1000);
    }

    private class DocumentHandler implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updatePreview();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updatePreview();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updatePreview();
        }
    }

    private String renderPreview(StyledDocument localSourceDoc) {
        String content;
        try {
            content = localSourceDoc.getText(0, localSourceDoc.getLength());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            content = ex.getLocalizedMessage();
        }
        AsciidocEngine asciidocEngine = Lookup.getDefault().lookup(AsciidocEngine.class);
        return asciidocEngine.convert(content, baseDirFolder, outputDirFolder, AsciidocUtils.getCurrentTheme(), AsciidocUtils.isDarkLaF());
    }
}
