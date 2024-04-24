package org.example.docwizard;

import java.io.File;

public class DirectoryListing {
    private File[] files;

    public DirectoryListing(String path){
        File directory = new File(path);

        files = directory.listFiles();
    }
    public void  setFiles(String path){
        files = new File(path).listFiles();
    }
    public File[] getFiles() {
        return files;
    }
}
