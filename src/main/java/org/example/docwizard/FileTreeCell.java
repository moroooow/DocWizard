package org.example.docwizard;

import javafx.scene.control.TreeCell;

import java.io.File;

public class FileTreeCell extends TreeCell<File> {
    @Override
    protected void updateItem(File file, boolean b) {
        super.updateItem(file, b);

        if(b || file == null){
            setText(null);
        } else {
            setText(file.getName());
        }
    }
}
