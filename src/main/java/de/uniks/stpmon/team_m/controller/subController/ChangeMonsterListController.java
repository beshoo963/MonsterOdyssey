package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.EncounterController;
import de.uniks.stpmon.team_m.controller.IngameController;
import de.uniks.stpmon.team_m.dto.Item;
import de.uniks.stpmon.team_m.dto.Monster;
import de.uniks.stpmon.team_m.service.MonstersService;
import de.uniks.stpmon.team_m.service.PresetsService;
import de.uniks.stpmon.team_m.utils.EncounterOpponentStorage;
import de.uniks.stpmon.team_m.utils.MonsterStorage;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ChangeMonsterListController extends Controller {
    @Inject
    EncounterController encounterController;
    @Inject
    IngameController ingameController;
    @Inject
    public Provider<PresetsService> presetsServiceProvider;
    @Inject
    Provider<TrainerStorage> trainerStorageProvider;
    @Inject
    Provider<EncounterOpponentStorage> encounterOpponentStorageProvider;
    @Inject
    MonstersService monstersService;
    @FXML
    public ListView<Monster> changeMonsterListView;
    public VBox monsterListVBox;
    public List<Monster> activeMonstersList;
    @Inject
    public Provider<MonsterStorage> monsterStorageProvider;
    private Item item;
    public Runnable onItemUsed;

    @Inject
    public ChangeMonsterListController() {
    }

    public void init(EncounterController encounterController, VBox monsterListVBox, IngameController ingameController, Item item, Runnable onItemUsed) {
        super.init();
        activeMonstersList = new ArrayList<>();
        this.encounterController = encounterController;
        this.monsterListVBox = monsterListVBox;
        this.ingameController = ingameController;
        this.item = item;
        this.onItemUsed = onItemUsed;
    }

    @Override
    public String getTitle() {
        return resources.getString("MONSTERS");
    }

    @Override
    public Parent render() {
        final Parent parent = super.render();
        disposables.add(monstersService.getMonsters(trainerStorageProvider.get().getRegion()._id(), trainerStorageProvider.get().getTrainer()._id()).observeOn(FX_SCHEDULER)
                .subscribe(list -> {
                    activeMonstersList = list.stream()
                            .filter(monster -> trainerStorageProvider.get().getTrainer().team().contains(monster._id())
                                    && (!Objects.equals(monster._id(), encounterOpponentStorageProvider.get().getSelfOpponent().monster())
                                    && !(encounterOpponentStorageProvider.get().isTwoMonster() && Objects.equals(monster._id(), encounterOpponentStorageProvider.get().getCoopOpponent().monster()))
                                    && monster.currentAttributes().health() > 0
                                    || item != null))
                            .collect(Collectors.toList());
                    initMonsterList(activeMonstersList);
                }, throwable -> showError(throwable.getMessage())));
        return parent;
    }

    private void initMonsterList(List<Monster> monsters) {
        changeMonsterListView.setCellFactory(param ->
                new MonsterCell(
                        resources,
                        presetsServiceProvider.get(),
                        null,
                        this,
                        this.encounterController,
                        this.ingameController,
                        false,
                        item,
                        monsterStorageProvider
                ));
        changeMonsterListView.getItems().addAll(monsters);
        changeMonsterListView.setFocusModel(null);
        changeMonsterListView.setSelectionModel(null);
    }

    public void onCloseMonsterList() {
        encounterController.rootStackPane.getChildren().remove(monsterListVBox);
    }
}
