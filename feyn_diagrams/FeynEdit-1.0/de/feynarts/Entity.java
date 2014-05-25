package de.feynarts;

import java.util.Iterator;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.Graphics;
import java.awt.Color;
import org.apache.regexp.RE;


public abstract class Entity implements UndoableEditListener, ChangeListener {
  public final static String FLOAT =
    "(-?[:digit:]*)(\\.?([:digit:]{0,3}[1-9])?)[:digit:]*";

  private static final int boxWidth = 8;
  private static final int bigBoxWidth = 12;
  private boolean deleted;

  /*
  * Primary state variables.
  */

  private boolean selected = false;
  private LinkedList.Node node;
  private Diagram diagram;
  private String tail;
  private java.util.LinkedList changeListeners;

  /*
  * Reference for inner classes.
  */

  private Entity entity = this;

  /*
  * Constructors.
  */

  protected Entity() {}

  public Entity(LinkedList.Node node, Diagram diagram, String tail) {
    initialize(node, diagram, tail);
  }

  protected void initialize(LinkedList.Node node, Diagram diagram, String tail) {
    deleted = false;

    changeListeners = new java.util.LinkedList();
    this.node = node;
    node.set(this);
    this.diagram = diagram;
    this.tail = tail;

    addChangeListener(diagram);
  }

  /*
  * First level update function.
  */

  public void delete() {
    UndoableEdit edit = new DeleteEdit();
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class DeleteEdit extends AbstractUndoableEdit {
    private LinkedList.Node node;

    public DeleteEdit() {
      super.undo();
      this.node = entity.node;
    }

    public void redo() {
      super.redo();
      node.delete();
      deleted = true;
      stateChanged(new ChangeEvent(entity));
    }

    public void undo() {
      super.undo();
      node.undelete();
      deleted = false;
      stateChanged(new ChangeEvent(entity));
    }
  }

  public boolean isDeleted() {
    return deleted;
  }

  /*
  * Second level update function with single edit.
  */

  public void select() {
    setSelected(true);
  }

  /*
  * Second level update function with single edit.
  */

  public void unselect() {
    setSelected(false);
  }

  /*
  * First level update function.
  */

  private void setSelected(boolean selected) {
    if( this.selected != selected ) {
      UndoableEdit edit = new ToggleSelectedEdit();
      edit.redo();
      undoableEditHappened(new UndoableEditEvent(this, edit));
    }
  }

  private class ToggleSelectedEdit extends AbstractUndoableEdit {
    public ToggleSelectedEdit() {
      super.undo();
    }

    public void redo() {
      super.redo();
      entity.selected = !entity.selected;
      stateChanged(new ChangeEvent(entity));
    }

    public void undo() {
      super.undo();
      entity.selected = !entity.selected;
      stateChanged(new ChangeEvent(entity));
    }
  }

  public abstract void draggedTo(int x, int y, CoordinateSystem c);

  public LinkedList.Node getNode() {
    return node;
  }

  public Diagram getDiagram() {
    return diagram;
  }

  protected String getTail() {
    return tail;
  }

  public abstract double getX();

  public abstract double getY();

  abstract public String getName();

  public abstract void paint(Graphics g, CoordinateSystem c, Entity selected);

  public Color getColor() {
    return Color.black;
  }

  protected int getBoxWidth() {
    return boxWidth;
  }

  protected void paintBox(Graphics g, int x, int y, boolean selected) {
    g.setColor(getColor());
    int w = selected ? bigBoxWidth : boxWidth;
    g.fillRect(x - w/2, y - w/2, w, w);
  }

  public boolean isAt(int x, int y, CoordinateSystem c) {
    return (Math.abs(c.posX(getX()) - x) <= getBoxWidth()/2) &&
           (Math.abs(c.posY(getY()) - y) <= getBoxWidth()/2);
  }

  public void merge(Entity target) {
    throw new CannotMergeException();
  }

  public boolean canMerge(Entity target) {
    return false;
  }

  /*
  * Undo/Redo
  */

  public void undoableEditHappened(UndoableEditEvent e) {
    diagram.undoableEditHappened(e);
  }

  public void beginEdit() {
    beginEdit(true);
  }

  public void beginEdit(boolean isSignificant) {
    diagram.beginEdit(isSignificant);
  }

  public void endEdit() {
    diagram.endEdit();
  }

  /*
  * State Changed
  */

  public void addChangeListener(ChangeListener listener) {
    UndoableEdit edit = new AddChangeListenerEdit(listener);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class AddChangeListenerEdit extends AbstractUndoableEdit {
    private ChangeListener listener;

    public AddChangeListenerEdit(ChangeListener listener) {
      super.undo();
      this.listener = listener;
    }

    public void redo() {
      super.redo();
      entity.changeListeners.add(listener);
      stateChanged(new ChangeEvent(entity));
    }

    public void undo() {
      super.undo();
      entity.changeListeners.remove(listener);
      stateChanged(new ChangeEvent(entity));
    }
  }

  public void removeChangeListener(ChangeListener listener) {
    UndoableEdit edit = new RemoveChangeListenerEdit(listener);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class RemoveChangeListenerEdit extends AbstractUndoableEdit {
    private ChangeListener listener;

    public RemoveChangeListenerEdit(ChangeListener listener) {
      super.undo();
      this.listener = listener;
    }

    public void redo() {
      super.redo();
      entity.changeListeners.remove(listener);
      stateChanged(new ChangeEvent(entity));
    }

    public void undo() {
      super.undo();
      entity.changeListeners.add(listener);
      stateChanged(new ChangeEvent(entity));
    }
  }

  public void stateChanged(ChangeEvent e) {
    Iterator i = changeListeners.iterator();
    while( i.hasNext() ) {
      ChangeListener current = (ChangeListener)i.next();
      current.stateChanged(e);
    }
  }

  /*
  * Static functions.
  */

  public final static String toString(double x) {
    return nice(Double.toString(x));
  }

  public final static String nice(String string) {
    RE refloat = new RE(FLOAT);
    return refloat.match(string) ?
      refloat.getParen(1) + refloat.getParen(2) : string;
  }
}

