package org.example.docwizard;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileScanner {
    private final TreeItem<File> root;
    private static final Lock lock = new ReentrantLock();

    public FileScanner(TreeItem<File> root) {
        this.root = root;
    }

    public void removeItem(TreeItem<File> selectedItem) {
        root.getChildren().remove(selectedItem);
    }

    public TreeItem<File> getRoot() {
        return root;
    }

    public List<File> getDocxAndXlsxFiles() {
        List<File> res = new ArrayList<>();
        getDocxAndXlsxFiles(root, res);
        return res;
    }

    public boolean isEmpty() {
        return root.getChildren().isEmpty();
    }

    private void getDocxAndXlsxFiles(TreeItem<File> root, List<File> res) {
        for (TreeItem<File> file : root.getChildren()) {
            if (file.getValue().isDirectory()) {
                getDocxAndXlsxFiles(file, res);
                continue;
            }

            if (file.getValue().getName().endsWith(".docx") || file.getValue().getName().endsWith(".xlsx")) {
                res.add(file.getValue());
            }
        }
    }

    private void scanFiles(TreeItem<File> directory, List<String> collection, ObservableAtomicInteger processFiles, CountDownLatch latch) {
        for (TreeItem<File> children : directory.getChildren()) {
            processFiles.incrementAndGet();
            if (children.getValue().isDirectory()) {
                scanFiles(children, collection, processFiles, latch);
                latch.countDown();
                continue;
            }
            if (children.getValue().getAbsolutePath().endsWith(".docx")) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        try (FileInputStream fis = new FileInputStream(children.getValue().getAbsolutePath())) {
                            XWPFDocument doc = new XWPFDocument(fis);
                            scanDocxFile(doc, (ArrayList<String>) collection);
                        } catch (IOException ignored) {
                        }
                        finally {
                            latch.countDown();
                        }
                        return null;
                    }
                };

                new Thread(task).start();
            } else if (children.getValue().getAbsolutePath().endsWith(".xlsx")) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        try (FileInputStream fis = new FileInputStream(children.getValue().getAbsolutePath())) {
                            XSSFWorkbook xlsx = new XSSFWorkbook(fis);
                            scanXlsxFile(xlsx, (ArrayList<String>) collection);
                        } catch (IOException ignored) {}
                        finally {
                        latch.countDown();
                        }
                        return null;
                    }
                };
                new Thread(task).start();
            }
        }
    }

    private int getTotalFileCount(TreeItem<File> treeItem) {
        int count = 1;
        for (TreeItem<File> child : treeItem.getChildren()) {
            if (child.getValue().isDirectory()) {
                count += getTotalFileCount(child);
            } else {
                count++;
            }
        }
        return count;
    }

    public List<String> handleScan() throws ExecutionException, InterruptedException {
        if (root == null) {
            return null;
        }

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);
        Stage stage = new Stage();
        VBox vbox = new VBox();
        vbox.getChildren().add(progressBar);
        Scene scene = new Scene(vbox, 200, 100);
        stage.setScene(scene);
        stage.setTitle("File Scanner");
        stage.toFront();
        stage.show();

        ObservableAtomicInteger processedFiles = new ObservableAtomicInteger(0);

        Task<List<String>> task = getListTask(stage, processedFiles);

        progressBar.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.start();


        return task.get();
    }

    private Task<List<String>> getListTask(Stage stage, ObservableAtomicInteger processedFiles) {
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() {
                ArrayList<String> res = new ArrayList<>() {
                    @Override
                    public boolean add(String s) {
                        if (!contains(s)) {
                            return super.add(s);
                        }
                        return false;
                    }
                };

                int totalFileCount = getTotalFileCount(root);
                CountDownLatch latch = new CountDownLatch(getTotalFileCount(root) - 10);

                processedFiles.addChangeListener((oldValue, newValue) -> {
                    updateProgress(newValue, totalFileCount);
                });

                scanFiles(root, res, processedFiles,latch);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    return res;
                }
                return res;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(stage::close));
        return task;
    }

    private static void scanXlsxFile(XSSFWorkbook xlsx, ArrayList<String> res) {
        for (int i = 0; i < xlsx.getNumberOfSheets(); i++) {
            XSSFSheet sheet = xlsx.getSheetAt(i);

            Iterator<Row> rowIter = sheet.rowIterator();

            while (rowIter.hasNext()) {
                Row row = rowIter.next();
                Iterator<Cell> cellIter = row.cellIterator();

                while (cellIter.hasNext()) {
                    Cell cell = cellIter.next();
                    if (cell.getCellType() == CellType.STRING) {
                        String str = cell.getStringCellValue();
                        findingMatches(str, res);
                    }
                }
            }
        }
    }

    private static void scanDocxFile(XWPFDocument doc, ArrayList<String> res) {
        List<XWPFParagraph> paragraphs = doc.getParagraphs();
        for (XWPFParagraph par : paragraphs) {
            String str = par.getText();
            findingMatches(str, res);
        }
    }

    private static void findingMatches(String str, ArrayList<String> res) {
        Pattern p = Pattern.compile("##+[^:,.\\s\\t\\n]+");
        Matcher m = p.matcher(str);
        lock.lock();
        try {
            while (m.find()) {

                res.add(m.group());
            }
        } finally {
            lock.unlock();
        }
    }

}