package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.IngameController;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyEvent;
import javax.inject.Inject;

public class IngameKeybindingsController extends Controller {

    @FXML
    public Button walkUpButton;
    @FXML
    public Button walkDownButton;
    @FXML
    public Button walkRightButton;
    @FXML
    public Button walkLeftButton;
    @FXML
    public Button interactionButton;
    @FXML
    public Button pauseMenuButton;
    @FXML
    public Button goBackButton;
    @FXML
    public Button defaultButton;
    @FXML
    public Button checkButton;
    @FXML
    public Label informationLabel;
    @Inject
    IngameController ingameController;
    private VBox ingameVbox;
    private EventHandler<KeyEvent> keyPressedHandler;

    @Inject
    public IngameKeybindingsController() {
    }

    public Parent render() {
        Parent parent = super.render();
        walkUpButton.setText(preferences.get("walkUp","W"));
        walkDownButton.setText(preferences.get("walkDown","S"));
        walkRightButton.setText(preferences.get("walkRight","D"));
        walkLeftButton.setText(preferences.get("walkLeft","A"));
        interactionButton.setText(preferences.get("interaction","E"));
        pauseMenuButton.setText(preferences.get("pauseMenu","ESC"));
        return parent;
    }

    public void init(IngameController ingameController, VBox ingameVbox) {
        this.ingameController = ingameController;
        this.ingameVbox = ingameVbox;
    }

    public void goBack() {
        ingameController.root.getChildren().remove(ingameVbox);
    }

    public void setDefault() {
    }

    public void check() {
    }

    public void setWalkLeft() {
        setKeyPressedHandler(walkLeftButton);
    }

    public void setPauseMenu() {
        setKeyPressedHandler(pauseMenuButton);
    }

    public void setWalkRight() {
        setKeyPressedHandler(walkRightButton);
    }

    public void setInteraction() {
        setKeyPressedHandler(interactionButton);
    }

    public void setWalkDown() {
        setKeyPressedHandler(walkDownButton);
    }

    public void setWalkUp() {
        setKeyPressedHandler(walkUpButton);
    }

    private void setKeyPressedHandler(Button button){
        keyPressedHandler = event -> {
            if(event.getCode() == KeyCode.SPACE){
                button.setText(preferences.get("walkLeft","A"));
                informationLabel.setText(resources.getString("WRONG.INPUT"));
                event.consume();
            }
            else if(event.getCode() == KeyCode.UP) {
                informationLabel.setText(resources.getString("CLICK.CHECK"));
                button.setText("");
                button.setText("\u2191");
            }
            else if(event.getCode() == KeyCode.DOWN){
                informationLabel.setText(resources.getString("CLICK.CHECK"));
                button.setText("");
                button.setText("\u2193");
            }
            else if(event.getCode() == KeyCode.RIGHT){
                informationLabel.setText(resources.getString("CLICK.CHECK"));
                button.setText("");
                button.setText("\u2192");
            }
            else if(event.getCode() == KeyCode.LEFT){
                informationLabel.setText(resources.getString("CLICK.CHECK"));
                button.setText("");
                button.setText("\u2190");
            }
            else if (event.getCode() == KeyCode.ESCAPE) {
                informationLabel.setText(resources.getString("CLICK.CHECK"));
                button.setText("");
                button.setText("ESC");
            } else {
                if (event.getText().length() != 0 && Character.isLetterOrDigit(event.getText().charAt(0))) {
                    informationLabel.setText(resources.getString("CLICK.CHECK"));
                    button.setText("");
                    button.setText(event.getText().toUpperCase());
                } else {
                    informationLabel.setText(resources.getString("WRONG.INPUT"));
                    button.setText("A");
                }
            }
            button.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            buttonsDisable(false, button);
        };
        buttonsDisable(true, button);
        button.setText("...");
        informationLabel.setText(resources.getString("WAITING.INPUT"));
        button.addEventFilter(KeyEvent.KEY_PRESSED,keyPressedHandler);
    }
    public void buttonsDisable(boolean set, Button currentbutton){
        walkUpButton.setDisable(set);
        walkRightButton.setDisable(set);
        walkLeftButton.setDisable(set);
        walkDownButton.setDisable(set);
        pauseMenuButton.setDisable(set);
        interactionButton.setDisable(set);
        if(set){
            currentbutton.setDisable(false);
        }
    }
}
