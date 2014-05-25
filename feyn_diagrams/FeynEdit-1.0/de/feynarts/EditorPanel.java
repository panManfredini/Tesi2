package de.feynarts;

import java.util.Iterator;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.undo.UndoManager;
import javax.swing.undo.CompoundEdit;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.UndoableEditEvent;
import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;


public class EditorPanel extends JPanel implements ExportImportReceiver, UndoableEditListener, ChangeListener {
  private JMenu menu;
  private UndoManager undoManager;
  private CompoundEdit currentUndoContainer;
  private int editLevel = 0;
  private java.util.LinkedList undoableEditListeners;
  private java.util.Collection changeListeners;

  private DiagramPanel diagramPanel;
  private Document document;
  // private PropertiesPanel propertiesPanel;
  private Addables actions;
  private AbstractUpdateAction previousAction, nextAction;
  private AbstractUpdateAction undoAction, redoAction;
  private AbstractUpdateAction gridAction;

  private EditorPanel panel = this;

  public EditorPanel() {
    super(new BorderLayout());

    undoableEditListeners = new java.util.LinkedList();
    currentUndoContainer = undoManager = new UndoManager();
    changeListeners = new java.util.LinkedList();

    this.document = new Document("");
    document.addUndoableEditListener(this);
    //document.addChangeListener(this);

    diagramPanel = new DiagramPanel(this, document);
    //propertiesPanel = new PropertiesPanel();
    //diagramPanel.addSelectionListener(propertiesPanel);
    /* JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new Workspace(diagramPanel), propertiesPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setResizeWeight(1);
    add(splitPane);*/
    add(diagramPanel);

    initActions();

    JToolBar toolBar = new JToolBar();

    actions.addTo(toolBar);

    menu = new JMenu("Diagram");
    menu.setMnemonic(KeyEvent.VK_D);

    actions.addTo(menu);

    diagramPanel.append(menu);

    add(toolBar, BorderLayout.NORTH);
  }

  private void initActions() {
    previousAction = new PreviousAction();
    nextAction = new NextAction();
    undoAction = new UndoAction();
    redoAction = new RedoAction();
    gridAction = new GridAction();

    actions = new Addables();
    Separator separator = Separator.instance();

    actions.add(new ClearAction());
    actions.add(separator);

    actions.add(new DeleteSelected());
    actions.add(separator);

    actions.add(previousAction);
    actions.add(nextAction);
    actions.add(separator);

    actions.add(undoAction);
    actions.add(redoAction);
    actions.add(separator);

    actions.add(gridAction);
    actions.add(separator);
  }

  public void setText(String text) {
    setDocument(new Document(text));
  }

  public String getText() {
    return document.toString();
  }

  private void setDocument(Document document) {
    this.document.removeUndoableEditListener(this);
    //this.document.removeChangeListener(this);
    this.document = document;
    document.addUndoableEditListener(this);
    //document.addChangeListener(this);
    undoManager.discardAllEdits();
    diagramPanel.setDocument(document);
    previousAction.update();
    nextAction.update();
    undoAction.update();
    redoAction.update();
  }

  public Document getDocument() {
    return document;
  }

  /*
  * Undo/Redo
  */

  public void addUndoableEditListener(UndoableEditListener listener) {
    undoableEditListeners.add(listener);
  }

  public void removeUndoableEditListener(UndoableEditListener listener) {
    undoableEditListeners.remove(listener);
  }

  private void notifyUndoableEditListeners(UndoableEditEvent e) {
    Iterator i = undoableEditListeners.iterator();
    while( i.hasNext() ) {
      UndoableEditListener current = (UndoableEditListener)i.next();
      current.undoableEditHappened(e);
    }
  }

  public void undoableEditHappened(UndoableEditEvent e) {
    currentUndoContainer.addEdit(e.getEdit());
    if( editLevel == 0 ) notifyUndoableEditListeners(e);
  }

  public void beginEdit() {
    beginEdit(true);
  }

  public void beginEdit(boolean isSignificant) {
    if( editLevel == 0 ) currentUndoContainer = new CompoundEdit();
    ++editLevel;
  }

  public void endEdit() {
    --editLevel;
    if( editLevel == 0 ) {
      currentUndoContainer.end();
      undoManager.addEdit(currentUndoContainer);
      currentUndoContainer = undoManager;
      notifyUndoableEditListeners(new UndoableEditEvent(this, currentUndoContainer));
    }
  }

  private UndoManager getUndoManager() {
    return undoManager;
  }

  public JMenu getMenu() {
    return menu;
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

  /*
  * Inner Classes.
  */ 

  public class ClearAction extends AbstractAction {
    public ClearAction() {
      super("Clear", Editor.newImageIcon("New"));

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_DELETE,
          ActionEvent.CTRL_MASK
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_L)
      );
    }

    public void actionPerformed(ActionEvent e) {
      setText("");
    }
  }

  public class NextAction extends AbstractUpdateAction implements ChangeListener {
    public NextAction() {
      super("Next Diagram", Editor.newImageIcon("Forward"));
      diagramPanel.addChangeListener(this);

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_N,
          0
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_N)
      );
    }

    public void actionPerformed(ActionEvent e) {
      beginEdit();

      document.nextDiagram();
      diagramPanel.repaint();
      this.update();
      previousAction.update();

      endEdit();
    }

    public void update() {
      try {
        setEnabled(getDocument().hasNextDiagram());
      }
      catch( Exception exception ) {
        setEnabled(false);
      }
    }
  }

  public class PreviousAction extends AbstractUpdateAction {
    public PreviousAction() {
      super("Previous Diagram", Editor.newImageIcon("Back"));
      diagramPanel.addChangeListener(this);

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_P,
          0
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_P)
      );
    }

    public void actionPerformed(ActionEvent e) {
      beginEdit();

      document.previousDiagram();
      diagramPanel.repaint();
      this.update();
      nextAction.update();

      endEdit();
    }

    public void update() {
      try {
        setEnabled(getDocument().hasPreviousDiagram());
      }
      catch( Exception exception ) {
        setEnabled(false);
      }
    }
  }

  public class UndoAction extends AbstractUpdateAction implements UndoableEditListener {
    public UndoAction() {
      super("Undo", Editor.newImageIcon("Undo"));
      addUndoableEditListener(this);

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_Z,
          ActionEvent.CTRL_MASK
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_U)
      );
    }

    public void actionPerformed(ActionEvent e) {
      getUndoManager().undo();
      this.update();
      redoAction.update();
    }

    public void update() {
      try {
        setEnabled(getUndoManager().canUndo());
      }
      catch( Exception exception ) {
        setEnabled(false);
      }
    }

    public void undoableEditHappened(UndoableEditEvent e) {
      update();
    }

    public void beginEdit(boolean isSignificant) {}

    public void beginEdit() {}

    public void endEdit() {}
  }

  public class RedoAction extends AbstractUpdateAction implements UndoableEditListener {
    public RedoAction() {
      super("Redo", Editor.newImageIcon("Redo"));
      addUndoableEditListener(this);

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_Z,
          ActionEvent.SHIFT_MASK + ActionEvent.CTRL_MASK
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_R)
      );
    }

    public void actionPerformed(ActionEvent e) {
      getUndoManager().redo();
      this.update();
      undoAction.update();
    }

    public void update() {
      try {
        setEnabled(getUndoManager().canRedo());
      }
      catch( Exception exception ) {
        setEnabled(false);
      }
    }

    public void undoableEditHappened(UndoableEditEvent e) {
      update();
    }

    public void beginEdit(boolean isSignificant) {}

    public void beginEdit() {}

    public void endEdit() {}
  }

  public class GridAction extends AbstractUpdateAction implements ChangeListener {
    private ImageIcon gridLock, gridFree;

    public GridAction() {
      super("");
      diagramPanel.addChangeListener(this);

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(
          KeyEvent.VK_G,
          0
        )
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_G)
      );

      gridLock = Editor.newImageIcon("GridLock");
      gridFree = Editor.newImageIcon("GridFree");
      this.update();
    }

    public void actionPerformed(ActionEvent e) {
      diagramPanel.getCoordinateSystem().toggleGrid();
      this.update();
    }

    public void update() {
      boolean grid = diagramPanel.getCoordinateSystem().getGrid();
      String name = grid ? "Grid Snap Off" : "Grid Snap On";
      putValue(SMALL_ICON, grid ? gridFree : gridLock);
      putValue(NAME, name);
      putValue(SHORT_DESCRIPTION, name);
    }
  }

  public class DeleteSelected extends AbstractUpdateAction {
    public DeleteSelected() {
      super ("Delete", Editor.newImageIcon("Delete"));
      diagramPanel.addChangeListener(this);

      putValue(
        ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)
      );
      putValue(
        MNEMONIC_KEY,
        new Integer(KeyEvent.VK_D)
      );
    }

    public void actionPerformed(ActionEvent e) {
      diagramPanel.getSelected().delete();
      this.update();
    }

    public void update() {
      try{ 
        setEnabled(null != diagramPanel.getSelected());
      }
      catch( RuntimeException exception ) {
        setEnabled(false);
      }
    }
  }

  public abstract class AbstractUpdateAction extends AbstractAction implements ChangeListener {
    public AbstractUpdateAction(String string, Icon icon) {
      super(string, icon);
      update();
    }

    public AbstractUpdateAction(String string) {
      super(string);
      update();
    }

    public abstract void update();

    public void stateChanged(ChangeEvent e) {
      update();
    }
  }
}

