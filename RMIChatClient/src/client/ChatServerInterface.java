/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import client.ChatClientInterface;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 *
 * @author Tomas
 */
public interface ChatServerInterface extends Remote {

    // register chat clients to server
    public void registerChatClient(String clientName, ChatClientInterface rmiClient) throws RemoteException;

    /**
     * Request a set of clients from the chat server.
     */
    public abstract Map getClientsList() throws RemoteException;
}
