package de.feynarts;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;


public class Delete extends Tool {
  private DiagramPanel parent;

  public Delete(DiagramPanel parent) {
    super("Delete");
    this.parent = parent;
  }

  private Diagram getDiagram() {
    return parent.getDiagram();
  }

  public void doo(int x, int y) {
    try {
      parent.findEntity(x, y).delete();
    }
    catch( NullPointerException exception ) {}
  }
}

