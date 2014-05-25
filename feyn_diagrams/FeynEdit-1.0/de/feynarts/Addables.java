package de.feynarts;


public class Addables extends java.util.LinkedList {
  public void addTo(javax.swing.JMenu container) {
    java.util.Iterator i = iterator();
    while( i.hasNext() ) {
      Addable current = (Addable)i.next();
      current.addTo(container);
    }
  }

  public void addTo(javax.swing.JToolBar container) {
    java.util.Iterator i = iterator();
    while( i.hasNext() ) {
      Addable current = (Addable)i.next();
      current.addTo(container);
    }
  }
}

