package de.feynarts;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;


public class Tadpole extends Propagator {
  /*
  * Primary state variables.
  */

  private double x, y;

  /*
  * Secondary state variables.
  */

  private double centerX, centerY, radius, arrowAngle, centerAngle;
  private boolean valid = false;

  /*
  * Reference for inner classes;
  */

  private Tadpole tadpole = this;

  protected Tadpole() {}

  public Tadpole(LinkedList.Node node, Diagram diagram, Vertex from,
      String xmid, String ymid, String type, String arrow, String tail) {
    initialize(node, diagram, from,
      Double.parseDouble(xmid), Double.parseDouble(ymid),
      type, Integer.parseInt(arrow), tail);
  }

  public Tadpole(LinkedList.Node node, Diagram diagram, Vertex from,
      double xmid, double ymid, String type, int arrow, String tail) {
    initialize(node, diagram, from, xmid, ymid, type, arrow, tail);
  }

  public Tadpole(LinkedList.Node node, Diagram diagram, Vertex from,
      double xmid, double ymid) {
    initialize(node, diagram, from, xmid, ymid, "Straight", 0, "\n");
    super.newLabel();
  }

  private void initialize(LinkedList.Node node, Diagram diagram, Vertex from,
      double xmid, double ymid, String type, int arrow, String tail) {
    super.initialize(node, diagram, from, type, arrow, tail);
    x = xmid;
    y = ymid;
    from.addChangeListener(this);
  }

  public void invalidate() {
    valid = false;
  }

  public void validate() {
    update();
    valid = true;
  }

  private void update() {
    double fromX = getFrom().getX();
    double fromY = getFrom().getY();
    centerX = .5*(fromX + x);
    centerY = .5*(fromY + y);
    double dx = centerX - fromX;
    double dy = centerY - fromY;
    radius = hypot(dx, dy);
    centerAngle = Math.atan2(dy, dx);
    arrowAngle = Math.atan2(dx, -dy);
  }

  /*
  * Second level update function with single edit.
  */

  private void setPos(double x, double y) {
    setMPos(x, y);
  }

  /*
  * First level update function.
  */

  private void setMPos(double x, double y) {
    UndoableEdit edit = new SetMPosEdit(x, y);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class SetMPosEdit extends AbstractUndoableEdit {
    private double x, oldx, y, oldy;

    public SetMPosEdit(double x, double y) {
      super.undo();
      oldx = tadpole.x;
      this.x = x;
      oldy = tadpole.y;
      this.y = y;
    }

    public void redo() {
      super.redo();
      tadpole.x = x;
      tadpole.y = y;
      stateChanged(new ChangeEvent(tadpole));
    }

    public void undo() {
      super.undo();
      tadpole.x = oldx;
      tadpole.y = oldy;
      stateChanged(new ChangeEvent(tadpole));
    }
  }

  /*
  * Second level update funciton.
  */

  public void setFrom(Vertex from) {
    beginEdit();
    getFrom().removeChangeListener(this);
    super.setFrom(from);
    getFrom().addChangeListener(this);
    endEdit();
  }

  /*
  * Second level update funciton with single Edit.
  */

  public void draggedTo(int intx, int inty, CoordinateSystem c) {
    setPos(c.unPosX(intx), c.unPosY(inty));
  }

  public double getX() {
    if( !valid ) validate();
    return x;
  }

  public double getY() {
    if( !valid ) validate();
    return y;
  }

  public double getCenterAngle() {
    if( !valid ) validate();
    return centerAngle;
  }

  public String getName() {
    return "Tadpole";
  }

  public String toString() {
    return "\\FAProp(" +
      toString(getFrom().getX()) + "," +
      toString(getFrom().getY()) + ")(" +
      toString(getTo().getX()) + "," +
      toString(getTo().getY()) + ")(" +
      toString(x) + "," +
      toString(y) + "){" +
      getType() + "}{" +
      Integer.toString(getArrow()) + "}" +
      getTail();
  }

  public void paint(Graphics g, CoordinateSystem c, Entity selected) {
    update();

    try {
      ((Graphics2D)g).setStroke(wide);
    }
    catch( ClassCastException exception ) {}
    g.setColor(Color.black);

    int rx = Math.abs(c.scaleX(radius));
    int ry = Math.abs(c.scaleY(radius));
    g.drawOval(c.posX(centerX) - rx, c.posY(centerY) - ry, 2*rx, 2*ry);

    if( getArrow() == 1 )
      drawArrowHead(g, c, x, y, arrowAngle + Math.PI);
    else if( getArrow() == -1 )
      drawArrowHead(g, c, x, y, arrowAngle);

    try {
      double labelX = getLabel().getX();
      double labelY = getLabel().getY();

      try {
        ((Graphics2D)g).setStroke(narrow);
      }
      catch( ClassCastException exception ) {}

      g.setColor(Color.black);
      g.drawLine(c.posX(getX()), c.posY(getY()), c.posX(labelX), c.posY(labelY));
      getLabel().paint(g, c, selected);
    }
    catch( NullPointerException e ) {}

    paintBox(g, c.posX(getX()), c.posY(getY()), this == selected);
  }

  private static double hypot(double x, double y) {
    return Math.sqrt(x*x + y*y);
  }

  /*
  * State Changed
  */

  public void stateChanged(ChangeEvent e) {
    invalidate();
    super.stateChanged(e);
  }
}

