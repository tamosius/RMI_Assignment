/*
 * @author Tomas Mikoliunas
 * class from which starts the GUI and chat client application
 */
package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Tomas
 */
public class StartChatClient extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLChatClient.fxml"));
        
        Scene scene = new Scene(root);
        stage.setTitle("Chat Client");
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
