package de.feynarts;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.ChangeEvent;

import java.util.Iterator;
import java.awt.Graphics;
import java.awt.Color;
import java.util.Iterator;


public class Vertex extends Entity {
  /*
  * Primary state variables.
  */

  private double x, y;
  private IntProperty c;
  private java.util.LinkedList propagators;

  /*
  * Reference for inner classes.
  */

  private Vertex vertex = this;

  /*
  * Constructors.
  */

  public Vertex(LinkedList.Node node, Diagram diagram,
      String x, String y, String c, String tail) {
    initialize(node, diagram,
      Double.parseDouble(x), Double.parseDouble(y), Integer.parseInt(c), tail);
  }

  public Vertex(LinkedList.Node node, Diagram diagram,
      double x, double y) {
    initialize(node, diagram, x, y, 0, "\n");
  }

  private void initialize(LinkedList.Node node, Diagram diagram,
      double x, double y, int c, String tail) {
    super.initialize(node, diagram, tail);
    propagators = new java.util.LinkedList();

    this.x = x;
    this.y = y;
    this.c = new IntProperty("C");
    this.c.set(c);
  }

  /*
  * Second level update function.
  */

  public void delete() {
    beginEdit();
    super.delete();
    getDiagram().remove(this);
    Iterator i = propagatorsIterator();
    while( i.hasNext() ) {
      Propagator current = (Propagator) i.next();
      current.delete();
    }
    endEdit();
  }

  /*
  * First level update function.
  */

  private void setC(int c) {
    UndoableEdit edit = new SetCEdit(c);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class SetCEdit extends AbstractUndoableEdit {
    private int c, old_c;

    public SetCEdit(int c) {
      super.undo();
      this.c = c;
      old_c = vertex.c.get();
    }

    public void redo() {
      super.redo();
      vertex.c.set(this.c);
      stateChanged(new ChangeEvent(vertex));
    }

    public void undo() {
      super.undo();
      vertex.c.set(old_c);
      stateChanged(new ChangeEvent(vertex));
    }
  }

  /*
  * First level update function.
  * @unsafe
  */

  public void addPropagator(Propagator propagator) {
    UndoableEdit edit = new AddPropagatorEdit(propagator);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class AddPropagatorEdit extends AbstractUndoableEdit {
    private Propagator propagator;

    public AddPropagatorEdit(Propagator propagator) {
      super.undo();
      this.propagator = propagator;
    }

    public void redo() {
      super.redo();
      vertex.propagators.add(propagator);
      stateChanged(new ChangeEvent(vertex));
    }

    public void undo() {
      super.undo();
      vertex.propagators.remove(propagator);
      stateChanged(new ChangeEvent(vertex));
    }
  }

  /*
  * First level update function.
  * @unsafe
  */

  public void remove(Propagator propagator) {
    if( !isDeleted() ) {
      UndoableEdit edit = new RemovePropagatorEdit(propagator);
      edit.redo();
      undoableEditHappened(new UndoableEditEvent(this, edit));
    }
    check();
  }

  private class RemovePropagatorEdit extends AbstractUndoableEdit {
    private Propagator propagator;

    public RemovePropagatorEdit(Propagator propagator) {
      super.undo();
      this.propagator = propagator;
    }

    public void redo() {
      super.redo();
      vertex.propagators.remove(propagator);
      stateChanged(new ChangeEvent(vertex));
    }

    public void undo() {
      super.undo();
      vertex.propagators.add(propagator);
      stateChanged(new ChangeEvent(vertex));
    }
  }

  /*
  * First level update function.
  */

  public void moveTo(double x, double y) {
    UndoableEdit edit = new MoveToEdit(x, y);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class MoveToEdit extends AbstractUndoableEdit {
    private double x, y, old_x, old_y;

    public MoveToEdit(double x, double y) {
      super.undo();
      old_x = vertex.x;
      old_y = vertex.y;
      this.x = x;
      this.y = y;
    }

    public void redo() {
      super.redo();
      vertex.x = x;
      vertex.y = y;
      stateChanged(new ChangeEvent(vertex));
    }

    public void undo() {
      super.undo();
      vertex.x = old_x;
      vertex.y = old_y;
      stateChanged(new ChangeEvent(vertex));
    }
  }

  /*
  * Wrapper for moveTo.
  */

  public void draggedTo(int x, int y, CoordinateSystem c) {
    moveTo(c.unPosX(x), c.unPosY(y));
  }

  public void check() {
    if( 0 == propagators.size() && !isDeleted() ) delete();
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public int getC() {
    return c.get();
  }

  public Color getColor() {
    if( isVisible() ) return Color.red;
    return Color.black;
  }

  public boolean isVisible() {
    return (propagators.size() > 1);
  }

  public Propagator getPropagator(int index) {
    return (Propagator)propagators.get(index);
  }

  public int getNPropagators() {
    return propagators.size();
  }

  public Iterator propagatorsIterator() {
    return propagators.iterator();
  }

  public String getName() {
    if( isVisible() ) return "interior Vertex";
    return "exterior Vertex";
  }

  public String toString() {
    if( isVisible() )
      return "\\FAVert(" + 
        toString(x) + "," + 
        toString(y) + "){"+ 
        Integer.toString(getC()) + "}" +
        getTail();
    return "";
  }

  public boolean canMerge(Entity target) {
    return Vertex.class.isInstance(target);
  }

  public void merge(Entity target) {
    if( !canMerge(target) ) throw new CannotMergeException();

    Iterator i = propagatorsIterator();
    while( i.hasNext() ) {
      Propagator current = (Propagator)i.next();
      i.remove();
      current.replace(this, (Vertex)target);
    }

    delete();
  }

  public void paint(Graphics g, CoordinateSystem c, Entity selected) {
    if( propagators.size() > 0 )
      paintBox(g, c.posX(getX()), c.posY(getY()), this == selected);
  }
}

