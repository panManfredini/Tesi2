package de.feynarts;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.ChangeEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import org.apache.regexp.RE;


public abstract class Propagator extends Entity {
  protected final static BasicStroke narrow = new BasicStroke();
  protected final static BasicStroke wide = new BasicStroke(3);

  private final RE falabel = new RE(Document.FALABEL);

  /*
  * Primary state variables.
  */

  private Vertex from;
  private String type;
  private int arrow;
  private Label label;

  /*
  * Reference for inner classes.
  */

  private Propagator propagator = this;

  protected Propagator() {}

  public Propagator(LinkedList.Node node, Diagram diagram,
      Vertex from, String type, int arrow, String tail) {
    initialize(node, diagram, from, type, arrow, tail);
  }

  protected void initialize(LinkedList.Node node, Diagram diagram,
      Vertex from, String type, int arrow, String tail) {
    super.initialize(node, diagram, tail);

    this.from = from;
    this.type = type;
    this.arrow = arrow;

    from.addPropagator(this);
  }

  /*
  * Second level update function with single edit.
  */

  public Label newLabel() {
    return new Label(getNode().insert(null), getDiagram(), this);
  }

  public void findLabel() {
    try {
      LinkedList.Node nextNode = (LinkedList.Node)getNode().getNext();
      String tex = (String)nextNode.get();
      if( falabel.match(tex) ) {
        nextNode.delete();
        getNode().insert(tex.substring(falabel.getParenEnd(0)));
        new Label(getNode().insert(null), getDiagram(), this,
          falabel.getParen(1), falabel.getParen(2), falabel.getParen(3),
          falabel.getParen(4), falabel.getParen(5));
        getNode().insert(tex.substring(0, falabel.getParenStart(0)));
      }
    }
    catch( ClassCastException exception ) {}
  }

  /*
  * Second level update function.
  */

  public void delete() {
    beginEdit();

    super.delete();
    getDiagram().remove(this);
    getFrom().remove(this);
    getTo().remove(this);
    try {
      label.delete();
    }
    catch( NullPointerException exception ) {}

    endEdit();
  }

  public void check() {
    if( getFrom().isDeleted() || getTo().isDeleted() ) delete();
  }

  public abstract double getCenterAngle();

  public void replace(Vertex old, Vertex nu) {
    if( getFrom() == old ) setFrom(nu);
  }

  /*
  * First level update function.
  */

  public void setFrom(Vertex from) {
    if( this.from != from ) {
      try {
        this.from.removeChangeListener(this);
        this.from.remove(this);
      }
      catch( NullPointerException exception ) {}

      UndoableEdit edit;
      edit = new SetFromEdit(from);
      edit.redo();
      undoableEditHappened(new UndoableEditEvent(this, edit));

      try{
        this.from.addPropagator(this);
        this.from.addChangeListener(this);
      } catch( NullPointerException exception ) {}
    }
  }

  /*
  * First level update function.
  */

  private class SetFromEdit extends AbstractUndoableEdit {
    private Vertex from, old_from;

    public SetFromEdit(Vertex from) {
      super.undo();
      this.from = from;
      old_from = propagator.from;
    }

    public void redo() {
      super.redo();
      propagator.from = this.from;
      stateChanged(new ChangeEvent(propagator));
    }

    public void undo() {
      super.undo();
      propagator.from = old_from;
      stateChanged(new ChangeEvent(propagator));
    }
  }

  public Vertex getFrom() {
    return from;
  }

  public Vertex getTo() {
    return from;
  }

  public String getType() {
    return type;
  }

  public int getArrow() {
    return arrow;
  }

  public Label getLabel() {
    return label;
  }

  /*
  * First level update funciton.
  */

  public void setLabel(Label label) {
    UndoableEdit edit = new SetLabelEdit(label);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class SetLabelEdit extends AbstractUndoableEdit {
    private Label label, old_label;

    public SetLabelEdit(Label label) {
      super.undo();
      this.label = label;
      old_label = propagator.label;
    }

    public void redo() {
      super.redo();
      propagator.label = this.label;
      stateChanged(new ChangeEvent(propagator));
    }

    public void undo() {
      super.undo();
      propagator.label = old_label;
      stateChanged(new ChangeEvent(propagator));
    }
  }

  /*
  * First level update funciton.
  */

  public void remove(Label label) {
    if( getLabel() == label ) setLabel(null);
  }

  public boolean hasLabel() {
    return (null != label);
  }

  public String getName() {
    return "Propagator";
  }

  /*
  * Second level update function with single edit.
  */

  public Label newLabel(double x, double y, String parameters) {
    return label = new Label(getNode().insert(null), getDiagram(),
      this, x, y, null, "labeltext", parameters);
  }

  public Color getColor() {
    return Color.blue;
  }

  public void drawArrowHead(Graphics g, CoordinateSystem c, double _x, double _y, double angle) {
    double ARROWHEAD_ANGLE = .1*Math.PI;
    int ARROWHEAD_LENGTH = 18;

    int dx1 = (int)Math.round(Math.cos(angle + ARROWHEAD_ANGLE)*ARROWHEAD_LENGTH);
    int dy1 = (int)Math.round(Math.sin(angle + ARROWHEAD_ANGLE)*ARROWHEAD_LENGTH) * (-c.orientation());
    int dx2 = (int)Math.round(Math.cos(angle - ARROWHEAD_ANGLE)*ARROWHEAD_LENGTH);
    int dy2 = (int)Math.round(Math.sin(angle - ARROWHEAD_ANGLE)*ARROWHEAD_LENGTH) * (-c.orientation());

    int x = c.posX(_x);
    int y = c.posY(_y);

    int[] xs = {x, x + dx1, x + dx2};
    int[] ys = {y, y + dy1, y + dy2};

    g.fillPolygon(xs, ys, 3);
  }
}

