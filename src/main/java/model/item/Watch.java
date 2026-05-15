package model.item;

public class Watch extends Item {
  private String brand; // Hãng: Rolex, Omega, Casio...

  public Watch(String id, String name, String brand) {
    super(id, name);
    this.brand = brand;
  }

  @Override
  public String getDetails() {
    return "Watch - Tên: " + getName() + " | Hãng: " + brand;
  }


}