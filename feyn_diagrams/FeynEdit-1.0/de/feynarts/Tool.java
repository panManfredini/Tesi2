package de.feynarts;

import java.awt.Graphics;

import java.util.Iterator;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


abstract public class Tool implements MouseListener, MouseMotionListener, ChangeListener {
  private String name;
  private java.util.LinkedList changeListeners;

  public Tool(String name) {
    this.name = name;
    changeListeners = new java.util.LinkedList();
  }

  public void paint(Graphics g) {}

  public String getName() {
    return name;
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
  * MouseListener
  */

  public void mousePressed(MouseEvent e) {
    begin(e.getX(), e.getY());
  }

  public void begin(int x, int y) {}

  public void mouseReleased(MouseEvent e) {
    end();
  }

  public void end() {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {
    doo(e.getX(), e.getY());
  }

  public void doo(int x, int y) {}

  /*
  * MouseMotionListener
  */

  public void mouseDragged(MouseEvent e) {
    move(e.getX(), e.getY());
  }

  public void move(int x, int Y) {}

  public void mouseMoved(MouseEvent e) {}
}

