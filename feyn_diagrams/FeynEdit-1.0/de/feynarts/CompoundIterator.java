package de.feynarts;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


public class CompoundIterator implements Iterator {
  private Collection iterators;
  private Iterator superIterator;
  private Iterator current;

  public CompoundIterator(Collection iterators) {
    this.iterators = iterators;
    superIterator = iterators.iterator();
    current = (Iterator)superIterator.next();
  }

  public Object next() {
    if( !current.hasNext() )
      current = (Iterator)superIterator.next();
    return current.next();
  }

  public boolean hasNext() {
    if( current.hasNext() ) return true;
    while( superIterator.hasNext() && !current.hasNext() )
      current = (Iterator)superIterator.next();
    return current.hasNext();
  }

  public void remove() {
    current.remove();
  }
}

