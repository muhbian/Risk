package risk.game;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class Attack {
    private CountryPair countryPair;
    private int attackerDice;
    private int defenderDice;
    private Collection<Integer> aDiceResults;
    private Collection<Integer> dDiceResults;
    
    public Attack(CountryPair countryPair) {
        super();
        this.countryPair = countryPair;
    }

    public void resetDice() {
        attackerDice = -1;
        defenderDice = -1;
    }
    
    public int getAttackerDice() {
        return attackerDice;
    }

    public void setAttackerDice(int attackerDice) {
        this.attackerDice = attackerDice;
    }

    public int getDefenderDice() {
        return defenderDice;
    }

    public void setDefenderDice(int defenderDice) {
        this.defenderDice = defenderDice;
    }

    public CountryPair getCountryPair() {
        return countryPair;
    }
    
    public int[] calcLosses() {
        int[] losses = new int[2];
        losses[0] = 0;
        losses[1] = 0;
        
        if (aDiceResults==null || dDiceResults==null)
            return losses;
        
        Iterator<Integer> aIterator = aDiceResults.iterator();
        Iterator<Integer> dIterator = dDiceResults.iterator();
        
        while (aIterator.hasNext() && dIterator.hasNext()) {
            if (aIterator.next() > dIterator.next()) {
                losses[1]++;
            } else {
                losses[0]++;
            }
        }
        
        return losses;
    }

    public Collection<Integer> getaDiceResults() {
        return aDiceResults;
    }

    public void setaDiceResults(Collection<Integer> aDiceResults) {
        this.aDiceResults = aDiceResults;
    }

    public Collection<Integer> getdDiceResults() {
        return dDiceResults;
    }

    public void setdDiceResults(Collection<Integer> dDiceResults) {
        this.dDiceResults = dDiceResults;
    }
}
