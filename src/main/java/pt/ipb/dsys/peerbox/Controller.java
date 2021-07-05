package pt.ipb.dsys.peerbox;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    public ListView<String> hostFiles;
    public ListView clusterFiles;
    public Spinner replicas;
    public Button fetchBttn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

}
