package main.service3;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Crane extends Thread{
    private PortSimulation.DockedShip ship;
    private final CargoType cargoType;
    private boolean isWorking;
    private CyclicBarrier barrierFirst;
    private CyclicBarrier barrierSecond;

    public Crane(CargoType cargoType, CyclicBarrier barrierFirst, CyclicBarrier barrierSecond) {
        ship = null;
        this.cargoType = cargoType;
        this.barrierFirst = barrierFirst;
        this.barrierSecond = barrierSecond;
        isWorking = true;
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        while (isWorking) {
            try {
                barrierFirst.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            if (ship != null) {
                if(ship.getShip().getAmountOfCargo().updateAndGet(operand -> (operand > 0) ? operand - 1 : operand) == 0)
                    ship = null;
            }
            try {
                barrierSecond.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    public void setShip(PortSimulation.DockedShip ship, int day, int time) {
        this.ship = ship;
        if (ship.getUploadStartDay() == -10) ship.setUploadStartDayAndMinutes(day, time);
    }

    public void killCrane() { isWorking = false; }

    public PortSimulation.DockedShip getShip() {
        return ship;
    }

    public boolean isFree() {
        return ship == null;
    }

    public CargoType getCargoType() {
        return cargoType;
    }
}
