package model.item;

public class Vehicle extends Item {
  private final int mileage; // Số km đã đi

  public Vehicle(String id, String name, int mileage) {
    super(id, name);
    this.mileage = mileage;
  }

  @Override
  public String getDetails() {
    return "Vehicle - Tên: " + getName() + " | Số km đã đi: " + mileage + " km";
  }
}