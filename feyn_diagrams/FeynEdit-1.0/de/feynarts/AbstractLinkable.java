package de.feynarts;


abstract class AbstractLinkable implements Linkable {
  private Linkable previous, next;
  boolean deleted;

  protected AbstractLinkable(Linkable previous) {
    this.previous = previous;
    this.next = previous.getNext(false);
    this.previous.setNext(this);
    this.next.setPrevious(this);
  }

  abstract public boolean isSignificant();

  public void setNext(Linkable node) {
    next = node;
  }

  public Linkable getNext() {
    return getNext(true);
  }

  public Linkable getNext(boolean redirect) {
    if( redirect ) return next.redirectNext();
    return next;
  }

  public Linkable redirectNext() {
    if( isSignificant() ) return this;
    return next.redirectNext();
  }

  public void setPrevious(Linkable node) {
    previous = node;
  }

  public Linkable getPrevious() {
    return previous;
  }

  public Linkable getPrevious(boolean redirect) {
    if( redirect ) return previous.redirectPrevious();
    return previous;
  }

  public Linkable redirectPrevious() {
    if( isSignificant() ) return this;
    return previous.redirectPrevious();
  }

  public Linkable delete() {
    setDeleted(true);
    getPrevious(false).setNext(getNext(false));
    getNext(false).setPrevious(getPrevious(false));
    return getPrevious(false);
  }

  public void undelete() {
    setDeleted(false);
    getPrevious(false).setNext(this);
    getNext(false).setPrevious(this);
  }

  public boolean isDeleted() {
    return deleted;
  }

  private void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}

