/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import client.ChatClientInterface;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * @author Tomas Mikoliunas 
 * This class will be used as a 'Stub' object, which will receive each remote method call and pass those to the RMI system,
 * which performs the networking that allows communication between remote objects.
 * This class also has to be generated using 'rmic' (done already) compiler in order to client access this class from remote machine.
 * This compiler includes required communication protocol.
 */
public class FXMLChatServerController extends UnicastRemoteObject implements ChatServerInterface, Initializable {

    @FXML
    private TextArea mainTextArea;  // the main text area where the information about the server is displayed
    @FXML
    private TextArea clientsNamesTextArea;  // the text area at the right-hand side, to display the connected 'known clients' names

    @FXML
    private TextField ipAddressField; // top field to enter the IP address of the server's registry 

    @FXML
    private Button startServerButton; // button to start the server after all IP address has been typed in

    @FXML
    private Label numberOfClients;  // displays the number of the clients connected to the network

    private static Map<String, ChatClientInterface> chatClients;  // 'Map' will hold all the references to the all connnected 'known clients' on the network

    private Registry rmiRegistry;  // 'rmiregistry' to which chat server connects

    // no-args constructor
    public FXMLChatServerController() throws RemoteException {

        // initialize the 'LinkedHashMap'
        chatClients = new LinkedHashMap<>();
    }
    
    // initialize the chat server's GUI panel
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // initialize 'numberOfClients' label
        numberOfClients.setText("");

        // initialize 'ipAddressField' TextField
        ipAddressField.setText("");

        mainTextArea.setEditable(false);
        clientsNamesTextArea.setEditable(false);
        
        // prompt user to enter IP address at the top field in the server's GUI panel
        mainTextArea.setText("Please enter the IP address at the top field.\nor leave it blank ('127.0.0.1' will be used by default).");
    }

    // start server by pressing 'Start Server' button in the server's GUI panel
    @FXML
    private void startServer() {

        // get IP address specified in 'ipAddressField'. If not specified, 'localhost' by default
        String ipAddress = ipAddressField.getText().equals("") ? "127.0.0.1" : ipAddressField.getText();

        // specify remoter object name
        String serverName = "rmi://" + ipAddress + "/RMIChatServer";

        try {

            // create RMI registry
            rmiRegistry = LocateRegistry.createRegistry(1099);

            // create remote object
            // bind chat server remote object in RMI registry
            Naming.rebind(serverName, new FXMLChatServerController());

            // display IP address in 'ipAddressField'
            ipAddressField.setText(ipAddress);
            // disable textField for IP address and 'Start Server' button
            ipAddressField.setEditable(false);
            startServerButton.setDisable(true);

            // display the time of server started
            displayMessage("Server started using '" + ipAddress + "' IP address on port: '1099'\nat: " + new Date());
            // display the number of clients currently on the network
            displayNumberOfClients();

            mainTextArea.setText("");

            // create Thread which periodically verifies whether clients are still reachable
            new Thread(() -> {

                while (true) {

                    try {

                        Thread.sleep(2000);  // verify every 2 seconds

                    } catch (InterruptedException ex) {

                        Logger.getLogger(FXMLChatServerController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // get all clients entries into Iterator to prevent 'ConcurrentModificationException' if modifying 'LindedHashMap'
                    Iterator<Map.Entry<String, ChatClientInterface>> iterator = chatClients.entrySet().iterator();

                    // iterate through them verify if whether clients are still 'alive'
                    while (iterator.hasNext()) {

                        // returns the next element (object) from the 'LinkedHashMap'
                        Entry<String, ChatClientInterface> client = iterator.next();

                        try {

                            // 'getValue' method returns referenceto object of 'ChatClientInterface',
                            // and calling 'isAlive' method in 'FXMLChatClientController.java' verify if that client is still reachable
                            // if exception is thrown, catch it and remove that client reference from 'chatClients' list
                            if (client.getValue().isAlive() == true) {

                                // if return value is 'true', do nothing (means client is still reachable)
                            }

                        } catch (RemoteException ex) {

                            iterator.remove();  // remove the client from a list if a reference is null
                        }

                    }

                    // get set of clients names on the network (updated) and show them in the window
                    Set<String> clientsNames = chatClients.keySet();
                    // call 'displayClientsNames' method and display the names in the server's GUI panel
                    displayClientsNames(clientsNames);
                    // display the number of clients after update, display them in the server's GUI panel
                    displayNumberOfClients();
                }
            }).start(); // starts Thread
            
        } catch (RemoteException ex) {
            
            makeAlert("Warning!!", "Connection refused to host '" + ipAddress + "'.\n"
                    + "Please check IP address entered.");
            // shut down this registry if exception is thrown
            try {

                UnicastRemoteObject.unexportObject(rmiRegistry, true);

            } catch (NoSuchObjectException ex1) {
                
                Logger.getLogger(FXMLChatServerController.class.getName()).log(Level.SEVERE, null, ex1);
                
            } finally {
                
                rmiRegistry = null;
                
            }
        } catch (MalformedURLException ex) {
            
            // display appropriate message if the exception is thrown when server tried connect to registry
            makeAlert("Warning!!", "Connection refused to '" + ipAddress + "'. Invalid authority!\n"
                    + "Please check IP address entered.");
            // shut down this registry if exception is thrown
            try {

                UnicastRemoteObject.unexportObject(rmiRegistry, true);

            } catch (NoSuchObjectException ex1) {
                
                Logger.getLogger(FXMLChatServerController.class.getName()).log(Level.SEVERE, null, ex1);
                
            } finally {
                
                rmiRegistry = null;
            }
        }
    }

    // when 'ChatClient' successfully connects to the server
    // add 'ChatClient' instance to the 'chatClients' LinkedHashMap
    @Override
    public synchronized void registerChatClient(String clientName, ChatClientInterface rmiClient) throws RemoteException {

        // add 'ChatClient' instance to the 'chatClients' LindedHashMap
        chatClients.put(clientName, rmiClient);
        // display the number of clients currently on the network
        displayNumberOfClients();
    }

    
    // Request a set of clients from the chat server.
    // this method is called from the 'ChatClients' objects
    @Override
    public Map getClientsList() throws RemoteException {

        return chatClients;
    }

    // display the any message requested in the main server window (the server started time, who just connected, etc..)
    private void displayMessage(String message) {

        //create another thread inside the main thread in a GUI 'Platform.runLater' 
        Platform.runLater(() -> {

            // display the message in the main text area of the server window
            mainTextArea.appendText(message + "\n");
        });
    }

    // display available clients on the network
    // dipslay it in the server's GUI panel
    private void displayClientsNames(Set<String> clientsNames) {

        //create another thread inside the main thread in a GUI 'Platform.runLater'
        Platform.runLater(() -> {

            try {

                clientsNamesTextArea.setText("\n");
                // display the clients names on the right text area of the window
                for (String name : clientsNames) {

                    clientsNamesTextArea.appendText(name + "\n");
                }
            } catch (NullPointerException e) {
                System.out.println("Null pointer in Servers clients names");
            }
        });
    }

    // display the number of clients currently on the network
    @FXML
    private void displayNumberOfClients() {

        //create another thread inside the main thread in a GUI 'Platform.runLater' 
        Platform.runLater(() -> {

            try {

                // display the number of clients currently on the network
                numberOfClients.setText(String.valueOf(chatClients.size()));
            } catch (NullPointerException e) {
                
                System.out.println("Null pointer in Servers clients numbers");
            }
        });
    }

    // return the number of clients currently on the network
    // display it in the server's GUI panel
    public String getNumberOfClients() {

        return String.valueOf(chatClients.size());
    }

    // pop-up alert window on some inappropriate actions
    public void makeAlert(String warning, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle("Information");
        alert.setHeaderText(warning);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
