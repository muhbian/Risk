package risk.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import risk.common.Logger;

public class Game implements GameView, GameController {
    /**
     * The map that represent the current state of the world.
     */
    private Map map = new Map();

    /**
     * A list of players.
     */
    private LinkedList<Player> players = new LinkedList<Player>();

    /**
     * A list of obersvers for the Observer design pattern.
     */
    private ArrayList<Observer> observers=new ArrayList<Observer>();

    /**
     * Indicates in which phase the current round is.
     * Eg. reinforcement phase, attack phase, regroup phase
     */
    private RoundPhase roundPhase;
    
    /**
     * The number of troops that can be assigned to countries at the beginning
     * of a new round as reinforcement.
     */
    private int availableReinforcement;

    /**
     * The player controlled by this client
     */
    private Player myPlayer;

    public void addPlayer(Player p) {
        players.add(p);

        modelChanged();
    }
    
    @Override
    public void setMyPlayer(Player p) {
        myPlayer = p;
        modelChanged();
    }
    

    public Collection<Player> getPlayers() {
        return players;
    }
    
    public Player getPlayer(String Name) {
        for (Player p : players) {
            if (p.getName().compareTo(Name) == 0) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Country getCountry(String countryName) {      
        return map.getCountry(countryName);
    }

    @Override
    public Collection<Continent> getContinents() {
        return map.getContinents();
    }
    
    @Override
    public Collection<Country> getCountries() {
        ArrayList<Country> countries = new ArrayList<Country>();
        Collection<Continent> continets = getContinents();
        for (Continent c : continets) {
            countries.addAll(c.getCountries());
        }
        return countries;
    }

    @Override
    public void initCountry(Country newCountry) {
        Logger.logdebug("Initing country: " + newCountry.getName());
        Country oldCountry = getCountry(newCountry.getName());
        Player newOwner = getPlayer(newCountry.getOwner().getName());
        oldCountry.setOwner(newOwner);
        oldCountry.setTroops(newCountry.getTroops());
        
        modelChanged();
    }
    
    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
        modelChanged();
    }

    private void modelChanged(){
        for(Observer o : observers){
            o.refresh(this);
        }
    }

    @Override
    public Color getCountryColor(String countryName) {
        Country c = getCountry(countryName);
        if (c != null && c.getOwner() != null) {
            return c.getOwner().getColor();
        }
        return null;
    }

    @Override
    public int getCountryTroops(String countryName) {
        Country c = getCountry(countryName);
        if (c != null) {
            return c.getTroops();
        }
        return 0;
    }

    @Override
    public void cancelCountrySelection(Country c) {
        map.cancelCountrySelection(c);
        modelChanged();
    }

    @Override
    public void selectCountry(Country c) {
        map.selectCountry(c);
        modelChanged();
    }

    @Override
    public boolean isCountrySelected(String name) {
        Country c = getCountry(name);
        return isCountrySelected(c);
    }

    @Override
    public boolean isCountryNeighbourSelected(String name) {
        Country c = getCountry(name);
        return getSelectedCountryNeighbour(c) != null;
    }
    
    @Override
    public boolean isCountrySelected(Country c) {
        return map.isCountrySelected(c);
    }

    @Override
    public Country getSelectedCountryNeighbour(Country c) {
        return map.getSelectedCountryNeighbour(c);
    }

    @Override
    public void cancelCountryNeighbourSelection(Country c) {
        map.cancelCountryNeighbourSelection(c);
    }

    @Override
    public RoundPhase getRoundPhase() {
        return roundPhase;
    }

    public void setRoundPhase(RoundPhase roundPhase) {
        this.roundPhase = roundPhase;
    }

    @Override
    public int getAvailableReinforcement() {
        return availableReinforcement;
    }

    @Override
    public void setAvailableReinforcement(int availableReinforcement) {
        this.availableReinforcement = availableReinforcement;
    }   
}
