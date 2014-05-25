package de.feynarts;


public interface CoordinateSystem {
  public int posX(double x);

  public int posY(double y);

  public int scaleX(double x);

  public int scaleY(double y);

  public double factorX();

  public double factorY();

  public int orientation();

  public double unPosX(int x);

  public double unPosY(int y);

  public double unScaleX(int x);

  public double unScaleY(int y);
}

