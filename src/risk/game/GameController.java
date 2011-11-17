package risk.game;

import java.util.Collection;

public interface GameController {
    // Will be filled with methods that control the game, give orders, take
    // actions
    // doAttack(Country from, Country to);
    // doRegroup(Country from, Country to, int howmanyunits);
    // connectToServer()
    // disconnect()

    public void addPlayer(Player p);
    public void setMyPlayer(Player p);
    public void setCurrentPlayer(Player p);
    public void initCountry(Country newCountry);

    public void cancelCountrySelection(Country c);
    public void selectCountry(Country c);
    public void cancelCountryNeighbourSelection(Country c);
    
    public void setRoundNumber(int roundNumber);
    public void setRoundPhases(Collection<RoundPhase> roundPhases);
    public boolean swicthToNextPhase();
    public void setAvailableReinforcement(int availableReinforcement);
    public void addTroopsToCountry(Country country, int troops);
}
