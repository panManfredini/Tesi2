package de.feynarts;


public class DrawPropagator extends SelectAndMove {
  private int xstart, ystart;

  public DrawPropagator(DiagramPanel parent) {
    super(parent, "Draw Propagator");
  }

  public void begin(int x, int y) {
    xstart = x;
    ystart = y;
    setActive(null);
  }

  public void move(int x, int y) {
    if( getActive() == null ) {
      Vertex from = getParent().findOrCreateVertex(xstart, ystart);
      Vertex to = getParent().newVertex(x, y);
      getParent().newSimplePropagator(from, to);
      setActive(to);
      select();
    }
    else super.move(x, y);
  }
}

