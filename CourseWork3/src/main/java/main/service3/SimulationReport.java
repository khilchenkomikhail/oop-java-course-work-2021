package main.service3;

import java.util.ArrayList;

public class SimulationReport {
    private ArrayList<PortSimulation.DockedShip> listOfShips;
    private transient long sumWaitingShips = 0;
    private int amountOfUnloadShips;
    private double averageQueueLength;
    private long maxDelay = Integer.MIN_VALUE;
    private double averageDelay = 0;
    private long totalFine;
    private int amountContainerCranes;
    private int amountLooseCranes;
    private int amountLiquidCranes;

    public ArrayList<PortSimulation.DockedShip> getListOfShips() {
        calculateReport();
        return listOfShips;
    }

    public SimulationReport(int amountOfShips, int amountContainerCranes, int amountLooseCranes, int amountLiquidCranes) {
        if (amountOfShips > 0)
            listOfShips = new ArrayList<>(amountOfShips);
        this.amountContainerCranes = amountContainerCranes;
        this.amountLiquidCranes = amountLiquidCranes;
        this.amountLooseCranes = amountLooseCranes;
    }

    public void addDockedShip(PortSimulation.DockedShip dockedShip) {
        if (dockedShip != null) {
            listOfShips.add(dockedShip);
        }
    }

    public void increaseSumOfWaitingShips (int increase) {
        sumWaitingShips += increase;
    }

    public void setTotalFine(long totalFine) {
        this.totalFine = totalFine;
    }

    public void calculateReport() {
        for (int i = 0; i < listOfShips.size(); i++) {
            listOfShips.get(i).computeShipInfo();
            if (listOfShips.get(i).getShip().isEmpty())
                amountOfUnloadShips++;
            if (listOfShips.get(i).getDelay() > maxDelay) {
                maxDelay = listOfShips.get(i).getDelay();
            }
            averageDelay += listOfShips.get(i).getDelay();
        }
        averageDelay /= listOfShips.size();
        averageQueueLength = sumWaitingShips / (45 * 1440);
    }

    public int getAmountContainerCranes() {
        return amountContainerCranes;
    }

    public int getAmountLooseCranes() {
        return amountLooseCranes;
    }

    public int getAmountLiquidCranes() {
        return amountLiquidCranes;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < listOfShips.size(); i++) {
            stringBuilder.append(listOfShips.get(i).toString() + '\n');
        }
        stringBuilder.append("Total amount of completely unloaded ships: " + amountOfUnloadShips + '\n'
                + "Average length of queue of waiting ships: " + averageQueueLength + '\n'
                + "Average waiting time (delay before the start of unloading): " + averageDelay + '\n'
                + "Max time of waiting (delay before the start of unloading): " + maxDelay + '\n'
                + "Total fine (including costs of additional cranes): " + totalFine +'\n'
                + "Total amount of container cranes: " + amountContainerCranes + '\n'
                + "Total amount of loose cranes: " + amountLooseCranes + '\n'
                + "Total amount of liquid cranes: " + amountLiquidCranes + '\n');
        return stringBuilder.toString();
    }
}
