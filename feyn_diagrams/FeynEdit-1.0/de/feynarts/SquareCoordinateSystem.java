package de.feynarts;

import java.awt.Dimension;


abstract public class SquareCoordinateSystem extends AbstractCoordinateSystem {
  private final int border = 7;
  private final double gridvalue = .5;
  private boolean grid = true;

  public double factorX() {
    return factor();
  }

  public double factorY() {
    return -orientation()*factor();
  }

  public double factor() {
    return (size()/20.);
  }

  public boolean getGrid() {
    return grid;
  }

  public void toggleGrid() {
    grid = !grid;
  }

  private int size() {
    return Math.min(height() - border, width() - border);
  }

  public int offsetX() {
    return (width() - size())/2;
  }

  public int offsetY() {
    return (height() - size())/2;
  }

  public double adjustX(double x) {
    return adjust(x);
  }

  public double adjustY(double x) {
    return adjust(x);
  }

  protected double adjust(double x) {
    return clip(grid(x));
  }

  private double clip(double x) {
    if( 0 > x ) return 0;
    if( 20 < x ) return 20;
    return x;
  }

  private double grid(double x) {
    if( grid ) return gridvalue*Math.rint(x/gridvalue);
    return x;
  }
}

