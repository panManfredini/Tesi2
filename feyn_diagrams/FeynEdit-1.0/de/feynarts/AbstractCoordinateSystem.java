package de.feynarts;

import java.awt.Dimension;


public abstract class AbstractCoordinateSystem implements CoordinateSystem {
  public int posX(double x) {
    return scaleX(x) + offsetX();
  }

  public double unPosX(int x) {
    return adjustX(unScaleX(x - offsetX()));
  }

  public int posY(double y) {
    return scaleY(y) + orientOffsetY();
  }

  public double unPosY(int y) {
    return adjustY(unScaleY(y - orientOffsetY()));
  }

  public int scaleX(double x) {
    return (int)Math.round(x*factorX());
  }

  public double unScaleX(int x) {
    return adjustX(((double)x)/factorX());
  }

  public int scaleY(double y) {
    return (int)Math.round(orientation()*y*factorY());
  }

  public double unScaleY(int y) {
    return adjustY(((double)y)/(orientation()*factorY()));
  }

  abstract public double factorX();    

  abstract public double factorY();

  abstract public int offsetX();

  abstract public int offsetY();

  private int orientOffsetY() {
    int result = 0;
    if( 1 == orientation() ) result += height();
    result += -orientation()*offsetY();
    return result;
  }

  public int orientation() {
    return 1;
  }

  public double adjustX(double x) {
    return x;
  }

  public double adjustY(double y) {
    return y;
  }

  public int height() {
    return getSize().height;
  }

  public int width() {
    return getSize().width;
  }

  abstract public Dimension getSize();
}

