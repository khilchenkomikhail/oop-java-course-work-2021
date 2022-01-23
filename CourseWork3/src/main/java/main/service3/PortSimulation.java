package main.service3;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class PortSimulation {

    private static long containerShipsPenalty;
    private static long looseShipsPenalty;
    private static long liquidShipsPenalty;

    private static ArrayList<LinkedList<Ship>> cloneTimetable(ArrayList<LinkedList<Ship>> original) {
        ArrayList<LinkedList<Ship>> timetable = new ArrayList<>();
        timetable.add(new LinkedList<>());
        timetable.add(new LinkedList<>());
        timetable.add(new LinkedList<>());

        for(int i = 0; i < original.get(0).size(); i++) {
            timetable.get(0).add(new Ship(original.get(0).get(i)));
        }
        for(int i = 0; i < original.get(1).size(); i++) {
            timetable.get(1).add(new Ship(original.get(1).get(i)));
        }
        for(int i = 0; i < original.get(2).size(); i++) {
            timetable.get(2).add(new Ship(original.get(2).get(i)));
        }

        return timetable;
    }

    public static SimulationReport computeOptimum(String JSONtimetable) {
        final ArrayList<LinkedList<Ship>> finalTimetable = getTimetableWithDelays(JSONtimetable);

        ArrayList<LinkedList<Ship>> timetable = cloneTimetable(finalTimetable);


        int[] result = { 1, 1, 1 };

        SimulationReport simulationReport = simulatePortProcess(result[0], result[1], result[2], 0, 0, 0, timetable);

        while (((containerShipsPenalty / 30000) > (result[0] - 1) || (looseShipsPenalty / 30000) > (result[1] - 1)
                || (liquidShipsPenalty / 30000) > (result[2] - 1))) {

            timetable = cloneTimetable(finalTimetable);

            if ((containerShipsPenalty / 30000) > (result[0] - 1))
                result[0]++;
            if ((looseShipsPenalty / 30000) > (result[1] - 1))
                result[1]++;
            if ((liquidShipsPenalty / 30000) > (result[2] - 1))
                result[2]++;
            simulationReport = simulatePortProcess(result[0], result[1], result[2],
                    (result[0] - 1)*30000, (result[1] - 1)*30000,
                    (result[2] - 1)*30000, timetable);
        }

        simulationReport.calculateReport();
        System.out.println("Finish");
        return simulationReport;
    }

    private static ArrayList<LinkedList<Ship>> getTimetableWithDelays(String JSONtimetable) {
        Type listOfShips = new TypeToken<ArrayList<LinkedList<Ship>>>() {}.getType();
        Gson gson = new Gson();
        ArrayList<LinkedList<Ship>> timetable = gson.fromJson(JSONtimetable, listOfShips);

        Random random = new Random();

        for (int i = 0; i < timetable.size(); i++) {
            for (int j = 0; j < timetable.get(i).size(); j++) {
                timetable.get(i).get(j).setDelayByDay(random.nextInt(15) - 7);
                timetable.get(i).get(j).setDelayByTime(random.nextInt(2879) - 1439);
            }
        }

        timetable.get(0).sort(Ship::compareTo);
        timetable.get(1).sort(Ship::compareTo);
        timetable.get(2).sort(Ship::compareTo);
        return  timetable;
    }

    public static SimulationReport simulatePortProcess(int amountContainerCranes, int amountLooseCranes, int amountLiquidCranes,
                                            int containerPenalty, int loosePenalty, int liquidPenalty, ArrayList<LinkedList<Ship>> timetable) {
        SimulationReport simulationReport = new SimulationReport((timetable.get(0).size() + timetable.get(1).size() + timetable.get(2).size()),
                amountContainerCranes, amountLooseCranes, amountLiquidCranes);

        containerShipsPenalty = containerPenalty;
        looseShipsPenalty = loosePenalty;
        liquidShipsPenalty = liquidPenalty;

        ArrayList<Crane> containerCranes = new ArrayList<>(amountContainerCranes);
        ArrayList<Crane> looseCranes = new ArrayList<>(amountLooseCranes);
        ArrayList<Crane> liquidCranes = new ArrayList<>(amountLiquidCranes);

        CyclicBarrier barrierFirst = new CyclicBarrier(amountContainerCranes + amountLiquidCranes + amountLooseCranes + 1);
        CyclicBarrier barrierSecond = new CyclicBarrier(amountContainerCranes + amountLiquidCranes + amountLooseCranes + 1);

        for (int i = 0; i < amountContainerCranes; i++)
            containerCranes.add(new Crane(CargoType.CONTAINER, barrierFirst, barrierSecond));
        for (int i = 0; i < amountLooseCranes; i++)
            looseCranes.add(new Crane(CargoType.LOOSE, barrierFirst, barrierSecond));
        for (int i = 0; i < amountLiquidCranes; i++)
            liquidCranes.add(new Crane(CargoType.LIQUID, barrierFirst, barrierSecond));

        ArrayList<DockedShip> containerShips = new ArrayList<>(timetable.get(0).size());
        ArrayList<DockedShip> looseShips = new ArrayList<>(timetable.get(1).size());
        ArrayList<DockedShip> liquidShips = new ArrayList<>(timetable.get(2).size());

        for (int day = -7; day < 38; day++) {

            for (int minutes = 0; minutes < 1440; minutes += 1) {

                while (!timetable.get(0).isEmpty() && timetable.get(0).get(0).getArrivalDay() == day
                        && timetable.get(0).get(0).getArrivalTime() == minutes) {
                    containerShips.add(new DockedShip(timetable.get(0).peekFirst()));
                    timetable.get(0).poll();
                }
                while (!timetable.get(1).isEmpty() && timetable.get(1).get(0).getArrivalDay() == day
                        && timetable.get(1).get(0).getArrivalTime() == minutes) {
                    looseShips.add(new DockedShip(timetable.get(1).peekFirst()));
                    timetable.get(1).poll();
                }
                while (!timetable.get(2).isEmpty() && timetable.get(2).get(0).getArrivalDay() == day
                        && timetable.get(2).get(0).getArrivalTime() == minutes) {
                    liquidShips.add(new DockedShip(timetable.get(2).peekFirst()));
                    timetable.get(2).poll();
                }

                allocateCranes(containerCranes, containerShips, day, minutes);

                allocateCranes(looseCranes, looseShips, day, minutes);

                allocateCranes(liquidCranes, liquidShips, day, minutes);

                removeEmptyShips(containerShips, day, minutes, simulationReport);

                removeEmptyShips(looseShips, day, minutes, simulationReport);

                removeEmptyShips(liquidShips, day, minutes, simulationReport);

                try {
                    barrierFirst.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
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
        {
            for (int i = 0; i < containerShips.size(); i++) {
                containerShipsPenalty += (containerShips.get(i).getPenalty());
                simulationReport.addDockedShip(containerShips.get(i));
            }
            for (int i = 0; i < looseShips.size(); i++) {
                looseShipsPenalty += (looseShips.get(i).getPenalty());
                simulationReport.addDockedShip(looseShips.get(i));
            }
            for (int i = 0; i < liquidShips.size(); i++) {
                liquidShipsPenalty += (liquidShips.get(i).getPenalty());
                simulationReport.addDockedShip(liquidShips.get(i));
            }

            for (int i = 0; i < containerCranes.size(); i++) {
                synchronized (containerCranes.get(i)) {
                    containerCranes.get(i).killCrane();
                }
            }
            for (int i = 0; i < looseCranes.size(); i++) {
                synchronized (looseCranes.get(i)) {
                    looseCranes.get(i).killCrane();
                }
            }
            for (int i = 0; i < liquidCranes.size(); i++) {
                synchronized (liquidCranes.get(i)) {
                    liquidCranes.get(i).killCrane();
                }
            }
        }

        simulationReport.setTotalFine(containerShipsPenalty + looseShipsPenalty + liquidShipsPenalty);

        return simulationReport;
    }

    private static void removeEmptyShips(ArrayList<DockedShip> listOfShips, int day, int minutes, SimulationReport simulationReport) {
        for (int counter = 0; counter < listOfShips.size(); counter++) {
            synchronized (listOfShips.get(counter)) {
                if (listOfShips.get(counter).getNumberOfCranes() == 0 && !listOfShips.get(counter).getShip().isEmpty()) {
                    simulationReport.increaseSumOfWaitingShips(1);
                }
                if (listOfShips.get(counter).getShip().isEmpty()) {

                    listOfShips.get(counter).setUploadStopDay(day);
                    listOfShips.get(counter).setUploadStopTime(minutes);
                    simulationReport.addDockedShip(listOfShips.get(counter));

                    if(listOfShips.get(counter).getShip().getCargoType() == CargoType.CONTAINER)
                        containerShipsPenalty += listOfShips.get(counter).getPenalty();
                    if(listOfShips.get(counter).getShip().getCargoType() == CargoType.LOOSE)
                        looseShipsPenalty += listOfShips.get(counter).getPenalty();
                    if(listOfShips.get(counter).getShip().getCargoType() == CargoType.LIQUID)
                        liquidShipsPenalty += listOfShips.get(counter).getPenalty();

                    listOfShips.remove(counter);
                    counter--;
                }
            }
        }
    }

   public static void allocateCranes(ArrayList<Crane> cranes, ArrayList<DockedShip> ships, int day, int minutes) {
        for (int j = 0; j < ships.size(); j++) {
            synchronized (ships.get(j)) {
                for (int i = 0; i < cranes.size() && ships.get(j).getNumberOfCranes() != 2; i++) {
                    if (cranes.get(i).isFree() && !ships.get(j).getShip().isEmpty()) {
                        if (ships.get(j).getNumberOfCranes() == 0)
                            ships.get(j).setUploadStartDayAndMinutes(day, minutes);
                        ships.get(j).increaseNumberOfCranes();
                        cranes.get(i).setShip(ships.get(j), day, minutes);
                    }
                }
            }
        }
    }

    public static class DockedShip {
        private transient Ship ship;
        private transient AtomicInteger uploadStartDay = new AtomicInteger();
        private transient AtomicInteger uploadStartMinutes = new AtomicInteger();
        private transient AtomicInteger uploadStopDay = new AtomicInteger();
        private transient AtomicInteger uploadStopTime = new AtomicInteger();
        private transient AtomicInteger numberOfCranes = new AtomicInteger();

        public enum State {
            DONE,
            NOT_STARTED,
            NOT_FINISHED
        }

        private class ShipInfo {
            public transient State state;

            public String name;
            public int arrivalDay;
            public int arrivalHours;
            public int arrivalMinutes;
            public int waitingDays;
            public int waitingHours;
            public int waitingMinutes;
            public int beginOfUploadingDays = Integer.MIN_VALUE;
            public int beginOfUploadingHours = Integer.MIN_VALUE;
            public int beginOfUploadingMinutes = Integer.MIN_VALUE;
            public int uploadingDays = Integer.MIN_VALUE;
            public int uploadingHours = Integer.MIN_VALUE;
            public int uploadingMinutes = Integer.MIN_VALUE;

            public ShipInfo() {
                beginOfUploadingDays = Integer.MIN_VALUE;
                beginOfUploadingHours = Integer.MIN_VALUE;
                beginOfUploadingMinutes = Integer.MIN_VALUE;
                uploadingDays = Integer.MIN_VALUE;
                uploadingHours = Integer.MIN_VALUE;
                uploadingMinutes = Integer.MIN_VALUE;
            }
        }

        ShipInfo shipInfo = new ShipInfo();

        public void computeShipInfo() {

            shipInfo.name = ship.getName();
            shipInfo.arrivalDay = ship.getArrivalDay();
            int tempArrivalMinutes = ship.getArrivalTime();
            int tempArrivalHours = tempArrivalMinutes / 60;
            tempArrivalMinutes -= (tempArrivalHours * 60);
            shipInfo.arrivalDay = ship.getArrivalDay();
            shipInfo.arrivalHours = tempArrivalHours;
            shipInfo.arrivalMinutes = tempArrivalMinutes;

            if (!ship.isEmpty()) {
                if (uploadStartDay.get() != -10 && uploadStartMinutes.get() != -10) {
                    shipInfo.state = State.NOT_FINISHED;

                    int minutesOfWaiting = 1440 * (uploadStartDay.get() - ship.getArrivalDay())
                            + (uploadStartMinutes.get() - ship.getArrivalTime());
                    int daysOfWaiting = minutesOfWaiting / 1440;
                    minutesOfWaiting -= daysOfWaiting * 1440;
                    int hoursOfWaiting = minutesOfWaiting / 60;
                    minutesOfWaiting -= hoursOfWaiting * 60;
                    int tempUploadStartMinutes = uploadStartMinutes.get();
                    int tempUploadStartHours = tempUploadStartMinutes/60;
                    tempUploadStartMinutes -= (tempUploadStartHours * 60);

                    shipInfo.waitingDays = daysOfWaiting;
                    shipInfo.waitingHours = hoursOfWaiting;
                    shipInfo.waitingMinutes = minutesOfWaiting;

                    shipInfo.beginOfUploadingDays = uploadStartDay.get();
                    shipInfo.beginOfUploadingHours = tempUploadStartHours;
                    shipInfo.beginOfUploadingMinutes = tempUploadStartMinutes;

                } else {
                    shipInfo.state = State.NOT_STARTED;

                    int stopDay = 37;
                    int stopMinutes = 1439;
                    int minutesOfWaiting = 1440 * (stopDay - ship.getArrivalDay())
                            + (stopMinutes - ship.getArrivalTime());
                    int daysOfWaiting = minutesOfWaiting / 1440;
                    minutesOfWaiting -= daysOfWaiting * 1440;
                    int hoursOfWaiting = minutesOfWaiting / 60;
                    minutesOfWaiting -= hoursOfWaiting * 60;

                    shipInfo.waitingDays = daysOfWaiting;
                    shipInfo.waitingHours = hoursOfWaiting;
                    shipInfo.waitingMinutes = minutesOfWaiting;

                }
            } else {
                shipInfo.state = State.DONE;

                int minutesOfWaiting = 1440 * (uploadStartDay.get() - ship.getArrivalDay())
                        + (uploadStartMinutes.get() - ship.getArrivalTime());
                int daysOfWaiting = minutesOfWaiting / 1440;
                minutesOfWaiting -= daysOfWaiting * 1440;
                int hoursOfWaiting = minutesOfWaiting / 60;
                minutesOfWaiting -= hoursOfWaiting * 60;
                int minutesOfUploading = 1440 * (uploadStopDay.get() - uploadStartDay.get())
                        + (uploadStopTime.get() - uploadStartMinutes.get());
                int daysOfUploading = minutesOfUploading / 1440;
                minutesOfUploading -= daysOfUploading * 1440;
                int hoursOfUploading = minutesOfUploading / 60;
                minutesOfUploading -= hoursOfUploading * 60;
                int tempUploadStartMinutes = uploadStartMinutes.get();
                int tempUploadStartHours = tempUploadStartMinutes/60;
                tempUploadStartMinutes -= (tempUploadStartHours * 60);

                shipInfo.waitingDays = daysOfWaiting;
                shipInfo.waitingHours = hoursOfWaiting;
                shipInfo.waitingMinutes = minutesOfWaiting;

                shipInfo.beginOfUploadingDays = uploadStartDay.get();
                shipInfo.beginOfUploadingHours = tempUploadStartHours;
                shipInfo.beginOfUploadingMinutes = tempUploadStartMinutes;

                shipInfo.uploadingDays = daysOfUploading;
                shipInfo.uploadingHours = hoursOfUploading;
                shipInfo.uploadingMinutes = minutesOfUploading;

            }
        }

        public Ship getShip() {
            return ship;
        }

        public DockedShip(Ship ship) {
            this.ship = ship;
            numberOfCranes.set(0);
            uploadStartDay.set(-10);
            uploadStartMinutes.set(-10);
            uploadStopDay.set(-10);
            uploadStopTime.set(-10);
        }

        public void increaseNumberOfCranes() {
            numberOfCranes.incrementAndGet();
        }

        public int getNumberOfCranes() {
            return numberOfCranes.get();
        }

        public void setUploadStartDayAndMinutes(int uploadStartDay, int uploadStartMinutes) {
            this.uploadStartMinutes.set(uploadStartMinutes);
            this.uploadStartDay.set(uploadStartDay);
        }

        public int getUploadStartDay() {
            return uploadStartDay.get();
        }

        public int getUploadStartMinutes() {
            return uploadStartMinutes.get();
        }

        public void setUploadStopDay(int uploadStopDay) {
            this.uploadStopDay.set(uploadStopDay);
        }

        public int getUploadStopDay() {
            return uploadStopDay.get();
        }

        public void setUploadStopTime(int uploadStopTime) {
            this.uploadStopTime.set(uploadStopTime);
        }

        public int getUploadStopTime() {
            return uploadStopTime.get();
        }

        public int getDelay() {
            computeShipInfo();
            int penaltyMinutes = shipInfo.waitingDays * 1440 + shipInfo.waitingHours * 60 + shipInfo.waitingMinutes;
            return penaltyMinutes;
        }

        public int getPenalty() {
            int penaltyMinutes = getDelay();
            int penaltyHours = (penaltyMinutes / 60) + ((penaltyMinutes % 60) > 0 ? 1 : 0);
            return (penaltyHours * 100);
        }

        public String toString() {

            computeShipInfo();

            if (shipInfo.state == State.DONE) {
                return ("Name: " + shipInfo.name
                        + "; Arrival time: " + shipInfo.arrivalDay + ' '
                        + shipInfo.arrivalHours + ' ' + shipInfo.arrivalMinutes
                        + "; Waiting time: " + shipInfo.waitingDays + ' ' + shipInfo.waitingHours + ' ' + shipInfo.waitingMinutes
                        + "; Begin of uploading: " + shipInfo.beginOfUploadingDays + ' ' + shipInfo.beginOfUploadingHours + ' ' + shipInfo.beginOfUploadingMinutes
                        + "; Uploading time: " + shipInfo.uploadingDays + ' ' + shipInfo.uploadingHours
                        + ' ' + shipInfo.uploadingMinutes
                        + "; Penalty: " + getPenalty());
            } else if (shipInfo.state == State.NOT_FINISHED) {
                return ("Name: " + shipInfo.name
                        + "; Arrival time: " + shipInfo.arrivalDay + ' '
                        + shipInfo.arrivalHours + ' ' + shipInfo.arrivalMinutes
                        + "; Waiting time: " + shipInfo.waitingDays + ' ' + shipInfo.waitingHours + ' ' + shipInfo.waitingMinutes
                        + "; Begin of uploading: " + shipInfo.beginOfUploadingDays + ' ' + shipInfo.beginOfUploadingHours + ' ' + shipInfo.beginOfUploadingMinutes
                        + "; Uploading wasn't finished "
                        + "; Penalty: " + getPenalty());
            } else {
                return ("Name: " + shipInfo.name
                        + "; Arrival time: " + shipInfo.arrivalDay + ' '
                        + shipInfo.arrivalHours + ' ' + shipInfo.arrivalMinutes
                        + "; Waiting time: " + shipInfo.waitingDays + ' ' + shipInfo.waitingHours + ' ' + shipInfo.waitingMinutes
                        + "; Uploading wasn't started "
                        + "; Uploading wasn't finished"
                        + "; Penalty: " + getPenalty());
            }
        }
    }
}
