package pt.ipb.dsys.peerboxui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import org.springframework.stereotype.Component;
import pt.ipb.dsys.peerbox.common.PeerFile;

@Component
public class PeerboxController {

    @FXML
    public ListView peerBoxFiles;
    @FXML
    public ListView clusterFiles;
    @FXML
    public Button fetchButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Spinner defaultReplicasButton;
    @FXML
    public ProgressIndicator progressIndicator;

    private PeerFile peerFile;

    /*public PeerboxController(PeerFile peerFile) {
        this.peerFile = peerFile;
    }*/
}
