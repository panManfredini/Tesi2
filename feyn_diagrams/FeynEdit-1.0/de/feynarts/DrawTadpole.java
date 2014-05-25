package de.feynarts;


public class DrawTadpole extends SelectAndMove {
  private int xstart, ystart;

  public DrawTadpole(DiagramPanel parent) {
    super(parent, "Draw Tadpole");
  }

  public void begin(int x, int y) {
    xstart = x;
    ystart = y;
    setActive(null);
  }

  public void move(int x, int y) {
    if( getActive() == null ) {
      Vertex from = getParent().findOrCreateVertex(xstart, ystart);
      setActive(getParent().newTadpole(from, x, y));
      select();
    }
    else super.move(x, y);
  }
}

