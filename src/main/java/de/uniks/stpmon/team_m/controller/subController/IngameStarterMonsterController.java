package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.App;
import de.uniks.stpmon.team_m.Main;
import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.IngameController;
import de.uniks.stpmon.team_m.dto.MonsterTypeDto;
import de.uniks.stpmon.team_m.service.PresetsService;
import de.uniks.stpmon.team_m.utils.MonsterStorage;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.inject.Inject;
import javax.inject.Provider;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static de.uniks.stpmon.team_m.Constants.ABILITYPALETTE;
import static de.uniks.stpmon.team_m.Constants.TYPESCOLORPALETTE;

public class IngameStarterMonsterController extends Controller {
    @FXML
    public AnchorPane starterSelectionAnchorPane;
    @FXML
    public Label starterSelectionLabel;
    @FXML
    public VBox starterSelectionVBox;
    @FXML
    public HBox starterSelectionHBox;
    @FXML
    public ImageView starterImageView;
    @FXML
    public TextFlow starterDescription;
    @FXML
    public ImageView arrowLeft;
    @FXML
    public ImageView arrowRight;
    @FXML
    public VBox typesVBox;
    @Inject
    IngameController ingameController;
    @Inject
    Provider<TrainerStorage> trainerStorageProvider;
    @Inject
    Provider<MonsterStorage> monsterStorageProvider;
    @Inject
    PresetsService presetsService;
    private List<String> starters;
    public MonsterTypeDto monster1;
    public MonsterTypeDto monster2;
    public MonsterTypeDto monster3;
    public Image monster1Image;
    public Image monster2Image;
    public Image monster3Image;
    public Integer index = 1;


    @Inject
    public IngameStarterMonsterController() {

    }

    public void init(IngameController ingameController, App app, List<String> starters) {
        this.ingameController = ingameController;
        this.app = app;
        this.starters = starters;
    }

    public Parent render() {
        final Parent parent = super.render();
        // Load and display arrows
        if (!GraphicsEnvironment.isHeadless()) {
            Image arrowLeft = new Image(Objects.requireNonNull(Main.class.getResource("images/arrowLeft.png")).toExternalForm());
            Image arrowRight = new Image(Objects.requireNonNull(Main.class.getResource("images/arrowRight.png")).toExternalForm());
            this.arrowLeft.setImage(arrowLeft);
            this.arrowRight.setImage(arrowRight);
        }

        // get monsters
        disposables.add(presetsService.getMonsters().observeOn(FX_SCHEDULER).subscribe(monsterType -> {
            monster1 = monsterType.get(Integer.parseInt(starters.get(0)) - 1);
            monster2 = monsterType.get(Integer.parseInt(starters.get(1)) - 1);
            monster3 = monsterType.get(Integer.parseInt(starters.get(2)) - 1);
            // get Images
            if (monsterStorageProvider.get().imagesAlreadyFetched()) {
                if (!GraphicsEnvironment.isHeadless()) {
                    monster1Image = monsterStorageProvider.get().getMonsterImage(Integer.parseInt(starters.get(0)));
                    monster2Image = monsterStorageProvider.get().getMonsterImage(Integer.parseInt(starters.get(1)));
                    monster3Image = monsterStorageProvider.get().getMonsterImage(Integer.parseInt(starters.get(2)));
                }
                showMonster(1);
            }

        }, error -> showError(error.getMessage())));
        return parent;
    }

    public VBox createTypeVBox(String type, String image) {
        VBox typeVBox = new VBox();
        typeVBox.setMaxSize(32, 32);
        if (TYPESCOLORPALETTE.containsKey(type)) {
            String color = TYPESCOLORPALETTE.get(type);
            typeVBox.setStyle("-fx-background-color: " + color + ";-fx-border-color: black");
        }
        ImageView typeImageView = new ImageView(image);
        typeImageView.setFitHeight(32);
        typeImageView.setFitWidth(32);
        typeVBox.getChildren().add(typeImageView);
        return typeVBox;
    }

    public void showMonster(int index) {
        typesVBox.getChildren().clear();
        starterDescription.getChildren().clear();
        MonsterTypeDto monster;
        Image monsterImage;
        switch (index) {
            default -> {
                monster = monster1;
                monsterImage = monster1Image;
                starterSelectionLabel.setText(resources.getString("FIRST.SELECTION"));
            }
            case 2 -> {
                monster = monster2;
                monsterImage = monster2Image;
                starterSelectionLabel.setText(resources.getString("SECOND.SELECTION"));
            }
            case 3 -> {
                monster = monster3;
                monsterImage = monster3Image;
                starterSelectionLabel.setText(resources.getString("THIRD.SELECTION"));
            }
        }

        // add description
        starterDescription.getChildren().add(new Text(resources.getString("NAME")));
        starterDescription.getChildren().add(new Text(" " + monster.name() + "\n\n"));
        starterDescription.getChildren().add(new Text(monster.description()));

        // add type
        monster.type().forEach(type -> {
            String typeImagePath = ABILITYPALETTE.get(type);
            URL resource = Main.class.getResource("images/" + typeImagePath);
            VBox typeVBox = createTypeVBox(type, Objects.requireNonNull(resource).toString());
            typesVBox.getChildren().add(typeVBox);
        });

        // set image
        if (monsterImage == null) {
            return;
        }
        starterImageView.setImage(monsterImage);
    }

    public void rotateLeft() {
        index--;
        if (index < 1) {
            index = 3;
        }
        showMonster(index);
    }

    public void rotateRight() {
        index++;
        if (index > 3) {
            index = 1;
        }
        showMonster(index);
    }
}
