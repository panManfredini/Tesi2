package de.feynarts;

import javax.swing.JMenu;
import javax.swing.JToolBar;


public class Separator implements Addable {
  static private Separator instance;

  static public Separator instance() {
    if( null == instance ) instance = new Separator();
    return instance;
  }

  protected Separator() {}

  public void addTo(JMenu menu) {
    menu.addSeparator();
  }

  public void addTo(JToolBar toolbar) {
    toolbar.addSeparator();
  }
}

