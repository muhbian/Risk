package risk.game.client;

import risk.common.Logger;
import risk.game.*;
import risk.network.QueuedSender;
import risk.protocol.command.DoAttackCmd;
import risk.protocol.command.PlaceReinforcementCmd;
import risk.view.client.View;

public class ClientGameLogic implements Controller {
    private GameView gameView;
    private GameController gameCtrl;
    private View view;
    private QueuedSender sender;
    
    private CountryPair currentAttack;

    public ClientGameLogic(GameView gameView, GameController gameCtrl) {
        super();
        this.gameView = gameView;
        this.gameCtrl = gameCtrl;
    }

    public void setSender(QueuedSender sender) {
        this.sender = sender;
    }
    
    @Override
    public void onCountryClick(String country) {
        Country c=gameView.getCountry(country);
        RoundPhase phase = gameView.getRoundPhase();
        
        if (phase == RoundPhase.REINFORCEMENT) {
            view.showReinforcementDialog(c);
        } else {
            Country selected;
            Logger.logdebug("Country \"" + c.getName() + "\" clicked");
            if (c.isSelected()) {
                gameCtrl.cancelCountrySelection(c);
            } else if ((selected = gameView.getSelectedCountryNeighbour(c)) != null) {
                Logger.logdebug("Action needed, current round phase: " + phase.toString());
                switch (phase) {
                case ATTACK:
                    if (currentAttack != null) {
                        Logger.logerror("CurrentAttack not null, when initiating new attack!");
                    }
                    currentAttack = new CountryPair(selected, c);
                    sender.queueForSend(new DoAttackCmd(currentAttack.From, currentAttack.To));
                    break;
                case REGROUP:
                    view.showRegroupDialog(new CountryPair(selected, c));
                    break;
                default:
                    Logger.logwarn("Invalid game phase!");
                }
                gameCtrl.cancelCountryNeighbourSelection(c);
                // Do something like attack or regroup
            } else {
                gameCtrl.selectCountry(c);
            }
        }
    }

    @Override
    public GameView getGameView() {
        return gameView;
    }

    public void setView(View view) {
        this.view = view;
    }

    @Override
    public boolean onReinforcementDialogOK(Country c, int troops) {
        int availableReinforcement = gameView.getAvailableReinforcement();
        if (availableReinforcement <= troops) {
            sender.queueForSend(new PlaceReinforcementCmd(c, troops));
            return true;
        }
        return false;
    }

    @Override
    public boolean onAttackDialog(boolean continuing) {
        if (currentAttack == null) {
            Logger.logerror("CurrentAttack is null, when attackdialog was OKd");
            return true;
        }
        if (continuing) {
            Logger.logdebug("Continuing attack");
            sender.queueForSend(new DoAttackCmd(currentAttack.From, currentAttack.To));
        } else {
            currentAttack = null;
        }
        return true;
    }

}
