package risk.game;

import java.util.Collection;
import java.util.Iterator;

public interface GameController {
    // Will be filled with methods that control the game, give orders, take
    // actions
    // doAttack(Country from, Country to);
    // doRegroup(Country from, Country to, int howmanyunits);
    // connectToServer()
    // disconnect()

    public void addPlayer(Player p);
    public void setMyPlayer(Player p);
    public boolean switchToNextPlayer();
    public void setRoundPlayers(Collection<Player> players);
    public void initCountry(Country newCountry);

    public void cancelCountrySelection(Country c);
    public void selectCountry(Country c);
    public void cancelCountryNeighbourSelection(Country c);
    
    public void setRoundNumber(int roundNumber);
    public void setRoundPhases(Collection<RoundPhase> roundPhases);
    public void resetPhases();
    public boolean swicthToNextPhase();
    public void setAvailableReinforcement(int availableReinforcement);
    public void addTroopsToCountry(Country country, int troops);
    public void regroup(CountryPair countryPair, int troops);
    public void setAttack(Attack attack);
    public void accountAttackLosses(int aLosses, int dLosses);
    public void setAttackDDice(int dice);
    public void setAttackADice(int dice);
    public void clearAttack();
    public void setAttackRoundResults(Collection<Integer> aDiceResults, Collection<Integer> dDiceResults);
}
