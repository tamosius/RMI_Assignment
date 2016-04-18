/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * @author Tomas Mikoliunas
 * 
 * Class from which starts the GUI and the chat server application
 */
public class StartChatServer extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader serverLoader = new FXMLLoader();
        serverLoader.setLocation(StartChatServer.class.getResource("FXMLChatServer.fxml"));
            
        Parent serverRoot = (Parent)serverLoader.load();
        
        Scene scene = new Scene(serverRoot);
        stage.setTitle("Chat Server");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        
        // terminate program after window is closed
        stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            
            @Override
            public void handle(WindowEvent t){
                Platform.exit();
                System.exit(0);
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
