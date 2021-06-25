package pt.ipb.dsys.peerbox.common;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static pt.ipb.dsys.peerbox.Main.CLUSTER_NAME;

public class GUI {

    private JPanel peerBox;
    private JList peerBoxFiles;
    private JList replicas;
    private JTextPane fileInfos;
    private JSpinner numOfreplicas;
    private JLabel membersLabel;
    private JButton fetchButton;
    private JLabel replicasSave;
    private JLabel peerFilesLabel;
    private JLabel replicasFilesLabel;

    public GUI() {
        initGUI();

        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        numOfreplicas.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

            }
        });
        peerBoxFiles.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

            }
        });
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initGUI() {
        JFrame frame = new JFrame(CLUSTER_NAME);
        frame.setContentPane(new GUI().peerBox);
        frame.setBounds(100,100,450,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);



    }

}
