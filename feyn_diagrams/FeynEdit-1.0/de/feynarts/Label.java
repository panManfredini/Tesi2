package de.feynarts;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.ChangeEvent;
import java.awt.Graphics;
import java.awt.Color;


public class Label extends Entity {
  private final double LabelFontSize = 2;
  private final String texalign[][] =
    {{"bl", "l", "tl"},
     {"b",  "",  "t"},
     {"br", "r", "tr"}};

  /*
  * Primary state variables.
  */

  private double d, angle;
  private String align, labeltext;
  private Propagator parent;

  /*
  * Reference for inner classes.
  */

  private Label label = this;

  /*
  * Constructors.
  */

  protected Label() {}

  public Label(LinkedList.Node node, Diagram diagram, Propagator parent,
      String x, String y, String align, String labeltext, String tail) {
    initialize(node, diagram, parent,
      Double.parseDouble(x), Double.parseDouble(y),
      align, labeltext, tail);
  }

  public Label(LinkedList.Node node, Diagram diagram, Propagator parent,
      double x, double y, String align, String labeltext, String tail) {
    initialize(node, diagram, parent, x, y, align, labeltext, tail);
  }

  public Label(LinkedList.Node node, Diagram diagram, Propagator parent) {
    initialize(node, diagram, parent, "", "new", "\n");
    setDefaultPos();
  }

  protected void initialize(LinkedList.Node node, Diagram diagram,
      Propagator parent, double x, double y,
      String align, String labeltext, String tail) {
    initialize(node, diagram, parent, align, labeltext, tail);
    setPos(x - correctionX(align), y - correctionY(align));
  }

  protected void initialize(LinkedList.Node node, Diagram diagram,
      Propagator parent, String align, String labeltext, String tail) {
    super.initialize(node, diagram, tail);
    this.parent = parent;
    this.align = align;
    this.labeltext = labeltext;
    parent.setLabel(this);
  }

  /*
  * First level update function.
  */

  private void setDefaultPos() {
    UndoableEdit edit = new SetPosEdit(1.3, 0);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  /*
  * First level update function.
  */

  private double correctionX(String align) {
    int corr = 0;
    if( align.indexOf('l') != -1 ) corr = -1;
    if( align.indexOf('r') != -1 ) corr = 1;
    return .24*LabelFontSize*corr;
  }

  private double correctionY(String align) {
    int corr = 0;
    if( align.indexOf('b') != -1 ) corr = -1;
    if( align.indexOf('t') != -1 ) corr = 1;
    return .24*LabelFontSize*corr;
  }

  private void setPos(double x, double y) {
    double d = hypot(x - parent.getX(), y - parent.getY());
    double angle = Math.atan2(y - parent.getY(), x - parent.getX()) -
      parent.getCenterAngle();

    UndoableEdit edit = new SetPosEdit(d, angle);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class SetPosEdit extends AbstractUndoableEdit {
    private double d, angle, old_d, old_angle;

    public SetPosEdit(double d, double angle) {
      super.undo();
      this.d = d;
      this.angle = angle;
      old_d = label.d;
      old_angle = label.angle;
    }

    public void redo() {
      super.redo();
      label.d = d;
      label.angle = angle;
      stateChanged(new ChangeEvent(label));
    }

    public void undo() {
      super.undo();
      label.d = old_d;
      label.angle = old_angle;
      stateChanged(new ChangeEvent(label));
    }
  }

  /* 
  * Second level update function.
  */

  public void delete() {
    beginEdit();
    super.delete();
    parent.remove(this);
    endEdit();
  }

  /* 
  * Second level update function with single Edit.
  */

  public void draggedTo(int x, int y, CoordinateSystem c) {
    setPos(c.unPosX(x), c.unPosY(y));
  }

  public double getX() {
    return d*Math.cos(angle + parent.getCenterAngle()) + parent.getX();
  }

  public double getY() {
    return d*Math.sin(angle + parent.getCenterAngle()) + parent.getY();
  }

  public Color getColor() {
    return Color.green;
  }

  public String getName() {
    return "Label";
  }

  public String toString() {
    double x = getX();
    double y = getY();

    // determine alignment
    double scale = -1.3*Math.signum(d*Math.cos(angle));
    double centerAngle = parent.getCenterAngle();
    int c = (int)Math.round(scale*Math.cos(centerAngle));
    int s = (int)Math.round(scale*Math.sin(centerAngle));
    String align = texalign[c + 1][s + 1];

    return "\\FALabel(" + 
      toString(getX() + correctionX(align)) + "," + 
      toString(getY() + correctionY(align)) + ")[" +
      align + "]{" +
      labeltext + "}" +
      getTail();
  }

  public void paint(Graphics g, CoordinateSystem c, Entity selected) {
    paintBox(g, c.posX(getX()), c.posY(getY()), this == selected);
  }

  private static double hypot(double x, double y) {
    return Math.sqrt(x*x + y*y);
  }
}

