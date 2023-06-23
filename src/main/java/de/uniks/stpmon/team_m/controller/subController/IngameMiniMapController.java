package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.IngameController;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import javax.inject.Provider;

public class IngameMiniMapController extends Controller {
    @FXML
    public Button closeButton;
    @FXML
    public ImageView mapImageView;
    @FXML
    public Label regionName;
    @FXML
    public AnchorPane mapAnchorPane;
    @Inject
    Provider<IngameController> ingameControllerProvider;
    @Inject
    Provider<TrainerStorage> trainerStorageProvider;

    @Inject
    public IngameMiniMapController() {
    }

    public Parent render() {
        final Parent parent = super.render();
        regionName.setText(trainerStorageProvider.get().getRegion().name());
        mapImageView.setImage(trainerStorageProvider.get().getRegionMap());
        return parent;
    }

    public void closeMap() {
        StackPane root = (StackPane) mapAnchorPane.getParent().getParent();
        root.getChildren().remove(1);
        root.getChildren().get(0).setEffect(null);
    }
}
