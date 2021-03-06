package risk.protocol.command;

import risk.protocol.CommandVisitor;

public class GameStartedCmd extends Command {
    private static final long serialVersionUID = Command.serialVersionUID;

    @Override
    public void accept(CommandVisitor cv) {
        cv.visit(this);
    }

}
