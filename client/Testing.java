import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
class Testing
{
  public void buildGUI()
  {
    JPanel gamePanel = new JPanel();
    gamePanel.setBackground(Color.RED);
    gamePanel.setPreferredSize(new Dimension(400,300));
 
    JPanel holdingPanel = new JPanel(new GridBagLayout());///A
    holdingPanel.add(gamePanel,new GridBagConstraints());///A
    //JPanel holdingPanel = new JPanel();///B
    //holdingPanel.add(gamePanel);///B
 
    JFrame f = new JFrame();
    f.getContentPane().add(holdingPanel);
    f.setSize(800,600);
    f.setLocationRelativeTo(null);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
  }
  public static void main(String[] args)
  {
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        new Testing().buildGUI();
      }
    });
  }
}