package de.feynarts;


public interface UndoableEditListener extends javax.swing.event.UndoableEditListener {
  public void beginEdit();
  public void beginEdit(boolean isSignificant);
  public void endEdit();
}

