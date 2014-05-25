package de.feynarts;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JMenu;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.SwingUtilities;

import java.awt.Dimension;
import java.awt.Color;
import java.lang.Math;


public class DiagramPanel extends JPanel implements UndoableEditListener, ChangeListener, MouseListener, MouseMotionListener {
  private final static BasicStroke narrow = new BasicStroke();
  private final static BasicStroke wide = new BasicStroke(3);

  private java.util.Collection changeListeners;

  private Document document;
  private Diagram currentDiagram;
  private EditorPanel editorPanel;
  private CoordinateSystem coordinateSystem;
  private ToolSelector left, middle, right;
  private Tool current;

  private boolean stateChange = false;

  /*
  * Primary state variables.  Subjected to undo monitoring.
  */

  private Entity selected;

  /*
  * Reference for inner classes.
  */

  private DiagramPanel diagramPanel = this;

  public DiagramPanel(EditorPanel editorPanel, Document document) {
    changeListeners = new java.util.LinkedList();
    this.editorPanel = editorPanel;
    this.document = document;
    document.addChangeListener(this);
    initialize();
  }

  private void initialize() {
    //selectionListeners = new java.util.LinkedList();

    Dimension dim = new Dimension(400, 400);
    setBackground(Color.white);
    setPreferredSize(dim);
    coordinateSystem = new DiagramPanel.CoordinateSystem(this);
    addMouseListener(this);
    addMouseMotionListener(this);

    java.util.AbstractList tools = new java.util.Vector(4);
    tools.add(new SelectAndMove(this));
    tools.add(new DrawPropagator(this));
    tools.add(new DrawTadpole(this));
    tools.add(new Delete(this));

    left = new ToolSelector("Left Button", tools, (Tool)tools.get(0));
    middle = new ToolSelector("Middle Button", tools, (Tool)tools.get(2));
    right = new ToolSelector("Right Button", tools, (Tool)tools.get(1));
  }

  public CoordinateSystem getCoordinateSystem() {
    return coordinateSystem;
  }

  public Diagram getDiagram() {
    try {
      return document.getDiagram();
    }
    catch( NullPointerException exception ) {
      return null;
    }
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    g.setColor(Color.black);
    for( int x = 0; x <= 20; ++x ) {		// draw grid
      for( int y = 0; y <= 20; ++y ) {
        int ix = coordinateSystem.posX(x);
        int iy = coordinateSystem.posY(y);
        if( x % 5 == 0 && y % 5 == 0 ) {
          g.drawLine(ix - 2, iy, ix + 2, iy);
          g.drawLine(ix, iy - 2, ix, iy + 2);
        }
        else g.drawRect(ix, iy, 0, 0);
      }
    }

    Iterator iterator = getDiagram().propagatorsIterator();
    while( iterator.hasNext() ) {
      Propagator current = (Propagator)iterator.next();
      current.paint(g, coordinateSystem, selected);
    }

    iterator = getDiagram().verticesIterator();
    while( iterator.hasNext() ) {
      Vertex current = (Vertex)iterator.next();
      current.paint(g, coordinateSystem, selected);
    }

    if( null != current ) current.paint(g);
  }

  private static double hypot(double x, double y) {
    return Math.sqrt(x*x + y*y);
  }

  public void setDocument(Document document) {
    this.document.removeChangeListener(this);
    this.document = document;
    this.document.addChangeListener(this);
    stateChanged(new ChangeEvent(this));
  }

  /*
  * First level update function.
  */

  public void setSelected(Entity selected) {
    UndoableEdit edit = new SetSelectedEdit(selected);
    edit.redo();
    undoableEditHappened(new UndoableEditEvent(this, edit));

    //notifySelection(selected);
  }

  public class SetSelectedEdit extends AbstractUndoableEdit {
    private Entity selected, old_selected;

    public SetSelectedEdit(Entity selected) {
      super.undo();
      this.selected = selected;
      old_selected = diagramPanel.selected;
    }

    public void redo() {
      super.redo();
      diagramPanel.selected = this.selected;
      stateChanged(new ChangeEvent(diagramPanel));
    }

    public void undo() {
      super.undo();
      diagramPanel.selected = old_selected;
      stateChanged(new ChangeEvent(diagramPanel));
    }

    public boolean isSignificant() {
      return false;
    }
  }

  public Entity getSelected() {
    if( !selected.isDeleted() ) return selected;
    return null;
  }

  public Entity select(int x, int y) {
    Entity result = findEntity(x, y); 
    setSelected(result);
    return result;
  }

  public void append(javax.swing.JMenu menu) {
    menu.add(left.getMenu());
    menu.add(middle.getMenu());
    menu.add(right.getMenu());
  }

  public Vertex newVertex(int x, int y) {
    return getDiagram().createVertex(getCoordinateSystem().unPosX(x),
                                     getCoordinateSystem().unPosY(y));
  }

  public SimplePropagator newSimplePropagator(Vertex from, Vertex to) {
    return getDiagram().newSimplePropagator(from, to);
  }

  public Tadpole newTadpole(Vertex from, int x, int y) {
    double xcenter = getCoordinateSystem().unPosX(x);
    double ycenter = getCoordinateSystem().unPosY(y);
    xcenter = xcenter + .5*(from.getX() - xcenter);
    ycenter = ycenter + .5*(from.getY() - ycenter);
    return getDiagram().newTadpole(from, xcenter, ycenter);
  }

  public Vertex findOrCreateVertex(int x, int y) {
    Vertex result = findVertex(x, y);
    if( null == result ) result = newVertex(x, y);
    return result;
  }

  public Vertex findVertex(int x, int y) {
    Iterator i = getDiagram().verticesIterator();
    while( i.hasNext() ) {
      Vertex current = (Vertex)i.next();
      if( current.isAt(x, y, coordinateSystem) ) return current;
    }
    return null;
  }

  /*
  * Finding Entities
  */

  public Entity findNextEntity(int x, int y) {
    Iterator i = entityIterator();
    while( i.hasNext() ) {
      Entity current = (Entity)i.next();
      if( current.isAt(x, y, coordinateSystem) &&
          current != null &&
          current != selected ) return current;
    }
    return null;
  }

  public Entity findEntity(int x, int y) {
    Iterator i = entityIterator();
    while( i.hasNext() ) {
      Entity current = (Entity)i.next();
      if( current.isAt(x, y, coordinateSystem) &&
          current != null ) return current;
    }
    return null;
  }

  public Iterator entityIterator() {
    java.util.LinkedList iterators = new java.util.LinkedList();
    iterators.add(getDiagram().verticesIterator());
    iterators.add(getDiagram().propagatorsIterator());
    iterators.add(getDiagram().labelsIterator());
    return new CompoundIterator(iterators);
  }

  /*
  * Undo/Redo
  */

  public void undoableEditHappened(UndoableEditEvent e) {
    editorPanel.undoableEditHappened(e);
  }

  public void beginEdit() {
    beginEdit(true);
  }

  public void beginEdit(boolean isSignificant) {
    editorPanel.beginEdit(isSignificant);
  }

  public void endEdit() {
    editorPanel.endEdit();
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
    if( !stateChange ) {
      stateChange = true;

      if( currentDiagram != getDiagram() ) {
        currentDiagram = getDiagram();
//        setSelected(null);
      }

      Iterator i = changeListeners.iterator();
      while( i.hasNext() ) {
        ChangeListener current = (ChangeListener)i.next();
        current.stateChanged(e);
      }
      repaint();

      stateChange = false;
    }
  }

  /*
  * MouseListener
  */

  public void mousePressed(MouseEvent e) {
    if( current == null ) {
      if( SwingUtilities.isLeftMouseButton(e) ) {
        current = left.getTool();
        current.addChangeListener(this);
      }

      if( SwingUtilities.isMiddleMouseButton(e) ) {
        current = middle.getTool();
        current.addChangeListener(this);
      }

      if( SwingUtilities.isRightMouseButton(e) ) {
        current = right.getTool();
        current.addChangeListener(this);
      }

      getDiagram().beginEdit();
      current.mousePressed(e);
    }
  }

  public void mouseReleased(MouseEvent e) {
    if( current != null ) {
      current.mouseReleased(e);
      current.removeChangeListener(this);
      current = null;
      getDiagram().endEdit();
    }
  }

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {
    if( current == null ) {
      if( SwingUtilities.isLeftMouseButton(e) ) {
        getDiagram().beginEdit();
        left.getTool().mouseClicked(e);
        getDiagram().endEdit();
      }

      if( SwingUtilities.isMiddleMouseButton(e) ) {
        getDiagram().beginEdit();
        middle.getTool().mouseClicked(e);
        getDiagram().endEdit();
      }

      if( SwingUtilities.isRightMouseButton(e) ) {
        getDiagram().beginEdit();
        right.getTool().mouseClicked(e);
        getDiagram().endEdit();
      }
    }
  }

  /*
  * MouseMotionListener
  */

  public void mouseDragged(MouseEvent e) {
    try {
      current.mouseDragged(e);
    }
    catch( NullPointerException exception ) {}
  }

  public void mouseMoved(MouseEvent e) {}

  public class CoordinateSystem extends SquareCoordinateSystem {
    DiagramPanel parent;

    public CoordinateSystem(DiagramPanel parent) {
      this.parent = parent;
    }

    public Dimension getSize() {
      return parent.getSize();
    }
  }

  private class ToolSelector {
    private String name;
    private Tool selected;
    private java.util.LinkedList selectors;

    public ToolSelector(String name, java.util.AbstractList tools) { 
      initialize(name, tools, (Tool)tools.get(0));
    }

    public ToolSelector(String name, java.util.AbstractList tools,
        Tool selected) {
      initialize(name, tools, selected);
    }

    public void initialize(String name, java.util.AbstractList tools,
        Tool selected) {
      this.name = name;
      this.selected = selected;

      selectors = new java.util.LinkedList();

      java.util.Iterator i = tools.iterator();
      while( i.hasNext() ) {
        Tool current = (Tool)i.next();
        selectors.add(new Select(current));
      }
    }

    public Tool getTool() {
      return selected;
    }

    public javax.swing.JMenu getMenu() {
      javax.swing.JMenu menu = new javax.swing.JMenu(name);
      javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();

      java.util.Iterator i = selectors.iterator();
      while( i.hasNext() ) {
        Select current = (Select)i.next();
        javax.swing.JRadioButton button = new javax.swing.JRadioButton(current);
        button.setSelected(current.isSelected());

        group.add(button);
        menu.add(button);
      }
      return menu;
    }

    private class Select extends AbstractAction {
      private Tool tool;

      public Select(Tool tool) {
        super(tool.getName());
        this.tool = tool;
      }

      public void actionPerformed(ActionEvent e) {
        selected = tool;
      }

      public boolean isSelected() {
        return tool == selected;
      }
    }
  }
}

