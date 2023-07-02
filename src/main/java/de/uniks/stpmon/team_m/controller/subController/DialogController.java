package de.uniks.stpmon.team_m.controller.subController;

import de.uniks.stpmon.team_m.Constants;
import de.uniks.stpmon.team_m.controller.Controller;
import de.uniks.stpmon.team_m.controller.IngameController;
import de.uniks.stpmon.team_m.dto.Trainer;
import de.uniks.stpmon.team_m.utils.TrainerStorage;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Objects;
import static de.uniks.stpmon.team_m.Constants.ContinueDialogReturnValues.*;

public class DialogController extends Controller {
    @Inject
    Provider<TrainerStorage> trainerStorageProvider;
    @Inject
    IngameController ingameController;

    private final Trainer npc;
    private final boolean alreadyEncountered;
    private final NpcTextManager npcTextManager;
    private String[] npcTexts;
    private int currentTextIndex;
    private final Text currentText;
    private final int amountOfTexts;
    private int starterSelection;
    private boolean alreadySeenNurseDialog = false;
    private boolean wantsHeal;
    private boolean noMons;
    private boolean starterSelected = false;

    @Inject
    public DialogController(Trainer npc, TextFlow dialogTextFlow, boolean alreadyEncountered, NpcTextManager npcTextManager, Trainer player, IngameController ingameController) {
        this.npc = npc;
        this.alreadyEncountered = alreadyEncountered;
        this.npcTextManager = npcTextManager;
        this.ingameController = ingameController;

        try {
            // check if npc can heal
            if (npc.npc().canHeal()) {
                // check if player has monsters
                if (player.team().size() == 0) {
                    this.npcTexts = npcTextManager.getNpcTexts("NurseNoMons");
                } else {
                    this.npcTexts = npcTextManager.getNpcTexts("Nurse");
                }
            } else {
                // check if player already encountered albert
                // -- maybe this can be swapped with starters == null, but not all npc's have the starters attribute
                if (Objects.equals(npc._id(), "645e32c6866ace359554a802") && alreadyEncountered) {
                    this.npcTexts = npcTextManager.getNpcTexts(npc._id() + "alreadyEncountered");
                } else {
                    this.npcTexts = npcTextManager.getNpcTexts(npc._id());
                }
            }

        } catch (Error e) {
            System.err.println("NPC does not have canHeal() attribute..");
            this.npcTexts = npcTextManager.getNpcTexts(npc._id());
        }

        this.currentTextIndex = 0;
        assert this.npcTexts != null;
        this.currentText = new Text(this.npcTexts[currentTextIndex]);
        this.amountOfTexts = npcTexts.length;

        dialogTextFlow.getChildren().add(currentText);
    }

    /**
     * Method to continue the dialog.
     * @param specialInteraction Whether the player had a popup with a special interaction with a npc
     * @return A continueDialogReturnValue, which determines what happens after the dialog
     */
    public Constants.ContinueDialogReturnValues continueDialog(Constants.DialogSpecialInteractions specialInteraction) {
        // check if a special interaction has been triggered
        if (specialInteraction != null) {
            switch (specialInteraction) {
                case nurseYes -> {
                    this.currentText.setText(npcTextManager.getSingleNpcText("NPC.NURSE.YES.DIALOG"));
                    this.wantsHeal = true;
                    this.alreadySeenNurseDialog = true;
                }
                case nurseNo -> {
                    this.currentText.setText(npcTextManager.getSingleNpcText("NPC.NURSE.NO.DIALOG"));
                    this.wantsHeal = false;
                    this.alreadySeenNurseDialog = true;
                }
                case nurseNoMons -> {
                    if (!this.noMons) {
                        this.currentText.setText(npcTextManager.getSingleNpcText("NPC.NURSE.NO.MONS1"));
                        this.wantsHeal = false;
                        this.alreadySeenNurseDialog = true;
                        this.noMons = true;
                    } else {
                        return dialogFinishedNoTalkToTrainer;
                    }
                }
                case starterSelection0 -> {
                    this.currentText.setText(npcTextManager.getSingleNpcText("NPC.ALBERT.CHOSE.MONSTER"));
                    this.starterSelection = 0;
                    this.starterSelected = true;
                }
                case starterSelection1 -> {
                    this.currentText.setText(npcTextManager.getSingleNpcText("NPC.ALBERT.CHOSE.MONSTER"));
                    this.starterSelection = 1;
                    this.starterSelected = true;
                }
                case starterSelection2 -> {
                    this.currentText.setText(npcTextManager.getSingleNpcText("NPC.ALBERT.CHOSE.MONSTER"));
                    this.starterSelection = 2;
                    this.starterSelected = true;
                }
            }
            return dialogNotFinished;
        }

        // check if at the end of dialog
        if (++currentTextIndex >= amountOfTexts) {
            if (alreadySeenNurseDialog) {
                if (wantsHeal) {
                    return dialogFinishedTalkToTrainer;
                } else {
                    return dialogFinishedNoTalkToTrainer;
                }
            }

            if (starterSelected) {
                switch (starterSelection) {
                    case 0 -> {
                        return albertDialogFinished0;
                    }
                    case 1 -> {
                        return albertDialogFinished1;
                    }
                    case 2 -> {
                        return albertDialogFinished2;
                    }
                }
            }

            if (npc.npc().canHeal()) {
                return spokenToNurse;
            }

            if (npc.npc().encounterOnTalk()) {
                return encounterOnTalk;
            }

            if (Objects.equals(this.npc._id(), "645e32c6866ace359554a802") && !this.alreadyEncountered) {
                ingameController.showStarterSelection(this.npc.npc().starters());
                return dialogNotFinished;
            } else {
                return dialogFinishedTalkToTrainer;
            }
        } else {
            this.currentText.setText(npcTexts[currentTextIndex]);
            return dialogNotFinished;
        }
    }
}