package de.feynarts;


public interface Linkable {
  //public Linkable insert();

  /*
  * @unsafe
  */

  public void setPrevious(Linkable previous);

  public Linkable getPrevious();

  public Linkable getPrevious(boolean redirect);

  public Linkable redirectPrevious();

  public LinkedList.Node insert(Object o);

  public LinkedList.Cursor cursor();

  /*
  * @unsafe
  */

  public void setNext(Linkable next);

  public Linkable getNext();

  public Linkable getNext(boolean redirect);

  public Linkable redirectNext();

  public boolean isSignificant();    
}

