package pt.ipb.dsys.peerbox;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static pt.ipb.dsys.peerbox.Main.peerBox;

@Component
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @FXML
    public ListView<String> hostFiles;
    @FXML
    public ListView<String> clusterFiles;
    @FXML
    public Spinner<Integer> replicas;
    @FXML
    public Button fetchBttn;
    @FXML
    public Button deleteBttn;
    @FXML
    public Button deleteOneReplica;
    @FXML
    public Button openFileBttn;

    private final PeerFile peerFile;

    public Controller(PeerFile peerFile) {
        this.peerFile = peerFile;
    }


    @FXML
    public void initialize() throws PeerBoxException {
        String hostname = DnsHelper.getHostName();
        AtomicReference<String> selectedFile = new AtomicReference<>(null);
        AtomicReference<String> clusterSelectedFile = new AtomicReference<>(null);
        ObservableList<String> files = FXCollections.observableArrayList();
        ObservableList<String> remoteFiles = FXCollections.observableArrayList();
        File[] listFiles = peerFile.readDirectory();
        Arrays.stream(listFiles).iterator().forEachRemaining(file -> files.add(file.getName()));
        hostFiles.setItems(files);
        clusterFiles.setItems(remoteFiles);

        SpinnerValueFactory<Integer> valueFactory = replicas.getValueFactory();
        replicas.setOnScroll(event -> {
            if (replicas.isDisabled()) {
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
        replicas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
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
                    peerFile.save(file.getName(), replicas.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(peerFile.getReceiver().clusterSize() > 1){
                        remoteFiles.add(file.getName());
                    }
                    files.add(file.getName());
                    success = true;
                }
            }
            event.setDropCompleted(success);
        });

        hostFiles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedFile.set(newValue);
            logger.info("Node selected: {}",newValue);
        });

        clusterFiles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            clusterSelectedFile.set(newValue);
            logger.info("Cluster selected: {}",newValue);
        });

        openFileBttn.setOnAction((event -> {
            try {
                if (selectedFile.get() != null) {
                    peerFile.fetch(new PeerFileID(null,selectedFile.get(),null,0));
                }
                else {
                    logger.warn("No Node file selected!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        fetchBttn.setOnAction((event -> {
            try {
                if (clusterSelectedFile.get() != null) {
                    peerFile.fetch(new PeerFileID(null,clusterSelectedFile.get(),null,0));
                }
                else {
                    logger.warn("No Cluster file selected!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        deleteBttn.setOnAction((event -> {
            try {
                if (clusterSelectedFile.get() != null){
                    peerFile.delete(new PeerFileID(null,clusterSelectedFile.get(),null,0));
                    remoteFiles.remove(clusterSelectedFile.get());
                    files.remove(clusterSelectedFile.get());
                }
                else {
                    logger.warn("No Cluster file selected!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        deleteOneReplica.setOnAction((event -> {
            if (selectedFile.get() != null) {
                String olFile = selectedFile.get();
                peerFile.getPeerFiles().remove(selectedFile.get());
                files.remove(selectedFile.get());
                File delFile = new File(peerBox + olFile);
                if (delFile.exists()) {
                    delFile.delete();
                }
                logger.info("File has been deleted only on this peer ({}) : {}",hostname,olFile);
            }
            else {
                logger.warn("No Node file selected!");
            }
        }));

    }

}


