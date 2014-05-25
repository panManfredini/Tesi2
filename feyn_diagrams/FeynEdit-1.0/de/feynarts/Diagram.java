package de.feynarts;

import java.util.Vector;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.apache.regexp.RE;


public class Diagram extends AbstractUndoableEditListener implements ChangeListener {
  protected static final double EPS =.001;
  private final RE faprop = new RE(Document.FAPROP);
  private final RE favert = new RE(Document.FAVERT);

  private java.util.LinkedList changeListeners;

  private Document document;

  /*
  * Primary state variables.
  */

  private Vector vertices;
  private Vector propagators;
  private LinkedList.Cursor spawnPointPropagator, spawnPointVertex;
  private LinkedList entities;

  /*
  * Reference for inner classes.
  */

  private Diagram diagram = this;

  public Diagram(Document document, String tex) {
    initialize(document, tex);
  }

  public Diagram(Document document) {
    initialize(document, "");
  }

  private void initialize(Document document, String tex) {
    changeListeners = new java.util.LinkedList();
    vertices = new Vector(5, 3);
    propagators = new Vector(10, 6);
    entities = new de.feynarts.LinkedList();

    this.document = document;
    addChangeListener(document);
    addUndoableEditListener(document);

    beginEdit();
    parse(tex);
    endEdit();
  }

  private void parse(String tex) {
    int index = 0;
    Linkable firstVertexNode = null, lastVertexNode = null;

    while( favert.match(tex, index) ) {
      String substring = tex.substring(index, favert.getParenStart(0));
      if( "" != substring ) entities.add(substring);

      if( null == firstVertexNode ) firstVertexNode = entities.getTail();
      entities.add(null);
      lastVertexNode = entities.getTail();
/*
      entities.add(null);
      lastVertexNode = entities.getTail();
      if( null == firstVertexNode ) firstVertexNode = lastVertexNode;
*/
      addVertex(new Vertex(
        (LinkedList.Node)lastVertexNode, //entities.getTail(),
        this, 
        favert.getParen(1), 
        favert.getParen(2), 
        favert.getParen(3),
        favert.getParen(4)
      ));

      index = favert.getParenEnd(0);
    }

    String substring = tex.substring(index);
    if( "" != substring ) entities.add(substring);

    if( null == firstVertexNode ) {
      spawnPointPropagator = entities.getTail().cursor();
      spawnPointPropagator.insert("");
      spawnPointVertex = entities.getTail().cursor();
    }
    else {
      spawnPointPropagator = firstVertexNode.cursor();
      spawnPointPropagator.add("");
      spawnPointVertex = lastVertexNode.cursor();
    }

    LinkedList.ListIterator i = (LinkedList.ListIterator)entities.iterator();
    Vertex from, to;

    try {
      while( true ) {
        try {
          String current = (String)i.next();
          i.remove();
          index = 0;
          while( faprop.match(current, index) ) {
            substring = tex.substring(index, faprop.getParenStart(0));
            if( "" != substring ) i.add(substring);

            from = findOrCreateVertex(faprop.getParen(1),
                                      faprop.getParen(2), EPS);
            if( null == faprop.getParen(6) ) {
              to = findOrCreateVertex(faprop.getParen(3),
                                      faprop.getParen(4), EPS);
              i.add(null);
              addPropagator(new SimplePropagator(
                i.getPreviousNode(),
                this, 
                from, to,
                faprop.getParen(5),
                faprop.getParen(7),
                faprop.getParen(8),
                faprop.getParen(9)
              ));
            }
            else {
              i.add(null);
              addPropagator(new Tadpole(
                i.getPreviousNode(), 
                this, 
                from, 
                faprop.getParen(5),
                faprop.getParen(6),
                faprop.getParen(7),
                faprop.getParen(8),
                faprop.getParen(9)
              ));
            }
            index = faprop.getParenEnd(0);
          }
          substring = current.substring(index);
          if( "" != substring ) i.add(substring);
        }
        catch( ClassCastException exception ) {}
      }
    }
    catch( java.util.NoSuchElementException exception ) {}

    Iterator j = propagatorsIterator();
    while( j.hasNext() ) {
      Propagator current = (Propagator)j.next();
      current.findLabel();
    }
  }

  /*
  * State Changed
  */

  public void addChangeListener(ChangeListener listener) {
    changeListeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    changeListeners.remove(listener);
  }

  public void stateChanged(ChangeEvent e) {
    Iterator i = changeListeners.iterator();
    while( i.hasNext() ) {
      ChangeListener current = (ChangeListener)i.next();
      current.stateChanged(e);
    }
  }

  /*
  * Propagators
  */

  private LinkedList.Node getPropagatorNode() {
    return spawnPointPropagator.add(null);
  }

  /*
  * First level update function.
  */

  private void addPropagator(Propagator propagator) {
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
      diagram.propagators.add(propagator);
      stateChanged(new ChangeEvent(diagram));
    }

    public void undo() {
      super.undo();
      diagram.propagators.remove(propagator);
      stateChanged(new ChangeEvent(diagram));
    }
  }

  /*
  * First level update function.
  */

  public void remove(Propagator propagator) {
    UndoableEdit edit = new RemovePropagatorEdit(propagator);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class RemovePropagatorEdit extends AbstractUndoableEdit {
    private Propagator propagator;

    public RemovePropagatorEdit(Propagator propagator) {
      super.undo();
      this.propagator = propagator;
    }

    public void redo() {
      super.redo();
      diagram.propagators.remove(propagator);
      stateChanged(new ChangeEvent(diagram));
    }

    public void undo() {
      super.undo();
      diagram.propagators.add(propagator);
      stateChanged(new ChangeEvent(diagram));
    }
  }

  /*
  * Vertices
  */

  private LinkedList.Node getVertexNode() {
    return spawnPointVertex.add(null);
  }

  /*
  * First level update function.
  */

  private void addVertex(Vertex vertex) {
    UndoableEdit edit = new AddVertexEdit(vertex);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class AddVertexEdit extends AbstractUndoableEdit {
    private Vertex vertex;

    public AddVertexEdit(Vertex vertex) {
      super.undo();
      this.vertex = vertex;
    }

    public void redo() {
      super.redo();
      diagram.vertices.add(vertex);
      stateChanged(new ChangeEvent(diagram));
    }

    public void undo() {
      super.undo();
      diagram.vertices.remove(vertex);
      stateChanged(new ChangeEvent(diagram));
    }
  }

  /*
  * First level update function.
  */

  public void remove(Vertex vertex) {
    UndoableEdit edit = new RemoveVertexEdit(vertex);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class RemoveVertexEdit extends AbstractUndoableEdit {
    private Vertex vertex;

    public RemoveVertexEdit(Vertex vertex) {
      super.undo();
      this.vertex = vertex;
    }

    public void redo() {
      super.redo();
      diagram.vertices.remove(vertex);
      stateChanged(new ChangeEvent(diagram));
    }

    public void undo() {
      super.undo();
      diagram.vertices.add(vertex);
      stateChanged(new ChangeEvent(diagram));
    }
  }

  /*
  * Miscellaneous
  */

  public String toString() {
    String result = "";
    Iterator i = entities.iterator();
    while( i.hasNext() ) result += i.next().toString();
    return result;
  }

  public boolean isEmpty() {
    return vertices.isEmpty();
  }

  public Vertex findVertex(double x, double y, double epsilon) {
    Iterator i = verticesIterator();
    return (Vertex)findEntityIn(i, x, y, epsilon);
  }

  public Vertex findOrCreateVertex(String x, String y, double epsilon) {
    return findOrCreateVertex(Double.parseDouble(x),
      Double.parseDouble(y), epsilon);
  }

  public Vertex findOrCreateVertex(double x, double y, double epsilon) {
    Vertex result = findVertex(x, y, epsilon);
    if( null == result ) result = createVertex(x, y);
    return result;
  }

  /*
  * Second level update function.
  */

  public Vertex createVertex(double x, double y) {
    beginEdit();
    Vertex result = new Vertex(getVertexNode(), this, x, y);
    addVertex(result);
    endEdit();
    return result;
  }

  public int getNVertices() {
    return vertices.size();
  }

  public Iterator verticesIterator() {
    return vertices.iterator();
  }

  /*
  * Second level update function.
  */

  public SimplePropagator newSimplePropagator(Vertex from, Vertex to,
      double height, String type, int arrow) {
    beginEdit();
    SimplePropagator result = new SimplePropagator(getPropagatorNode(),
      this, from, to, height, type, arrow, "\n");
    addPropagator(result);
    endEdit();
    return result;
  }

  /*
  * Second level update function.
  */

  public SimplePropagator newSimplePropagator(Vertex from, Vertex to) {
    beginEdit();
    SimplePropagator result = new SimplePropagator(getPropagatorNode(),
      this, from, to);
    addPropagator(result);
    endEdit();
    return result;
  }

  /*
  * Second level update function.
  */

  public Tadpole newTadpole(Vertex from,
      double xcenter, double ycenter, String type, int arrow) {
    beginEdit();
    Tadpole result = new Tadpole(getPropagatorNode(),
      this, from, xcenter, ycenter, type, arrow, "\n");
    addPropagator(result);
    endEdit();
    return result;
  }

  /*
  * Second level update function.
  */

  public Tadpole newTadpole(Vertex from, double xcenter, double ycenter) {
    beginEdit();
    Tadpole result = new Tadpole(getPropagatorNode(),
      this, from, xcenter, ycenter);
    addPropagator(result);
    endEdit();
    return result;
  }

  public Iterator propagatorsIterator() {
    return propagators.iterator();
  }

  public Iterator labelsIterator() {
    return new Diagram.LabelsIterator(this);
  }

  private Entity findEntityIn(Iterator i, double x, double y, double epsilon) {
    Entity current;
    while( i.hasNext() ) {
      current = (Entity)i.next();
      if( Math.abs(current.getX() - x) <= epsilon &&
          Math.abs(current.getY() - y) <= epsilon ) return current;
    }
    return null;
  }

  private class LabelsIterator implements Iterator {
    private Iterator propagatorsIterator;
    private Object next;

    public LabelsIterator(Diagram top) {
      propagatorsIterator = top.propagatorsIterator();
      findNext();
    }

    private void findNext() {
      next = null;
      while( null == next && propagatorsIterator.hasNext() ) {
        Propagator current = (Propagator)propagatorsIterator.next();
        next = current.getLabel();
      }
    }

    public boolean hasNext() {
      return (null != next);
    }

    public Object next() {
      if( hasNext() ) {
        Object result = next;
        findNext();
        return result;
      }
      else throw(new NoSuchElementException());
    }

    public void remove() {}
  }
}

