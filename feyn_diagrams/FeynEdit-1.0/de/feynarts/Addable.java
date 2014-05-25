package de.feynarts;

import javax.swing.JMenu;
import javax.swing.JToolBar;


public interface Addable {
  public void addTo(JMenu menu);
  public void addTo(JToolBar toolbar);
}

