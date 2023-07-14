package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.IngameController;
import de.uniks.stpmon.team_m.service.AudioService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class ChangeAudioController extends Controller {
    @Inject
    public ChangeAudioController() {
    }

    @FXML
    private Button closeButton;

    @FXML
    private Slider audioSlider;

    private VBox changeAudioVBox;

    @Inject
    IngameController ingameController;

    @Override
    public Parent render() {
        final Parent parent = super.render();
        if (preferences.getBoolean("mute", false)) {
            audioSlider.setValue(0);
        } else {
            audioSlider.setValue(preferences.getDouble("volume", AudioService.getInstance().getVolume()) * 100);
        }
        return parent;
    }


    public void init(IngameController ingameController, VBox changeAudioVBox) {
        this.ingameController = ingameController;
        this.changeAudioVBox = changeAudioVBox;
    }

    public void onCloseButtonClick() {
        ingameController.root.getChildren().remove(changeAudioVBox);
        ingameController.showSettings();
    }

    public void getSliderValue() {
        audioSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == 0) {
                preferences.putBoolean("mute", true);
            } else {
                preferences.putBoolean("mute", false);
                AudioService.getInstance().unmuteSound();
            }
            AudioService.getInstance().setVolume(newValue.doubleValue() / 100);
            preferences.putDouble("volume", newValue.doubleValue() / 100);
        });
    }
}