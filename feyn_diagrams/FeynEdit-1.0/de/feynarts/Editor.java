package de.feynarts;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;


public class Editor extends JFrame {
  private static final String imagePostfix = "24";

  private EditorPanel top;
  private JSplitPane splitPane;
  private TeXPanel bottom;

  public Editor() {
    super("FeynEdit");
    getContentPane().setLayout(new BorderLayout());

    top = new EditorPanel();
    bottom = new TeXPanel(top);

    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
    splitPane.setOneTouchExpandable(true);
    splitPane.setResizeWeight(1);
    getContentPane().add(splitPane);

    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("Editor");
    menu.setMnemonic(KeyEvent.VK_E);
    menu.add(new Quit());
    menuBar.add(menu);
    menuBar.add(top.getMenu());
    menuBar.add(bottom.getMenu());
    getContentPane().add(menuBar, BorderLayout.NORTH);

    pack();
  }

  public static void main(String args[]) { 
    Editor editor = new Editor();
    editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    editor.setVisible(true);
  }

  protected static ImageIcon newImageIcon(String path) {
    return newImageIcon(path, "");
  }

  protected static ImageIcon newImageIcon(String path, String description) {
    path = getImagePrefix() + path + getImagePostfix();
    java.net.URL imgURL = Editor.class.getResource(path);
    if( imgURL != null ) return new ImageIcon(imgURL, description);
    else {
      System.err.println("Couldn't find file " + path);
      return null;
    }
  }

  private static String getImagePostfix() {
    return imagePostfix + ".gif";
  }

  private static String getImagePrefix() {
    return "icons/";
  }

  private class Quit extends AbstractAction {
    public Quit() {
      super("Quit");

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_Q,
          ActionEvent.CTRL_MASK
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_Q)
      );
    }

    public void actionPerformed(ActionEvent e) {
      System.exit(1);
    }
  }
}

