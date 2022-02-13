package com.geekbrains.cloud.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class MainController implements Initializable {



    public TextField clientPath;
    public TextField serverPath;
    public ListView<String> clientView;
    public ListView<String> serverView;
    private File currentDirectory;

    private static ClientConnector client;

    // Platform.runLater(() -> {})

    private void updateClientView() {
        Platform.runLater(() -> {
            if (currentDirectory != null){
            clientPath.setText(currentDirectory.getAbsolutePath());
            clientView.getItems().clear();
            clientView.getItems().add("...");
            if (currentDirectory.list()!=null) {
                clientView.getItems()
                        .addAll(currentDirectory.list());
            }
        }
        });
    }

    public void updateServerView (String[] s){
        Platform.runLater(() -> {
            String sb = "";
            String command = null;
                serverPath.setText(s[0]);
                serverView.getItems().clear();
                serverView.getItems().add("...");
                for (int i = 1; i<s.length;i++){
                    serverView.getItems()
                            .add(s[i]);
                }
        });

    }

    public void download(ActionEvent actionEvent) {
        String item = serverView.getSelectionModel().getSelectedItem();
        if (item.contains(".")) {
            client.getSelectedFile(item,currentDirectory);
        }
        updateClientView();
    }

    // upload file to server
    public void upload(ActionEvent actionEvent) throws IOException {
        String item = clientView.getSelectionModel().getSelectedItem();
        File selected = currentDirectory.toPath().resolve(item).toFile();
        if (selected.isFile()) {
            client.sendSelectedFile(selected);
        }
    }

    public static void exitButtonAction() {
        client.sendClose();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDirectory = new File(System.getProperty("user.home"));
        this.client = new ClientConnector(this);
        // run in FX Thread
        // :: - method reference
        updateClientView();
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = clientView.getSelectionModel().getSelectedItem();
                if (item.equals("...")) {
                    currentDirectory = currentDirectory.getParentFile();
                    updateClientView();
                } else {
                    File selected = currentDirectory.toPath().resolve(item).toFile();
                    if (selected.isDirectory()) {
                        currentDirectory = selected;
                        updateClientView();
                    }
                }
            }
        });
    }
}
