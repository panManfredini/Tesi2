package de.feynarts;

import java.util.Vector;
import java.math.*;
import java.util.Iterator;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.UndoableEditEvent;
import org.apache.regexp.RE;


public class Document extends AbstractUndoableEditListener implements ChangeListener {
  public static final String RESTART = "(?:";
  public static final String REEND = ")";

  public static final String FLOAT = RESTART + "-?[:digit:]+(?:\\.[:digit:]*)?" + REEND;
  public static final String COORDINATES = RESTART + "\\((" + FLOAT + "),(" + FLOAT + ")\\)" + REEND;
  public static final String HEIGHT = RESTART + "\\((" + FLOAT + "),(" + FLOAT + ")?\\)" + REEND;
  public static final String TYPE = RESTART + "\\{([:alnum:]*)\\}" + REEND;
  public static final String ARROW = RESTART + "\\{(-?[:digit:])\\}" + REEND;
  public static final String TEXOPT = RESTART + "\\[([^)]*)\\]" + REEND;
  public static final String TEXARG = RESTART + "\\{([^}]*)\\}" + REEND;
  public static final String ENDLINE = RESTART + "([:blank:]*\\n?)" + REEND;

  public static final String FADIAGRAM = RESTART + "\\\\FADiagram" + TEXARG + REEND;
  public static final String FAPROP = RESTART + "\\\\FAProp" + COORDINATES + COORDINATES + HEIGHT + TYPE + ARROW + ENDLINE + REEND;
  public static final String FAVERT = RESTART + "\\\\FAVert" + COORDINATES + TEXARG + ENDLINE + REEND;
  public static final String FALABEL = RESTART + "\\\\FALabel" + COORDINATES + TEXOPT + "?" + TEXARG + ENDLINE + REEND;

  private static final double EPS = 1e-7;

  private final RE fadiagram = new RE(FADIAGRAM);

  private java.util.Collection changeListeners;

  /*
  * Primary state variables.
  */

  private de.feynarts.LinkedList diagrams;
  private LinkedList.Node currentDiagram;

  /*
  * Reference for inner classes.
  */

  private Document document = this;

  public Document(String tex) {
    initialize(tex);
  }

  public Document() {
    initialize("");
  }

  private void initialize(String tex) {
    changeListeners = new java.util.LinkedList();
    diagrams = new de.feynarts.LinkedList();
    parse(tex);
  }

  private void parse(String tex) {    
    splitDiagrams(tex);
    firstDiagram();
  }

  public void splitDiagrams(String tex) {
    int index = 0;
    while( fadiagram.match(tex, index + 1) ) {
      diagrams.add(new Diagram(this, tex.substring(index, fadiagram.getParenStart(0))));
      index = fadiagram.getParenStart(0);
    }
    diagrams.add(new Diagram(this, tex.substring(index)));
  }

  private void firstDiagram() {
    currentDiagram = (LinkedList.Node)diagrams.getHead();
    while( getDiagram().isEmpty() && hasNextDiagram() )
      nextDiagram();
  }

  public void nextDiagram() {
    beginEdit();
    if( !hasNextDiagram() ) throw new java.util.NoSuchElementException();
    setCurrentDiagram(getNextDiagram());
    endEdit();
  }

  private LinkedList.Node getNextDiagram() {
    try {
      return (LinkedList.Node)currentDiagram.getNext();
    }
    catch( ClassCastException exception ) {
      return null;
    }
    catch( NullPointerException exception ) {
      return null;
    }
  }

  public boolean hasNextDiagram() {
    return null != getNextDiagram();
  }

  public void previousDiagram() {
    if( !hasPreviousDiagram() )
      throw new java.util.NoSuchElementException();

    try {
      setCurrentDiagram(getPreviousDiagram());
    }
    catch( ClassCastException exception ) {}

    /* This should not be needed. 
    try {
      currentDiagram.set(new Diagram(this, (String)currentDiagram.get()));
    }
    catch (ClassCastException exception) {}
    */
  }

  private LinkedList.Node getPreviousDiagram() {
    try {
      return (LinkedList.Node)currentDiagram.getPrevious();
    }
    catch( ClassCastException exception ) {
      return null;
    }
    catch( NullPointerException exception ) {
      return null;
    }
  }

  public boolean hasPreviousDiagram() {
    return null != getPreviousDiagram();
  }

  /*
  * First level update function.
  */

  private void setCurrentDiagram(LinkedList.Node currentDiagram) {
    UndoableEdit edit = new SetCurrentDiagramEdit(currentDiagram);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));
  }

  private class SetCurrentDiagramEdit extends AbstractUndoableEdit {
    private LinkedList.Node currentDiagram, old_currentDiagram;

    public SetCurrentDiagramEdit(LinkedList.Node currentDiagram) {
      super.undo();
      this.currentDiagram = currentDiagram;
      old_currentDiagram = document.currentDiagram;
    }

    public void redo() {
      super.redo();
      document.currentDiagram = this.currentDiagram;
      stateChanged(new ChangeEvent(document));
    }

    public void undo() {
      super.undo();
      document.currentDiagram = old_currentDiagram;
      stateChanged(new ChangeEvent(document));
    }

    public boolean isSignificant() {
      return false;
    }
  }

  public Diagram getDiagram() {
    try {
      return (Diagram)currentDiagram.get();
    }
    catch( ClassCastException exception ) {
      return null;
    }
    catch( NullPointerException exception ) {
      return null;
    }
  }

  public void remove(Entity entity) {}

  public String toString() {
    String result = "";
    Iterator i = diagrams.iterator();
    while( i.hasNext() ) result += i.next().toString();
    return result;
  }

  public Iterator diagramsIterator() {
    return diagrams.iterator();
  }


  /*
  * ChangeListener
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
}

