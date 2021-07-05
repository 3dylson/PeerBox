package pt.ipb.dsys.peerbox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    public ListView<String> hostFiles;
    public ListView<String> clusterFiles;
    public Spinner<Integer> replicas;
    public Button fetchBttn;

    private PeerFile peerFile;

    public void setPeerFile(PeerFile peerFile) {
        this.peerFile = peerFile;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> files = FXCollections.observableArrayList();
        try {
            File[] listFiles = peerFile.readDirectory();
            Arrays.stream(listFiles).iterator().forEachRemaining(file -> files.add(file.getName()));
            hostFiles.setItems(files);

        } catch (PeerBoxException e) {
            e.printStackTrace();
        }

        SpinnerValueFactory<Integer> valueFactory = replicas.getValueFactory();
        replicas.setOnScroll(event -> {
            if( replicas.isDisabled()) {
                return;
            }
            if (!replicas.isVisible()) {
                return;
            }
            double delta = event.getDeltaY();
            if (delta == 0.0) {
                return;
            }
            int increment = delta < 0 ? -1 : +1;
            valueFactory.setValue(replicas.getValue() + increment);
        });
        replicas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,20,2));
        //BidirectionalBinding.bindNumber(,valueFactory.valueProperty());
        //replicas.setEditable(true);

        hostFiles.setOnDragOver(event -> {
            if (event.getGestureSource() != hostFiles
                    && event.getDragboard().hasFiles()) {
                /* allow for copying. */
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        hostFiles.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                //TODO Copy multiple files at once
                File file = db.getFiles().get(0);
                Path path = Paths.get(db.getFiles().get(0).toString());
                try {
                    peerFile.setData(Files.readAllBytes(path));
                    peerFile.save(file.getName(),replicas.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    files.add(file.getName());
                    success = true;
                }
            }
            event.setDropCompleted(success);
        });

    }



}


