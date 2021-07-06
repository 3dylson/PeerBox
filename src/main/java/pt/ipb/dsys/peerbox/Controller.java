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
import org.jgroups.JChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.Sleeper;
import pt.ipb.dsys.peerbox.util.WatchCallable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static pt.ipb.dsys.peerbox.Main.*;

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

    private PeerFile peerFile;

    public Controller(PeerFile peerFile) {
        this.peerFile = peerFile;
    }


    @FXML
    public void initialize() {

        // Cluster initialization and connection
        try (JChannel channel = peerFile.getChannel()) {

            logger.info("Starting a background thread for watching folders.");
            //new Thread(new WatchFolder()).start();
            ExecutorService executor = Executors.newCachedThreadPool();
            channel.setReceiver(peerFile.getReceiver());
            channel.setDiscardOwnMessages(true);  // <-- Own messages will be discarded
            channel.connect(CLUSTER_NAME);

            // optional: let cluster stabilize to reduce ambiguity, although it works all the same
            Sleeper.sleep(5000);

            executor.submit(new WatchCallable(peerFile));
            String hostname = DnsHelper.getHostName();

            File dir = new File(peerBox);
            if (!dir.exists()) {
                dir.mkdir();
            }

            ObservableList<String> files = FXCollections.observableArrayList();
            File[] listFiles = peerFile.readDirectory();
                Arrays.stream(listFiles).iterator().forEachRemaining(file -> files.add(file.getName()));
                hostFiles.setItems(files);

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


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}

