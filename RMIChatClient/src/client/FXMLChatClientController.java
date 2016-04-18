/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import server.ChatServerInterface;

/**
 * @author Tomas Mikoliunas
 * This class will be used as a 'Stub' object, which will receive each remote method call and pass those to the RMI system,
 * which performs the networking that allows communication between remote objects
 */
public class FXMLChatClientController extends UnicastRemoteObject implements ChatClientInterface, Initializable {

    @FXML
    private TextArea chatTextArea;  // text area to display all messages during the chat
    @FXML
    private TextArea connectedClientsTextArea;  // text area to display what chat users are connected currently

    @FXML
    private TextField serverIpAddress;  // field to type server's IP address to connect, '127.0.0.1' by default
    @FXML
    private TextField submitNameField;  // field to sumbmit the user name to start a chat
    @FXML
    private TextField writeMessageField; // field to write new messages for sending to other chat users

    @FXML
    private Button submitNameButton; // submit chat client name button
    @FXML
    private Button sendMessageButton; // send new message to other clients button

    private String chatServerURL;

    private ChatServerInterface chatServer; // pick up chat server instance

    private Map<String, ChatClientInterface> clientsList;  // clients list returned from the server

    private String myName;  // register your name when starting the chat

    // no-args constructor
    public FXMLChatClientController() throws RemoteException {

    }
    
    // initialize the chat client GUI panel
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // set text fields not editable
        chatTextArea.setEditable(false);
        connectedClientsTextArea.setEditable(false);

        // set 'writeMessageField' not editable until user enters his name
        writeMessageField.setEditable(false);

        try {

            // prompt user to enter his name at the 'submitNameField'
            displayMessage("Hi, please enter the server's IP address \nor leave this field blank if the server using localhost (127.0.0.1) IP address.\nAnd please enter your name at the top field.\n");

        } catch (RemoteException ex) {
            Logger.getLogger(FXMLChatClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // find remote server, method takes as an arguments the IP address and the name of the server on which the remote object 'Chat server' is running.
    public void findServer(String ipAddress) {

        // name (IP address) of remote server object bound to rmi registry   
        chatServerURL = "rmi://" + ipAddress + "/RMIChatServer";

        myName = "";

        try {
            
            //The method 'lookup' connects to the RMI registry and returns a
            //'Remote' reference to remote object
            // lookup chat server remote object
            // 'Chat client' refers to remote 'Chat server' through 'ChatServerInterface'
            // The client can use this remote reference 'chatServer' to invoke remote objects methods
            chatServer = (ChatServerInterface) Naming.lookup(chatServerURL);

            // get all available clients information from the server
            clientsList = chatServer.getClientsList();

            // display them on the right chat client window
            displayClientsNames(clientsList);

        } catch (NotBoundException ex) {
            
            makeAlert("Error!!", ex.toString());
            
        } catch (MalformedURLException ex) {
            
            makeAlert("Error!!", ex.toString());
            
        } catch (RemoteException ex) {  // display error message in the pop-up window if the exception is thrown
           ex.printStackTrace();
           makeAlert("Error!!", "The server's IP address '" + ipAddress + "' you have entered is not available.\n"
                   + "Please enter the known server's IP address.");
        }

    }

    // send message for all 'known clients' on the network
    @FXML
    private void sendMessage() {

        // get the message you just typed in
        String message = writeMessageField.getText();

        // get the current collection of clients locations (IP addresses) on the network 
        // the current collection means the references to the other cliets that 'this' client has
        Collection<ChatClientInterface> locations = clientsList.values();

        // iterate through collection of clients locations
        for (ChatClientInterface clientLocation : locations) {

            try {

                // send message to other clients 'receiveMessage' method, include current 'clientsList'
                clientLocation.receiveMessage(clientsList, myName + ":  " + message);

            } catch (RemoteException e) {
            }
        }

        Platform.runLater(() -> {

            // clear the message field after send
            writeMessageField.setText("");
        });
    }

    // receive new message from other clients, including list of clients who has seen this message
    @Override
    public void receiveMessage(Map<String, ChatClientInterface> clientsList, String message) throws RemoteException {

        displayMessage(message); // received message displayed to this user, then passed on for iteration of known clients
                                 // to determine whether clients has been already processed this message

        // add only a new clients instances received
        // add them to 'this' current client object
        this.clientsList.putAll(clientsList);

        // after updated clients list, refresh clients list on the window in the GUI panel
        displayClientsNames(this.clientsList);

        for (ChatClientInterface client : this.clientsList.values()) { // 'this' refers to this user current client list

            if (!(clientsList).containsValue(client)) { // if message is not seen by other clients, send this message to them
                                                        // this is done by comparing a reference variables
                // send message to other clients 'receiveMessage' method, include updated 'clientsList'
                client.receiveMessage(this.clientsList, message);
            }
        }
    }

    // display the messages in the GUI panel received from other clients
    @Override
    public void displayMessage(String message) throws RemoteException {

        //create another thread inside the main thread in a GUI 'Platform.runLater' 
        Platform.runLater(() -> {

            // display the message in the main text area of the client window
            chatTextArea.appendText(message + "\n");
        });
    }

    // display available clients on the network, get this data by calling 'displayClientsNames' in the 'FXMLChatServerController' class
    private void displayClientsNames(Map<String, ChatClientInterface> clientsList) {

        // get the set of clients names who are on the network (key values from LinkedHashMap)
        Set<String> clientsNames = clientsList.keySet();

        //create another thread inside the main thread in a GUI 'Platform.runLater'
        Platform.runLater(() -> {

            try {

                connectedClientsTextArea.setText("\n");
                // display the clients names on the right-side text area in the GUI panel
                for (String name : clientsNames) {
                    
                    // if the name equals to 'this' object registeted name, let user know that is his current name (You)
                    if (myName.equals(name)) {

                        connectedClientsTextArea.appendText(name + " (You)\n");
                    } else {
                        
                        // display other 'known clients' names in the GUI panel
                        connectedClientsTextArea.appendText(name + "\n");
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("Null pointer in client clients names");
            }
        });
    }
    
    // after the IP address of the remote chat server and the user name have been entered,
    // user can press 'Submit' button to start a chat communication
    @FXML
    private void submitUserDetails() throws RemoteException {
        
        // get server's IP address entered by the user in the text field
        String ipAddress = serverIpAddress.getText().equals("") ? "127.0.0.1" : serverIpAddress.getText();

        // call 'findServer' method and pass that IP address as argument
        // then the chat client would be able to register with the remote chat server
        findServer(ipAddress);

        try {
            
            // get your name you entered in 'submitNameField' and set the first letter upper case and then lower case
            myName = submitNameField.getText().substring(0, 1).toUpperCase() + submitNameField.getText().substring(1).toLowerCase();
            
            // if name field left blank, throw/catch an exception and display the appropriate message
        } catch (StringIndexOutOfBoundsException e) {
            
            makeAlert("Warning!!", "The name field cannot be empty!\nPlease re-enter.");
            return; // break method execution
        }
        
        // if the name already exist in the chat network, call 'makeAlert' function with appropriate message
        // asking to choose another name
        if (clientsList.containsKey(myName)) {

            makeAlert("Warning!!", "The name: '" + myName + "' is already exist on the network!\nPlease choose another name.");
            
        } else {

            Platform.runLater(() -> {

                // display the IP address and the client name in the top fields
                submitNameField.setText(myName);
                serverIpAddress.setText(ipAddress);
            });

            // display welcome message on the main chat area
            displayMessage("Welcome, " + myName + "!\n You can start a chat now with the other clients on the network!\n");

            new Thread(() -> { // start the new chat client thread
                               // by using the remote 'chatServer' reference invoke the 'registerChatClient' method to register with the server
                try {          // pass as arguments 'myName' entered at the top field and the reference to the current 'this' chat client object

                    chatServer.registerChatClient(myName, this);

                    // refresh clients list after creating this instance
                    refreshClientsList();

                } catch (RemoteException ex) {
                    Logger.getLogger(FXMLChatClientController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();

            // after user submits the name to the server, set 'writeMessageField' editable for writing the messages
            writeMessageField.setEditable(true);
            // set 'submitNameField', 'ipAddressField' not editable 
            submitNameField.setEditable(false);
            serverIpAddress.setEditable(false);
            // disable 'Submit' button
            submitNameButton.setDisable(true);
        }
    }

    // refresh clients list on request
    // pressing the 'Refresh List' button in the GUI panel, clients calls the 'refreshClientsList' method in 'FXMLChatServerController'
    // and refreshes the current list of all 'known clients' on the network
    @FXML
    public void refreshClientsList() throws RemoteException {

        // get the new (updated) clients list from the server
        clientsList = chatServer.getClientsList();

        // display them on the right chat client window
        displayClientsNames(clientsList);
    }

    // if 'this' client object is still alive on the network, return true
    // this method is called by the chat server every 2 seconds to verify 
    @Override
    public boolean isAlive() throws RemoteException {

        return true;
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
