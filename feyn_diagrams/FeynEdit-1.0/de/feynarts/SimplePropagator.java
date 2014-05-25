package de.feynarts;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.ChangeEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;


public class SimplePropagator extends Propagator {
  /*
  * Primary state variables.
  */

  private double height;
  private int signHeight;
  private Vertex to;

  /*
  * Secondary state variables.
  */

  private double x, y;
  private double radius, centerX, centerY, arrowAngle, centerAngle;
  private int startAngle, angle;

  private boolean valid = false;

  /*
  * Reference for inner classes;
  */

  private SimplePropagator simplePropagator = this;

  protected SimplePropagator() {}

  public SimplePropagator(LinkedList.Node node, Diagram diagram,
      Vertex from, Vertex to,
      String height, String type, String arrow, String tail) {
    initialize(node, diagram, from, to, Double.parseDouble(height),
      type, Integer.parseInt(arrow), tail);
  }

  public SimplePropagator(LinkedList.Node node, Diagram diagram,
      Vertex from, Vertex to,
      double height, String type, int arrow, String tail) {
    initialize(node, diagram, from, to, height, type, arrow, tail);
  }

  public SimplePropagator(LinkedList.Node node, Diagram diagram,
      Vertex from, Vertex to) {
    initialize(node, diagram, from, to, 0, "Straight", 0, "\n");
    super.newLabel();
  }

  private void initialize(LinkedList.Node node, Diagram diagram,
      Vertex from, Vertex to,
      double height, String type, int arrow, String tail) {
    super.initialize(node, diagram, from, type, arrow, tail);
    this.height = height;

    if( height == 0 ) signHeight = 0;
    else if( height > 0 ) signHeight = 1;
    else signHeight = -1;

    setTo(to);
    from.addChangeListener(this);
    to.addChangeListener(this);
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
    double toX = getTo().getX();
    double toY = getTo().getY();

    x = .5*(fromX + toX);
    y = .5*(fromY + toY);

    double dx = toX - fromX;
    double dy = toY - fromY;
    arrowAngle = Math.atan2(dy, dx);
    centerAngle = arrowAngle - .5*Math.PI;

    double absh = Math.abs(height);
    if( absh > .001 ) {
      if( height < 0 ) centerAngle += Math.PI;
      double cosCenterAngle = Math.cos(centerAngle);
      double sinCenterAngle = Math.sin(centerAngle);

      double halfLength = .5*hypot(dx, dy);
      double halfAngle = 2*Math.atan(absh);
      radius = halfLength/Math.sin(halfAngle);
      double rad = Math.sqrt(radius*radius - halfLength*halfLength);
      if( absh > 1 ) rad = -rad;
      centerX = x - rad*cosCenterAngle;
      centerY = y - rad*sinCenterAngle;

      x += absh*halfLength*cosCenterAngle;
      y += absh*halfLength*sinCenterAngle;

      startAngle = (int)Math.round(Math.toDegrees(centerAngle - halfAngle));
      angle = (int)Math.round(Math.toDegrees(2*halfAngle));
    }
  }

  public double getHeight() {
    return height;
  }

  public int getSignHeight() {
    return signHeight;
  }

  /*
  * First level update function.
  */

  private void setHeight(double height) {
    double newheight = Math.abs(height) < .05 ? 0 : height;
    UndoableEdit edit = new SetHeightEdit(newheight);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class SetHeightEdit extends AbstractUndoableEdit {
    private double height, old_height;
    private int signHeight, old_signHeight;

    public SetHeightEdit(double height) {
      super.undo();
      this.height = height;
      old_height = simplePropagator.height;
      if( height == 0 ) signHeight = 0;
      else if( height > 0 ) signHeight = 1;
      else signHeight = -1;
      old_signHeight = simplePropagator.signHeight;
    }

    public void redo() {
      super.redo();
      simplePropagator.height = this.height;
      simplePropagator.signHeight = this.signHeight;
      stateChanged(new ChangeEvent(simplePropagator));
    }

    public void undo() {
      super.undo();
      simplePropagator.height = old_height;
      simplePropagator.signHeight = old_signHeight;
      stateChanged(new ChangeEvent(simplePropagator));
    }
  }

  public Vertex getTo() {
    return to;
  }

  public void replace(Vertex old, Vertex nu) {
    if( getTo() == old ) setTo(nu);
    super.replace(old, nu);
  }

  /*
  * First level update funciton.
  */

  public void setTo(Vertex to) {
    if( this.to != to ) {
      try {
        this.to.removeChangeListener(this);
        this.to.remove(this);
      }
      catch( NullPointerException exception ) {}

      UndoableEdit edit = new SetToEdit(to);
      edit.redo();
      undoableEditHappened(new UndoableEditEvent(this, edit));

      try{
        this.to.addPropagator(this);
        this.to.addChangeListener(this);
      } catch( NullPointerException exception ) {}
    }

    if( getTo() == getFrom() ) delete();
  }

  private class SetToEdit extends AbstractUndoableEdit {
    private Vertex to, old_to;

    public SetToEdit(Vertex to) {
      super.undo();
      this.to = to;
      old_to = simplePropagator.to;
    }

    public void redo() {
      super.redo();
      simplePropagator.to = this.to;
      stateChanged(new ChangeEvent(simplePropagator));
    }

    public void undo() {
      super.undo();
      simplePropagator.to = old_to;
      stateChanged(new ChangeEvent(simplePropagator));
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

    if( getTo() == getFrom() ) delete();
  }

  /*
  * Second level update funciton with single Edit.
  */

  public void draggedTo(int intx, int inty, CoordinateSystem c) {
    double x = c.unPosX(intx);
    double y = c.unPosY(inty);

    double fromX = getFrom().getX();
    double fromY = getFrom().getY();
    double toX = getTo().getX();
    double toY = getTo().getY();

    double xm = .5*(toX + fromX);
    double ym = .5*(toY + fromY);
    double dx = toX - fromX;
    double dy = toY - fromY;

        // adjust dragged position to lie on the perpendicular bisector
    if( dx == 0 ) y = ym;
    else if( dy == 0 ) x = xm;
    else {
      double h = dy/dx;
      double d = h + dx/dy;
      x = (xm/h + ym - y + h*x)/d;
      y = (xm + ym*h - x + y/h)/d;
    }

    double newheight = hypot(xm - x, ym - y);
    if( newheight < .3 ) newheight = 0;
    else {
      newheight = 2*newheight/hypot(dx, dy);
            // cross product tells which side of the prop we are on
      double cross = dy*(x - fromX) - dx*(y - fromY);
      if( newheight*cross < 0 ) newheight = -newheight;
    }

    setHeight(newheight);
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

  public String toString() {
    return "\\FAProp(" + 
      toString(getFrom().getX()) + "," + 
      toString(getFrom().getY()) + ")(" + 
      toString(getTo().getX()) + "," + 
      toString(getTo().getY()) + ")(" + 
      toString(getHeight()) + ",){" + 
      getType() + "}{" + 
      Integer.toString(getArrow()) + "}" + 
      getTail();
  }

  public void paint(Graphics g, CoordinateSystem c, Entity selected) {
    double fromX = getFrom().getX();
    double fromY = getFrom().getY();
    double toX = getTo().getX();
    double toY = getTo().getY();

    update();

    try {
      ((Graphics2D)g).setStroke(wide);
    }
    catch( ClassCastException exception ) {}
    g.setColor(Color.black);

    if( Math.abs(height) < 0.001 )
      g.drawLine(c.posX(fromX), c.posY(fromY), c.posX(toX), c.posY(toY));
    else
      g.drawArc(
        c.posX(centerX - radius),
        c.posY(centerY + radius),
        Math.abs(c.scaleX(2*radius)),
        Math.abs(c.scaleY(2*radius)),
        startAngle, angle
      );

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
      g.drawLine(c.posX(x), c.posY(y), c.posX(labelX), c.posY(labelY));
      getLabel().paint(g, c, selected);
    }
    catch( NullPointerException e ) {}

    paintBox(g, c.posX(x), c.posY(y), this == selected);
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

