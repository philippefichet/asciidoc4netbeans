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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class AsciidocPreviewPanel extends javax.swing.JPanel {

    private static final Logger LOG = Logger.getLogger(AsciidocPreviewPanel.class.getName());

    /**
     * Reference to WebView create later
     */
    private final AtomicReference<WebView> webViewReference = new AtomicReference<>();
    private final JFXPanel jfxPanelForBrowser = new JFXPanel();
    private URI currentURI;
    private ChangeListener<Document> currentDOMListener;

    /**
     * handler for click on XRef/hyperlink
     */
    private final Consumer<File> xrefHandler;

    static {
        // To avoid closing the JavaFX application when the JFXPanel is closed
        Platform.setImplicitExit(false);
    }

    /**
     * Creates new form AsciidocPreviewPanel
     * @param xrefHandler handler for click on XRef/hyperlink
     */
    public AsciidocPreviewPanel(Consumer<File> xrefHandler) {
        initComponents();
        this.xrefHandler = xrefHandler;
        Platform.runLater(() -> {
            BorderPane borderPane = new BorderPane();
            Scene scene  =  new  Scene(borderPane);
            webViewReference.set(new WebView());
            webViewReference.get().autosize();
            borderPane.setCenter(webViewReference.get());
            jfxPanelForBrowser.setScene(scene);
            if (AsciidocUtils.isDarkLaF()) {
                scene.getStylesheets().add(getClass().getResource("/com/github/philippefichet/asciidoc4netbeans/javafx-nb-dark.css").toString());
                // Loading default content to force apply a content with css for dark background
                webViewReference.get().getEngine().loadContent(AsciidocUtils.readLoadingPage());
            }
        });
        add(jfxPanelForBrowser, BorderLayout.CENTER);
    }

    private void externalLinkHandle(URI baseURI, URI uri, File baseDirFolder) {
        if (currentDOMListener != null) {
            webViewReference.get().getEngine()
            .documentProperty()
            .removeListener(currentDOMListener);
        }
        
        AsciidocLinkEventListener eventListener = new AsciidocLinkEventListener(
            baseURI,
            uri,
            baseDirFolder,
            xrefHandler
        );
        currentDOMListener =
            (ObservableValue<? extends Document> observable, Document oldValue, Document newValue) -> {
                if (newValue != null) {
                    NodeList elementsByTagName = newValue.getElementsByTagName("a");
                    for (int i = 0; i < elementsByTagName.getLength(); i++) {
                        Node item = elementsByTagName.item(i);
                        if (item instanceof EventTarget) {
                            ((EventTarget) item).addEventListener("click", eventListener, false);
                        }
                    }
                }
            };
        webViewReference.get().getEngine()
        .documentProperty()
        .addListener(currentDOMListener);
    }

    public void loading() {
        loadginProgressBar.setIndeterminate(true);
        loadginProgressBar.setValue(0);
        loadginProgressBar.setString("Loading ...");
        loadginProgressBar.setVisible(true);
    }

    public void loaded() {
        loadginProgressBar.setIndeterminate(false);
        loadginProgressBar.setValue(100);
        loadginProgressBar.setString("Loaded");
        loadginProgressBar.setVisible(false);
    }

    public void load(URI baseURI, URI uri, File baseDirFolder) {
        currentURI = uri;
        externalLinkHandle(baseURI, uri, baseDirFolder);
        Platform.runLater(() -> {
            uriTextField.setText(uri.toString());
            webViewReference.get().getEngine().load(uri.toString());
            webViewReference.get().getEngine().reload();
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({
        "unchecked",
        "java:S1604" // "Anonymous inner classes containing only one method should become lambdas" disabled because managed by netbeans
    })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel headPanel = new javax.swing.JPanel();
        javax.swing.JPanel loadingPanel = new javax.swing.JPanel();
        javax.swing.JButton openInExternalBrowserButton = new javax.swing.JButton();
        loadginProgressBar = new javax.swing.JProgressBar();
        uriTextField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        openInExternalBrowserButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/github/philippefichet/asciidoc4netbeans/resources/open_in_external_browser.png"))); // NOI18N
        openInExternalBrowserButton.setToolTipText(org.openide.util.NbBundle.getMessage(AsciidocPreviewPanel.class, "AsciidocPreviewPanel.openInExternalBrowserButton.toolTipText")); // NOI18N
        openInExternalBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openInExternalBrowserButtonActionPerformed(evt);
            }
        });

        loadginProgressBar.setIndeterminate(true);
        loadginProgressBar.setString(org.openide.util.NbBundle.getMessage(AsciidocPreviewPanel.class, "AsciidocPreviewPanel.loadginProgressBar.string")); // NOI18N
        loadginProgressBar.setStringPainted(true);

        uriTextField.setEditable(false);
        uriTextField.setText(org.openide.util.NbBundle.getMessage(AsciidocPreviewPanel.class, "AsciidocPreviewPanel.uriTextField.text")); // NOI18N

        javax.swing.GroupLayout loadingPanelLayout = new javax.swing.GroupLayout(loadingPanel);
        loadingPanel.setLayout(loadingPanelLayout);
        loadingPanelLayout.setHorizontalGroup(
            loadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loadingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(openInExternalBrowserButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(uriTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadginProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .addContainerGap())
        );
        loadingPanelLayout.setVerticalGroup(
            loadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loadingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(loadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loadginProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(loadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(uriTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(openInExternalBrowserButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout headPanelLayout = new javax.swing.GroupLayout(headPanel);
        headPanel.setLayout(headPanelLayout);
        headPanelLayout.setHorizontalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headPanelLayout.createSequentialGroup()
                .addComponent(loadingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        headPanelLayout.setVerticalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loadingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        add(headPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings({
        "java:S1172" // "Unused method parameters should be removed" disabled because managed by netbeans
    })
    private void openInExternalBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openInExternalBrowserButtonActionPerformed
        if (currentURI == null) {
            return;
        }
        try {
            Desktop.getDesktop().browse(currentURI);
        } catch (IOException ex) {
            LOG.severe("Cannot open browser to open URI \"" + currentURI + "\"");
        }
    }//GEN-LAST:event_openInExternalBrowserButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar loadginProgressBar;
    private javax.swing.JTextField uriTextField;
    // End of variables declaration//GEN-END:variables
}
