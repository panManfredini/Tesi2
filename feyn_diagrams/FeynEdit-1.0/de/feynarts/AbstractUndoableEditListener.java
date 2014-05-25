package de.feynarts;

import javax.swing.event.UndoableEditEvent;
import java.util.Iterator;


abstract public class AbstractUndoableEditListener implements UndoableEditListener {
  private java.util.LinkedList undoableEditListeners;

  public AbstractUndoableEditListener() {
    undoableEditListeners = new java.util.LinkedList();
  }

  public void addUndoableEditListener(UndoableEditListener listener) {
    undoableEditListeners.add(listener);
  }

  public void removeUndoableEditListener(UndoableEditListener listener) {
    undoableEditListeners.remove(listener);
  }

  public void undoableEditHappened(UndoableEditEvent e) {
    Iterator i = undoableEditListeners.iterator();
    while( i.hasNext() ) {
      UndoableEditListener current = (UndoableEditListener)i.next();
      current.undoableEditHappened(e);
    }
  }

  public void beginEdit() {
    beginEdit(true);
  }

  public void beginEdit(boolean isSignificant) {
    Iterator i = undoableEditListeners.iterator();
    while( i.hasNext() ) {
      UndoableEditListener current = (UndoableEditListener)i.next();
      current.beginEdit(isSignificant);
    }
  }

  public void endEdit() {
    Iterator i = undoableEditListeners.iterator();
    while( i.hasNext() ) {
      UndoableEditListener current = (UndoableEditListener)i.next();
      current.endEdit();
    }
  }
}

