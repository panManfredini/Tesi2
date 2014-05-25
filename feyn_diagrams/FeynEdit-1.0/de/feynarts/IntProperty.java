package de.feynarts;


public class IntProperty implements Property {
  private int value;
  private String name;

  public IntProperty(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public int get() {
    return value;
  }

  public void set(int value) {
    this.value = value;
  }

  public String getText() {
    return Integer.toString(value);
  }

  public void setText(String string) {
    try {
      value = Integer.parseInt(string);
    }
    catch( NumberFormatException exception ) {}
  }
}

