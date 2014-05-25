package de.feynarts;

import java.util.Iterator;
import java.util.NoSuchElementException;


public class LinkedList extends java.util.AbstractSequentialList implements Linkable {
  private Linkable first, last;
  private int size;

  /*
  * References for inner classes.
  */

  private LinkedList list = this;

  public LinkedList() {
    init();
  }

  public LinkedList(java.util.Collection collection) {
    init();
    java.util.Iterator i = collection.iterator();
    while( i.hasNext() ) {
      Object current = i.next();
      add(current);
    }
  }

  private void init() {
    first = last = this;
    size = 0;
  }

  public Linkable getHead() {
    return first;
  }

  public Linkable getTail() {
    return last;
  }

  /*
  * Linkable.
  */

  public void setNext(Linkable node) {
    first = node;
  }

  public Linkable getNext() {
    return getNext(true);
  }

  public Linkable getNext(boolean redirect) {
    if( redirect ) return first.redirectNext();
    return first;
  }

  public Linkable redirectNext() {
    return this;
  }

  public void setPrevious(Linkable node) {
    last = node;
  }

  public Linkable getPrevious() {
    return getPrevious(true);
  }

  public Linkable getPrevious(boolean redirect) {
    if( redirect ) return last.redirectPrevious();
    return last;
  }

  public Linkable redirectPrevious() {
    return this;
  }

  public Node insert(Object o) {
    Node node = new Node(this, o);
    return node;
  }

  public boolean isSignificant() {
    return true;
  }

  /*
  * AbstractList.
  */

  private void inc() {
    ++size;
  }

  private void dec() {
    --size;
  }

  public int size() {
    return size;
  }

  public boolean add(Object object) {
    last.insert(object);
    return true;
  }

  public java.util.ListIterator listIterator(int i) {
    return new ListIterator(i);
  }

  public Cursor cursor() {
    return new Cursor(this);
  }

  /*
  * Inner Classes.
  */

  public class ListIterator implements java.util.ListIterator {
    private Linkable next;
    private Node current;
    private int index;
    private boolean movedNext;

    public ListIterator(int i) {
      current = null;
      next = list;
      findNext();
      for( index = 0; index < i; ++index ) next();
    }

    public Object next() {
      try {
        current = (Node)next;
        Object result = current.get();
        findNext();
        return result;
      }
      catch( ClassCastException exception ) {
        throw(new java.util.NoSuchElementException());
      }
    }

    public Node getNextNode() {
      return (Node)next;
    }

    private void findNext() {
      ++index;
      next = next.getNext();
    }

    public boolean hasNext() {
      return (list != next);
    }

    public Object previous() {
      try {
        current = (Node)next.getPrevious();
        Object result = current.get();
        findPrevious();
        return result;
      }
      catch( ClassCastException exception ) {
        throw(new java.util.NoSuchElementException());
      }
    }

    public int nextIndex() {
      return index;
    }

    public Node getPreviousNode() {
      return (Node)getPrevious();
    }

    public Linkable getPrevious() {
      return next.getPrevious();
    }

    private void findPrevious() {
      --index;
      next = next.getPrevious();
    }

    public boolean hasPrevious() {
      return (list != getPrevious());
    }

    public int previousIndex() {
      return index - 1;
    }

    public void remove() {
      try {
        current.delete();
      } catch( NullPointerException exception ) {
        throw new java.lang.IllegalStateException();
      }
      current = null;
      if( movedNext ) --index;
    }

    public void set(Object o) {
      current.set(o);
    }

    public void add(Object o) {
      next.getPrevious().insert(o);
      ++index;
      current = null;
    }

    public void cursor() {
      new Cursor(next.getPrevious(false));
    }
  }

  public class Node extends AbstractLinkable {
    private Linkable next, previous;
    private Object content;
    private boolean deleted;

    public Node(Linkable previous, Object object) {
      super(previous);
      content = object;
      list.inc();
    }

    public boolean isSignificant() {
      return true;
    }

    public LinkedList.Node insert(Object o) {
      LinkedList.Node node = new LinkedList.Node(this, o);
      return node;
    }

    public Object get() {
      return content;
    }

    public void set(Object object) {
      content = object;
    }

    public Cursor cursor() {
      return new Cursor(this);
    }
  }

  public class Cursor extends AbstractLinkable {
    public Cursor(Linkable previous) {
      super(previous);
    }

    public boolean isSignificant() {
      return false;
    }

    public Node insert(Object o) {
      Node node = new Node(this, o);
      return node;
    }

    public Node add(Object o) {
      Node node = new Node(getPrevious(), o);
      return node;
    }

    public Cursor cursor() {
//      return new Cursor(this);
      return this;
    }
  }
}

