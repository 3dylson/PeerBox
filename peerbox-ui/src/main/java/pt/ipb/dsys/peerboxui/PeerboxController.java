package pt.ipb.dsys.peerboxui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import org.springframework.stereotype.Component;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;

import java.util.Arrays;
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
    public Spinner defaultReplicasButton;
    @FXML
    public ProgressIndicator progressIndicator;

    private PeerFile peerFile;


    public PeerboxController(PeerFile peerFile) {
        this.peerFile = peerFile;
    }

    @FXML
    public void initialize() throws PeerBoxException {
        ObservableList<String> files = FXCollections.observableArrayList();
        files.add(Arrays.toString(peerFile.readDirectory()));
        peerBoxFiles.setItems(files);

    }


}
