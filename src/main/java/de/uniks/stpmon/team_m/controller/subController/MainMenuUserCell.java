package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.dto.User;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.PopOver;

import static de.uniks.stpmon.team_m.Constants.*;
import static javafx.geometry.Pos.CENTER_RIGHT;

import java.util.prefs.Preferences;


public class MainMenuUserCell extends UserCell {

    public MainMenuUserCell(Preferences preferences) {
        super(preferences);
    }

    @Override
    protected void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            final Button popOverButton = new Button();
            final HBox buttonHBox = new HBox(popOverButton);
            popOverButton.setPrefSize(BUTTON_PREF_SIZE, BUTTON_PREF_SIZE);
            popOverButton.setText(THREE_DOTS);
            popOverButton.setStyle(BUTTON_TRANSPARENT_STYLE);
            buttonHBox.setAlignment(CENTER_RIGHT);
            HBox.setHgrow(buttonHBox, Priority.ALWAYS);
            super.getRootHBox().getChildren().add(buttonHBox);
            popOverButton.setOnAction(event -> showPopOver(popOverButton, item));
        }
    }

    private void showPopOver(Button button, User user) {
        PopOver popOver = new PopOver();
        popOver.setContentNode(new FriendSettingsController(preferences, getListView(), user).render());
        popOver.setDetachable(false);
        popOver.show(button);
    }
}
