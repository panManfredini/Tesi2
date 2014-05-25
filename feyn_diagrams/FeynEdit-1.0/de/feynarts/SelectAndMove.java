package de.feynarts;


public class SelectAndMove extends Tool {
  private static final int RADIUS = 10;

  private DiagramPanel parent;
  private Entity active = null, target = null;

  public SelectAndMove(DiagramPanel parent) {
    super("Move");
    this.parent = parent;
  }

  public SelectAndMove(DiagramPanel parent, String name) {
    super(name);
    this.parent = parent;
  }

  protected void setActive(Entity active) {
    this.active = active;
  }

  protected void select() {
    parent.setSelected(active);
  }

  protected Entity getActive() {
    return active;
  }

  protected DiagramPanel getParent() {
    return parent;
  }

  public void begin(int x, int y) {
    Entity picked = parent.findEntity(x, y); 
    setActive(picked);
    select();
  }

  public void move(int x, int y) {
    try {
      active.draggedTo(x, y, parent.getCoordinateSystem());
      target = parent.findNextEntity(x, y);
    } catch( NullPointerException n ) {}
  }

  public void end() {
    if( canMerge() ) {
      active.merge(target);
      parent.setSelected(target);
    }
    target = null;
    setActive(null);
  }

  public void paint(java.awt.Graphics g) {
    if( canMerge() ) {
      g.setColor(java.awt.Color.black);
      int x = parent.getCoordinateSystem().posX(target.getX());
      int y = parent.getCoordinateSystem().posY(target.getY());
      g.drawOval(x - RADIUS, y - RADIUS, 2*RADIUS + 1, 2*RADIUS + 1);
    }
  }

  private boolean canMerge() {
    try {
      if( active.canMerge(target) ) return true;
    }
    catch( NullPointerException exception ) {}
    return false;
  }
}

