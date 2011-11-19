package risk.view.client;

import java.awt.BorderLayout;
import java.awt.Dimension;

import risk.game.Controller;
import risk.game.Country;
import risk.game.CountryPair;
import risk.game.GameView;
import risk.game.Observer;
import risk.game.View;

import javax.swing.JPanel;
import javax.swing.JOptionPane;

public class ClientPanel extends JPanel implements Observer, View {
    private MapPanel map = new MapPanel(this);
    private FeedbackPanel fbp = new FeedbackPanel();
    private Controller controller;
    private AttackDialog ad;

    public void setController(Controller controller) {
        this.controller = controller;
        controller.setView(this);
        map.setController(controller);
        fbp.setController(controller);
    }

    public ClientPanel() {
        setLayout(new BorderLayout());
        add(map, BorderLayout.CENTER);
        add(fbp, BorderLayout.EAST);
    }

    public void refresh(GameView view) {
        map.refresh(view);
        fbp.refresh(view);
        if (view.getAttack() == null) {
            if (ad != null) {
                ad.dispose();
                ad = null;
            }
        } else {
            if (ad == null) {
                int viewerType=0;
                if(view.getMyPlayer().equals(view.getAttack().getCountryPair().From.getOwner())){
                    viewerType=1;
                }
                if(view.getMyPlayer().equals(view.getAttack().getCountryPair().To.getOwner())){
                    viewerType=2;
                }
                ad = new AttackDialog(view.getAttack(), controller, viewerType);
            }

            else
                ad.refresh(view.getAttack());
        }
    }
    public void resize(){
        Dimension mapSize, temp, temp2;
        mapSize=map.getPreferredSize();
        temp=fbp.getSize();
        temp2=new Dimension();
        temp2.width=Math.min((int)(this.getSize().getWidth()-mapSize.getWidth()),200);
        temp2.height=(int)temp.getHeight();
        fbp.setSize(temp2);
        fbp.setPreferredSize(temp2);
        repaint();
    }

    @Override
    public void showReinforcementDialog(Country to, int availableTroops) {
        ReinforcementDialog rd = new ReinforcementDialog(to, availableTroops,
                controller);
    }

    @Override
    public void showRegroupDialog(CountryPair cp) {
        RegroupDialog rd = new RegroupDialog(cp, controller);

    }

    @Override
    public void addMessage(String msg, boolean popup) {
        if (popup) {
            JOptionPane.showMessageDialog(this, msg, "Message!",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        fbp.addMessage(msg);
    }
}
