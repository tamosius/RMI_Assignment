/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 *
 * @author Tomas
 */
public interface ChatClientInterface extends Remote{
    
    // display the messages for each client on the network
    public void displayMessage(String message) throws RemoteException;
    
    // receive new message from other clients, including list of clients who has seen this message
    public void receiveMessage(Map<String, ChatClientInterface>clientsList, String message)throws RemoteException;
    
    // check if whether the clients are alive on the network
    public boolean isAlive() throws RemoteException;
}
