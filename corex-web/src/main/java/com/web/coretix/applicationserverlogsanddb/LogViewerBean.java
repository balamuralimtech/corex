/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
package com.web.coretix.applicationserverlogsanddb;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.DefaultStreamedContent;

import javax.inject.Named;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

@Named("logViewerBean")
@Scope("session")
public  class LogViewerBean implements Serializable {


    private static final Logger logger = LoggerFactory.getLogger(LogViewerBean.class);
    
    private TreeNode root;
    private TreeNode selectedNode;
    private String selectedFileContent;
    private String selectedFileName;
    
    private StreamedContent fileToDownload;
    private StreamedContent zipToDownload;
    
    private final String serverLocation = System.getProperty("catalina.base");
    private final String fileSeparator = System.getProperty("file.separator");
    private String serverLogsLocation;

    // Constructor initializes the tree structure by loading files and folders
    public LogViewerBean() {
        
    }
    
    

    // Getters and Setters
    /**
     * @return the root
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public String getSelectedFileContent() {
        return selectedFileContent;
    }

    public String getSelectedFileName() {
        return selectedFileName;
    }
    
    public boolean accept(String name) {
                return name.endsWith(".log");
            }

    // Recursively create the tree structure by adding files and folders
    private void createTree(File file, TreeNode parent) {
        if (file.isDirectory()) {
            // Create a folder node and add it to the parent node
            TreeNode folderNode = new DefaultTreeNode("folder", file.getName(), parent);
            File[] childFiles = file.listFiles();  // List all files and folders inside this directory
            if (childFiles != null) {
                for (File child : childFiles) {
                    createTree(child, folderNode);  // Recursively add children to the folder node
                }
            }
        } else {
            // Create a file node and add it to the parent node
            if (accept(file.getName())) {
                new DefaultTreeNode("file", file.getName(), parent);
            }
        }
    }
    
    public void refreshButtonAction()
    {
        selectedFileName = "";
        selectedFileContent = "";
        root = null;
        selectedNode = null;
                
        root = new DefaultTreeNode("Root", null);  // Root of the tree
        File logDir = new File(serverLogsLocation);
        if (logDir.exists() && logDir.isDirectory()) {
            createTree(logDir, root);  // Load files and folders recursively
        }
    }
    
    public void selectionChanged()
    {
        logger.debug("inside the selectionChanged method !!"+selectedNode);
        fileToDownload = null;
        
         if (selectedNode != null && "file".equals(selectedNode.getType())) {
            StringBuilder filePathBuilder = new StringBuilder();
            TreeNode currentNode = selectedNode;

            // Reconstruct the file path by navigating up through the tree nodes
            while (currentNode != null && currentNode.getParent() != null && currentNode.getData() != null) {
                filePathBuilder.insert(0, currentNode.getData() + fileSeparator);
                currentNode = currentNode.getParent();
            }

            logger.debug("filePathBuilder.toString() : " + filePathBuilder.toString());

            String fullFilePath = serverLocation +fileSeparator +filePathBuilder.toString();
            File logFile = new File(fullFilePath);

            selectedFileName = logFile.getName();  // Store the file name
            logger.debug("selectedFileName : " + selectedFileName);
            
            logger.debug("logFile : " + logFile.getPath());
            
              // Initialize the content to display
            StringBuilder contentBuilder = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                logger.debug("Error reading log file: " + e);
            }

            try {

                InputStream is = new FileInputStream(logFile.getPath());

                fileToDownload = DefaultStreamedContent.builder()
                        .name(selectedFileName)
                        .contentType("text/plain")
                        .stream(() -> is)
                        .build();
            } catch (FileNotFoundException ex) {
                logger.debug("FileNotFoundException : " + ex);
            }
        }


    }

    // Load the content of the selected log file into the dialog
    public void loadSelectedLogFile() {
        logger.debug("inside the loadSelectedLogFile method !!"+selectedNode);
        
        if (selectedNode != null && "file".equals(selectedNode.getType())) {
            StringBuilder filePathBuilder = new StringBuilder();
            TreeNode currentNode = selectedNode;

            // Reconstruct the file path by navigating up through the tree nodes
            while (currentNode != null && currentNode.getParent() != null && currentNode.getData() != null) {
                filePathBuilder.insert(0, currentNode.getData() + fileSeparator);
                currentNode = currentNode.getParent();
            }

            logger.debug("filePathBuilder.toString() : " + filePathBuilder.toString());

            String fullFilePath = serverLocation +fileSeparator +filePathBuilder.toString();
            File logFile = new File(fullFilePath);

            logger.debug("logFile : " + logFile.getPath());

            // Initialize the content to display
            StringBuilder contentBuilder = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                contentBuilder.append("Error reading log file: ").append(e.getMessage());
            }

            // Store the file content in selectedFileContent
            selectedFileContent = contentBuilder.toString();
            logger.debug("selectedFileContent : " + selectedFileContent);
            selectedFileName = logFile.getName();  // Store the file name
            logger.debug("selectedFileName : " + selectedFileName);
        }

    }

// Download the selected log file
    public StreamedContent downloadSelectedLogFile() {
        logger.debug("inside the downloadSelectedLogFile method !!");
        if (selectedNode != null && "file".equals(selectedNode.getType())) {
            selectedFileName = selectedNode.getData().toString();
            TreeNode currentNode = selectedNode;
            StringBuilder filePathBuilder = new StringBuilder(selectedFileName);

            // Reconstruct the file path by navigating up through the tree nodes
            while (currentNode.getParent() != null && currentNode.getParent().getData() != null) {
                currentNode = currentNode.getParent();
                filePathBuilder.insert(0, currentNode.getData() + "/");
            }

            String fullFilePath = serverLocation + fileSeparator + filePathBuilder.toString();
            File file = new File(fullFilePath);
            logger.debug("logFile : " + file.getPath());

            try {
                InputStream stream = new FileInputStream(file);
                return DefaultStreamedContent.builder()
                        .name(selectedFileName)
                        .contentType("text/plain")
                        .stream(() -> stream)
                        .build();
            } catch (FileNotFoundException ex) {
                logger.debug("Exception while downloading the" + selectedFileName + " File : " + ex);
            }
        }
        return null;
    }

    // Download all log files (recursively) as a ZIP
    public StreamedContent downloadAllLogFilesAsZip()  {
        logger.debug("inside the downloadAllLogFilesAsZip method !!");
        try {
            File logDir = new File(serverLogsLocation);
            logger.debug("logFile : " + logDir.getPath());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream);

            zipFilesRecursively(logDir, zipOut, "");

            zipOut.close();
            InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return DefaultStreamedContent.builder()
                    .name("logs.zip")
                    .contentType("application/zip")
                    .stream(() -> inputStream)
                    .build();
        } catch (IOException ex) {
            logger.debug("Exception while downloading the logs.zip  : " + ex);
        }
        logger.debug("end of downloadAllLogFilesAsZip method !!");
        return null;
    }

    // Helper method to zip files and folders recursively
    private void zipFilesRecursively(File fileToZip, ZipOutputStream zipOut, String parentDir) throws IOException {
        logger.debug("inside the zipFilesRecursively method !!");
        if (fileToZip.isDirectory()) {
            String folderName = parentDir + fileToZip.getName() + fileSeparator;
            zipOut.putNextEntry(new ZipEntry(folderName));
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFilesRecursively(childFile, zipOut, folderName);
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                ZipEntry zipEntry = new ZipEntry(parentDir + fileToZip.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
            }
        }
    }
    
    public void initializePageAttributes()
    {
        logger.debug("inside initializePageAttributes !!!!");
        serverLogsLocation  = serverLocation + fileSeparator+ "logs";
        
        try {

            InputStream is = new FileInputStream(serverLogsLocation);

            zipToDownload = DefaultStreamedContent.builder()
                    .name("serverlogs.zip")
                    .contentType("application/zip")
                    .stream(() -> is)
                    .build();
        } catch (FileNotFoundException ex) {
            logger.debug("FileNotFoundException : " + ex);
        }
        
        
        logger.debug("serverLocation : "+serverLocation);
        logger.debug("fileSeparator : "+fileSeparator);
        logger.debug("serverLogsLocation : "+serverLogsLocation);
        refreshButtonAction();
    }

    /**
     * @return the fileToDownload
     */
    public StreamedContent getFileToDownload() {
        return fileToDownload;
    }

    /**
     * @param fileToDownload the fileToDownload to set
     */
    public void setFileToDownload(StreamedContent fileToDownload) {
        this.fileToDownload = fileToDownload;
    }

    /**
     * @return the zipToDownload
     */
    public StreamedContent getZipToDownload() {
        return zipToDownload;
    }

    /**
     * @param zipToDownload the zipToDownload to set
     */
    public void setZipToDownload(StreamedContent zipToDownload) {
        this.zipToDownload = zipToDownload;
    }
  
}




