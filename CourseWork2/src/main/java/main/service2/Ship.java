package main.service2;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Ship implements Comparable{
    public static final int maxAmountOfContainers = 2000;
    public static final int maxLooseWeight = 2000;
    public static final int maxLiquidWeight = 2000;

    private String name;
    private CargoType cargoType;
    public AtomicInteger amountOfCargo;
    private int arrivalDay;
    private int arrivalTime;
    private int timeOfStaying;

    public Ship(String name, CargoType cargoType, int amountOfCargo, int arrivalDay, int arrivalTime) {
        this.name = name;
        this.cargoType = cargoType;
        this.amountOfCargo = new AtomicInteger();
        this.amountOfCargo.set(amountOfCargo);
        this.arrivalDay = arrivalDay;
        this.arrivalTime = arrivalTime;
        timeOfStaying = amountOfCargo;
    }

    public Ship(Ship other) {
        amountOfCargo = new AtomicInteger();
        if (other != null) {
            this.name = other.getName();
            this.cargoType = other.getCargoType();
            this.amountOfCargo.set(other.getAmountOfCargo().get());
            this.arrivalDay = other.getArrivalDay();
            this.arrivalTime = other.getArrivalTime();
            this.timeOfStaying = other.getTimeOfStaying();
        }
    }

    public void setDelayByDay(int delay) {
        if (delay < -7 || delay > 7) {
            throw new IllegalArgumentException();
        }
        arrivalDay += delay;
    }

    public void setDelayByTime(int delay) {
        if (delay <= -1440 || delay >= 1440) {
            throw new IllegalArgumentException();
        }

        arrivalTime += delay;
        if (arrivalTime < 0) {
           arrivalDay--;
           arrivalTime += 1440;
        } else if(arrivalTime >= 1440) {
            arrivalDay++;
            arrivalTime -= 1440;
        }
    }

    public String getName() {
        return name;
    }

    public CargoType getCargoType() {
        return cargoType;
    }

    public AtomicInteger getAmountOfCargo() {
        return amountOfCargo;
    }

    public boolean isEmpty() {return amountOfCargo.get() == 0;}

    public int unloadCargoUnit() {
        if (amountOfCargo.get() != 0) {
            int temp = amountOfCargo.decrementAndGet();
            return temp;
        }
        else
            return 0;
    }

    public int getArrivalDay() {
        return arrivalDay;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getTimeOfStaying() {
        return timeOfStaying;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return arrivalDay == ship.arrivalDay && arrivalTime == ship.arrivalTime && timeOfStaying == ship.timeOfStaying && Objects.equals(name, ship.name) && cargoType == ship.cargoType && Objects.equals(amountOfCargo, ship.amountOfCargo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cargoType, amountOfCargo, arrivalDay, arrivalTime, timeOfStaying);
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) return -1;
        Ship ship = (Ship) o;
        if (getArrivalDay() < ship.getArrivalDay()) return -1;
        if (getArrivalDay() == ship.getArrivalDay()) {
            if (getArrivalTime() <= ship.getArrivalTime()) return -1;
            else return 1;
        }
        return 1;
    }

    @Override
    public String toString() {
        int arrivalHours = arrivalTime / 60;
        int tempArrivalMinutes = arrivalTime - arrivalHours*60;
        return "Name: " + name +
                "; cargoType: " + cargoType +
                "; amountOfCargo: " + amountOfCargo +
                "; arrivalTime: " + arrivalDay + ' ' + arrivalHours + ' '  + tempArrivalMinutes +
                "; timeOfStaying: " + timeOfStaying;
    }
}
