package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.App;
import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.IngameController;
import de.uniks.stpmon.team_m.controller.MainMenuController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import javax.inject.Provider;
import java.awt.*;
import java.util.Objects;

import static de.uniks.stpmon.team_m.Constants.*;

public class IngameSettingsController extends Controller {
    @FXML
    public VBox settingsVbox;
    @FXML
    public Button audioSettingsButton;
    @FXML
    public Button keybindingsButton;
    @FXML
    public Button trainerSettingsButton;
    @FXML
    public Button goBackButton;
    @FXML
    public ImageView audioSettingsImageView;
    @FXML
    public ImageView keybindingsImageView;
    @FXML
    public ImageView trainerSettingsImageView;
    @FXML
    public ImageView goBackImageView;
    @Inject
    IngameController ingameController;
    private VBox ingameVbox;
    @Inject
    Provider<IngameController> ingameControllerProvider;
    @Inject
    Provider<MainMenuController> mainMenuControllerProvider;
    @Inject
    Provider<ChangeAudioController> changeAudioControllerProvider;

    @Inject
    public IngameSettingsController() {
    }

    public Parent render() {
        final Parent parent = super.render();
        if (!GraphicsEnvironment.isHeadless()) {
            audioSettingsImageView.setImage(new javafx.scene.image.Image(Objects.requireNonNull(App.class.getResource(AUDIOSYMBOL)).toString()));
            keybindingsImageView.setImage(new javafx.scene.image.Image(Objects.requireNonNull(App.class.getResource(KEYBINDINGSSYMBOL)).toString()));
            goBackImageView.setImage(new javafx.scene.image.Image(Objects.requireNonNull(App.class.getResource(GOBACKSYMBOL)).toString()));
            trainerSettingsImageView.setImage(new javafx.scene.image.Image(Objects.requireNonNull(App.class.getResource(SETTINGSYMBOL2)).toString()));
        }
        return parent;
    }

    public void init(IngameController ingameController, VBox ingameVbox) {
        this.ingameController = ingameController;
        this.ingameVbox = ingameVbox;
    }

    public void openAudioSettings() {
        ingameController.root.getChildren().remove(ingameVbox);
        ingameController.showChangeAudioSettings();
    }

    public void openKeybindings() {
        ingameController.showKeybindings();
    }

    public void openTrainerSettings() {
        ingameController.showTrainerSettings();
    }

    public void goBack() {
        ingameController.root.getChildren().remove(ingameVbox);
        ingameController.buttonsDisable(false);
        ingameController.pauseGame();
    }
}
