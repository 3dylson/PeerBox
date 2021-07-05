package pt.ipb.dsys.peerboxui;

import com.sun.javafx.binding.BidirectionalBinding;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.springframework.stereotype.Component;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static pt.ipb.dsys.peerbox.Main.peerBox;

@Component
public class PeerboxController  {

    @FXML
    public ListView<String> peerBoxFiles;
    @FXML
    public ListView<String> clusterFiles;
    @FXML
    public Button fetchButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Spinner<Integer> defaultReplicasButton;
    @FXML
    public ProgressIndicator progressIndicator;

    private PeerFile peerFile;


    public PeerboxController(PeerFile peerFile) {
        this.peerFile = peerFile;
    }

    @FXML
    public void initialize() throws PeerBoxException {
        ObservableList<String> files = FXCollections.observableArrayList();
        File[] listFiles = peerFile.readDirectory();
        Arrays.stream(listFiles).iterator().forEachRemaining(file -> files.add(file.getName()));
        peerBoxFiles.setItems(files);

        //defaultReplicasButton = new Spinner<>(1, 20, 2);
        SpinnerValueFactory<Integer> valueFactory = defaultReplicasButton.getValueFactory();
        defaultReplicasButton.setOnScroll(event -> {
            if( defaultReplicasButton.isDisabled()) {
                return;
            }
            if (!defaultReplicasButton.isVisible()) {
                return;
            }
            double delta = event.getDeltaY();
            if (delta == 0.0) {
                return;
            }
            int increment = delta < 0 ? -1 : +1;
            valueFactory.setValue(defaultReplicasButton.getValue() + increment);
        });
        defaultReplicasButton.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,20,2));
        //BidirectionalBinding.bindNumber(,valueFactory.valueProperty());
        //defaultReplicasButton.setEditable(true);
        //Spinner spinner = new Spinner(0, 10, 5);
        //defaultReplicasButton.setValueFactory(new Spinn);

        peerBoxFiles.setOnDragOver(event -> {
            if (event.getGestureSource() != peerBoxFiles
                    && event.getDragboard().hasFiles()) {
                /* allow for both copying and moving. */
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        peerBoxFiles.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                //TODO Copy multiple files at once
                File file = db.getFiles().get(0);
                Path path = Paths.get(db.getFiles().get(0).toString());
                try {
                    peerFile.setData(Files.readAllBytes(path));
                    peerFile.save(file.getName(),defaultReplicasButton.getValue());
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
