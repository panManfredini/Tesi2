package de.feynarts;

import javax.swing.Icon;


abstract public class AbstractAction extends javax.swing.AbstractAction implements Addable {
  public AbstractAction(String name, Icon icon) {
    super(name, icon);
    putValue(SHORT_DESCRIPTION, name);
  }

  public AbstractAction(String name) {
    super(name);
    putValue(SHORT_DESCRIPTION, name);
  }

  public void addTo(javax.swing.JMenu menu) {
    menu.add(this);
  }

  public void addTo(javax.swing.JToolBar toolbar) {
    toolbar.add(this);
  }
}

