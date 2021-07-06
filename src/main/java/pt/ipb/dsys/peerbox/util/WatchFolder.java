package pt.ipb.dsys.peerbox.util;


import pt.ipb.dsys.peerbox.DnsHelper;
import pt.ipb.dsys.peerbox.common.PeerFile;

import java.io.File;
import java.nio.file.*;

import static pt.ipb.dsys.peerbox.Main.peerBox;

public class WatchFolder {

    PeerFile pf;

    public WatchFolder(PeerFile pf) {
        this.pf = pf;
    }

    public void main(String[] args) {

        watchFolder();
    }

    public void watchFolder() {

        try {


            // Create a watch service
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // Get the path of the directory which you want to monitor.
            Path directory = Path.of(peerBox);

            // Register the directory with the watch service
            WatchKey watchKey = directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            // Poll for events
            while (true) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {

                    // Get file name from even context
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;

                    Path fileName = pathEvent.context();

                    // Check type of event.
                    WatchEvent.Kind<?> kind = event.kind();

                    // Perform necessary action with the event
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

                        System.out.println("A new file is created : " + fileName);
                        Path path = Paths.get(peerBox+fileName);
                        pf.setData(Files.readAllBytes(path));
                        int defaultReplicas = 2;
                        pf.save(fileName.toString(), defaultReplicas);

                    }

                    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {

                        String hostname = DnsHelper.getHostName();
                        System.out.println("File has been deleted only on this peer (" + hostname + ") : "+fileName);
                        pf.getPeerFiles().remove(fileName.toString());

                    }
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                        // TODO...
                    }

                    Sleeper.sleep(5000);

                }

                // Reset the watch key everytime for continuing to use it for further event polling
                boolean valid = watchKey.reset();
                if (!valid) {
                    break;
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
