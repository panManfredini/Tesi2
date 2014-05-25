package de.feynarts;


public class StringProperty implements Property {
  private String value;
  private int name;

  public StringProperty(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String get() {
    return value;
  }

  public void set(String value) {
    this.value = value;
  }

  public String getText() {
    return value;
  }

  public void setText(String string) {
    this.value = string;
  }
}

