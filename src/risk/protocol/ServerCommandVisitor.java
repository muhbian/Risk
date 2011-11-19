package risk.protocol;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import risk.common.Logger;
import risk.game.*;
import risk.game.server.*;
import risk.protocol.command.*;

public class ServerCommandVisitor implements CommandVisitor {
    private ClientHandler clientHandler;
    private CommandSender cmdSender;
    private GameView gameView;
    private GameController gameCtrl;
    private ArrayList<Color> colors;

    public ServerCommandVisitor(CommandSender cmdSender, GameView gameView, GameController gameCtrl, ClientHandler clientHandler) {
        this.gameView = gameView;
        this.gameCtrl = gameCtrl;
        this.cmdSender = cmdSender;
        this.clientHandler = clientHandler;
        
        colors = new ArrayList<Color>();
        colors.add(new Color(200, 0, 0)); // Red
        colors.add(new Color(0, 0, 200)); // Blue
        colors.add(new Color(0, 200, 0)); // Green
        colors.add(new Color(200, 200, 0)); // Yellow
        colors.add(new Color(200, 0, 200)); // Magenta
        colors.add(new Color(0, 200, 200)); // Cyan
    }

    @Override
    public void visit(HelloCmd cmd) {
        Logger.logdebug("Got HelloCmd!");

        Player newPlayer = new Player(cmd.getName(), colors.get(gameView.getPlayers().size()));
        clientHandler.setPlayer(newPlayer);
        
        // Get current players and send them to the new player
        Iterable<Player> players = gameView.getPlayers();
        for (Player p : players) {
            clientHandler.queuePlayerJoinedForSend(p);
        }

        // Create new player and send it to everyone
        
        gameCtrl.addPlayer(newPlayer);
        cmdSender.sendPlayerJoinedCmd(newPlayer);
        
        // Check if all the players are here
        if (gameView.getPlayers().size() == 2) {
            startGame();
        }
    }

    @Override
    public void visit(PlayerJoinedCmd cmd) {
        WrongCommand(cmd);
    }

    @Override
    public void visit(GameStartedCmd cmd) {
        WrongCommand(cmd);
    }

    @Override
    public void visit(NextRoundCmd cmd) {
        WrongCommand(cmd);
    }

    @Override
    public void visit(CountyInitCmd cmd) {
        WrongCommand(cmd);
    }

    @Override
    public void visit(GameEndedCmd cmd) {
        WrongCommand(cmd);
    }

    private void WrongCommand(Command cmd) {
        Logger.logwarn("Server shouldn't receive " + cmd.toString());
    }

    private void startGame() {
        // Send game started notification
        cmdSender.sendCmd(new GameStartedCmd(), null);
        
        // Randomly give countries to players
        ArrayList<Country> countries = new ArrayList<Country>(gameView.getCountries());
        Collection<Player> players = gameView.getPlayers();
        
        Random r = new Random();
        Iterator<Player> playerIterator = players.iterator();
        while (!countries.isEmpty()) {
            Logger.logdebug("Countries: " + countries.size());
            // Generate random number
            int rand = r.nextInt(countries.size());
            // Set random country to be owned by the next player
            Country c = countries.get(rand); 
            Logger.logdebug("Country name: " + c.getName());
            c.setOwner(playerIterator.next());
            c.setTroops(1);
            // Remove Country from the 'unowned' countries list
            cmdSender.sendCmd(new CountyInitCmd(c), null);
            countries.remove(rand);
            // If the iterator has ended, reset it to start.
            if (!playerIterator.hasNext()) {
                playerIterator = players.iterator();
            }
        }
        
        initNextRound();
    }
    
    private void initNextRound() {
        Logger.logdebug("Initing next round");
        gameCtrl.setRoundPlayers(gameView.getPlayers());
        gameCtrl.setRoundNumber(gameView.getRoundNumber() + 1);
        LinkedList<RoundPhase> phases = new LinkedList<RoundPhase>();
        switch (gameView.getRoundNumber()) {
        case 1:
            phases.add(RoundPhase.REINFORCEMENT);
            break;
        case 2:
            phases.add(RoundPhase.REGROUP);
            break;
        default:
            phases.add(RoundPhase.REINFORCEMENT);
            phases.add(RoundPhase.ATTACK);
            phases.add(RoundPhase.REGROUP);
            break;
        }
        gameCtrl.setRoundPhases(phases);
        cmdSender.sendCmd(new NextRoundCmd(gameView.getPlayers(), gameView.getRoundPhases()), null);
        gotoNextPlayer();
    }
    
    private void initNextPhase() {
        Logger.logdebug("Initing next phase");
        if (!gameCtrl.swicthToNextPhase()) {
            Logger.logdebug("After last phase");
            gotoNextPlayer();
            return;
        }
        RoundPhase phase = gameView.getRoundPhase();
        switch (phase) {
        case REINFORCEMENT:
            Player p = gameView.getCurrentPlayer();
            int reinforcement = getReinforcement(p);
            gameCtrl.setAvailableReinforcement(reinforcement);
            cmdSender.sendCmd(new NextPhaseCmd(reinforcement), null);
            break;
        case ATTACK:
            cmdSender.sendCmd(new NextPhaseCmd(0), null);
            break;
        case REGROUP:
            cmdSender.sendCmd(new NextPhaseCmd(0), null);
            break;
        default:
            Logger.logerror("INVALID round phase!!");
            break;
        }
    }
    
    private void gotoNextPlayer() {
        Logger.logdebug("Initing next player");
        if (!gameCtrl.switchToNextPlayer()) {
            Logger.logdebug("After last player");
            initNextRound();
            return;
        }
        gameCtrl.resetPhases();
        cmdSender.sendCmd(new NextPlayerCmd(), null);
        initNextPhase();
    }
    
    private int getReinforcement(Player p) {
        // TODO: get real number
        return 5;
    }

    @Override
    public void visit(DoAttackCmd cmd) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(PlaceReinforcementCmd cmd) {
        Logger.logdebug("Got PlaceReinforcement command to " + cmd.getCountry().getName() + " (" + cmd.getTroops() + ")");
        if (clientHandler.getPlayer() != gameView.getCurrentPlayer()) {
            // TODO: Error handling
            return;
        }
        
        if (cmd.getTroops() < 0 || cmd.getTroops() > gameView.getAvailableReinforcement()) {
            // TODO: Error handling
            return;
        }
        
        if (gameView.getCountry(cmd.getCountry().getName()).getOwner() != clientHandler.getPlayer()) {
            // TODO: Error handling
            return;
        }
        
        gameCtrl.setAvailableReinforcement(gameView.getAvailableReinforcement() - cmd.getTroops());
        Country country = gameView.getCountry(cmd.getCountry().getName());
        gameCtrl.addTroopsToCountry(country, cmd.getTroops());
        cmdSender.sendCmd(new PlaceReinforcementCmd(country, cmd.getTroops(), gameView.getCurrentPlayer()), null);
        
        if (gameView.getAvailableReinforcement() == 0) {
            initNextPhase();
        }
    }

    @Override
    public void visit(NextPhaseCmd cmd) {
        WrongCommand(cmd);
    }

    @Override
    public void visit(NextPlayerCmd cmd) {
        WrongCommand(cmd);
    }

    @Override
    public void visit(RegroupCmd cmd) {
        Country from = gameView.getCountry(cmd.getCountryPair().From.getName());
        Country to = gameView.getCountry(cmd.getCountryPair().To.getName());
        Logger.logdebug("Got regroup command " + from.getName() + " -> " + to.getName() + " : " + cmd.getTroops());
        if (from.getOwner() != to.getOwner() || from.getOwner() != clientHandler.getPlayer()) {
            // TODO: error handling
            Logger.logerror("Invalid regroup command");
            return;
        }
        CountryPair cp = new CountryPair(from, to);
        gameCtrl.regroup(cp, cmd.getTroops());
    }

    @Override
    public void visit(AttackStartCmd cmd) {
        Country from = gameView.getCountry(cmd.getCountryPair().From.getName());
        Country to = gameView.getCountry(cmd.getCountryPair().To.getName());
        
        Logger.logdebug("Got AttackStart command " + from.getName() + " -> " + to.getName());
        
        if (from.getOwner() != clientHandler.getPlayer() || to.getOwner() == clientHandler.getPlayer()) {
            // TODO: error handling
            Logger.logdebug("Wrong countries in attack");
            return ;
        }
        
        CountryPair cp = new CountryPair(from, to);
        gameCtrl.setAttack(new Attack(cp));
        cmdSender.sendCmd(new AttackStartCmd(cp), null);
    }

    @Override
    public void visit(AttackSetADiceCmd cmd) {
        int dice = cmd.getADice();
        Logger.logdebug("Got attackSetADiceCmd: " + dice);
        Country from = gameView.getAttack().getCountryPair().From;
        if (!(dice >= 1 && dice <= 3 && dice < from.getTroops())) {
            // TODO: error handling
            Logger.logdebug("Too many adice!");
            return;
        }
        gameCtrl.setAttackADice(dice);
        cmdSender.sendCmd(new AttackSetADiceCmd(dice), null);
    }

    @Override
    public void visit(AttackSetDDiceCmd cmd) {
        int dice = cmd.getDDice();
        Logger.logdebug("Got attackSetDDiceCmd: " + dice);
        Country to = gameView.getAttack().getCountryPair().To;
        if (!(dice >= 1 && dice <=2 && dice < to.getTroops())) {
            // TODO: error handling
            Logger.logdebug("Too many ddice!");
            return;
        }
        gameCtrl.setAttackDDice(dice);
        cmdSender.sendCmd(new AttackSetDDiceCmd(dice), null);
        doAttack();
    }

    private void doAttack() {
        Logger.logdebug("Doing attack");
        Attack attack = gameView.getAttack();
        Random r = new Random();
        
        LinkedList<Integer> aDice = new LinkedList<Integer>();
        for (int i = 0; i < attack.getAttackerDice(); i++) {
            aDice.add(r.nextInt(6) + 1);
        }
        
        LinkedList<Integer> dDice = new LinkedList<Integer>();
        for (int i = 0; i < attack.getDefenderDice(); i++) {
            dDice.add(r.nextInt(6) + 1);
        }
        
        Integer aLosses = new Integer(0);
        Integer dLosses = new Integer(0);
        attack.calcLosses(aDice, dDice, aLosses, dLosses);
        
        gameCtrl.accountAttackLosses(aLosses.intValue(), dLosses.intValue());
        cmdSender.sendCmd(new AttackRoundResultCmd(aDice, dDice), null);
    }

    @Override
    public void visit(AttackRoundResultCmd cmd) {
        WrongCommand(cmd);
    }

    @Override
    public void visit(AttackRetreatCmd cmd) {
        gameCtrl.clearAttack();
        cmdSender.sendCmd(new AttackRetreatCmd(), null);
    }
}
