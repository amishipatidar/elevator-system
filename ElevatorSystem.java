import java.util.*;

enum Direction {
    UP, DOWN, IDLE
}

abstract class Request {
    int floor;

    Request(int floor) {
        this.floor = floor;
    }
}

class ExternalRequest extends Request {
    Direction direction;

    ExternalRequest(int floor, Direction direction) {
        super(floor);
        this.direction = direction;
    }
}

class InternalRequest extends Request {
    int weight;

    InternalRequest(int floor, int weight) {
        super(floor);
        this.weight = weight;
    }
}

class ElevatorCar {
    private int id;
    private int currentFloor;
    private Direction direction;
    private int maxWeight;
    private int currentWeight;

    private TreeSet<Integer> upStops;
    private TreeSet<Integer> downStops;

    ElevatorCar(int id, int currentFloor, int maxWeight) {
        this.id = id;
        this.currentFloor = currentFloor;
        this.maxWeight = maxWeight;
        this.currentWeight = 0;
        this.direction = Direction.IDLE;

        upStops = new TreeSet<>();
        downStops = new TreeSet<>(Collections.reverseOrder());
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isIdle() {
        return direction == Direction.IDLE;
    }

    public void addExternalRequest(ExternalRequest request) {
        if (request.floor > currentFloor) {
            upStops.add(request.floor);
        } else if (request.floor < currentFloor) {
            downStops.add(request.floor);
        } else {
            System.out.println("Elevator " + id + " already at floor " + currentFloor);
        }
    }

    public void addInternalRequest(InternalRequest request) {
        if (currentWeight + request.weight > maxWeight) {
            System.out.println("Elevator " + id + " overloaded!");
            return;
        }

        currentWeight += request.weight;

        if (request.floor > currentFloor) {
            upStops.add(request.floor);
        } else if (request.floor < currentFloor) {
            downStops.add(request.floor);
        } else {
            System.out.println("Already at destination floor");
        }
    }

    public boolean hasRequests() {
        return !upStops.isEmpty() || !downStops.isEmpty();
    }

    public void step() {
        if (!upStops.isEmpty()) {
            direction = Direction.UP;
            int nextFloor = upStops.pollFirst();
            moveTo(nextFloor);

        } else if (!downStops.isEmpty()) {
            direction = Direction.DOWN;
            int nextFloor = downStops.pollFirst();
            moveTo(nextFloor);

        } else {
            direction = Direction.IDLE;
            System.out.println("Elevator " + id + " is idle at floor " + currentFloor);
        }
    }

    private void moveTo(int floor) {
        System.out.println("Elevator " + id + " moving from " + currentFloor + " to " + floor);

        currentFloor = floor;

        System.out.println("Elevator " + id + " reached floor " + currentFloor);

        // Passenger exits after reaching destination
        currentWeight = 0;

        if (!hasRequests()) {
            direction = Direction.IDLE;
        }
    }
}

interface ElevatorSelectionStrategy {
    ElevatorCar selectElevator(List<ElevatorCar> elevators, ExternalRequest request);
}

class SmartElevatorStrategy implements ElevatorSelectionStrategy {

    @Override
    public ElevatorCar selectElevator(List<ElevatorCar> elevators, ExternalRequest request) {

        ElevatorCar bestElevator = null;
        int minScore = Integer.MAX_VALUE;

        for (ElevatorCar elevator : elevators) {

            int score = Integer.MAX_VALUE;

            // Best preference: idle elevator
            if (elevator.isIdle()) {
                score = Math.abs(elevator.getCurrentFloor() - request.floor);
            }

            // Elevator moving up and request is also upward
            else if (elevator.getDirection() == Direction.UP
                    && request.direction == Direction.UP
                    && elevator.getCurrentFloor() <= request.floor) {

                score = request.floor - elevator.getCurrentFloor();
            }

            // Elevator moving down and request is also downward
            else if (elevator.getDirection() == Direction.DOWN
                    && request.direction == Direction.DOWN
                    && elevator.getCurrentFloor() >= request.floor) {

                score = elevator.getCurrentFloor() - request.floor;
            }

            if (score < minScore) {
                minScore = score;
                bestElevator = elevator;
            }
        }

        return bestElevator;
    }
}

class ElevatorController {
    private List<ElevatorCar> elevators;
    private ElevatorSelectionStrategy strategy;

    ElevatorController(ElevatorSelectionStrategy strategy) {
        this.strategy = strategy;
        this.elevators = new ArrayList<>();
    }

    public void addElevator(ElevatorCar elevator) {
        elevators.add(elevator);
    }

    public void requestElevator(int floor, Direction direction) {

        ExternalRequest request = new ExternalRequest(floor, direction);

        ElevatorCar selectedElevator = strategy.selectElevator(elevators, request);

        if (selectedElevator == null) {
            System.out.println("No elevator available");
            return;
        }

        System.out.println("\nRequest at floor " + floor + " for " + direction);
        System.out.println("Assigned Elevator: " + selectedElevator.getId());

        selectedElevator.addExternalRequest(request);
    }

    public void selectFloor(int elevatorId, int destinationFloor, int passengerWeight) {

        for (ElevatorCar elevator : elevators) {
            if (elevator.getId() == elevatorId) {

                elevator.addInternalRequest(
                        new InternalRequest(destinationFloor, passengerWeight)
                );
                return;
            }
        }

        System.out.println("Invalid elevator id");
    }

    public void runStep() {
        System.out.println("\n----- SYSTEM STEP -----");

        for (ElevatorCar elevator : elevators) {
            elevator.step();
        }
    }
}

public class ElevatorSystem {
    public static void main(String[] args) {

        ElevatorController controller =
                new ElevatorController(new SmartElevatorStrategy());

        controller.addElevator(new ElevatorCar(1, 0, 700));
        controller.addElevator(new ElevatorCar(2, 5, 1000));
        controller.addElevator(new ElevatorCar(3, 10, 500));

        controller.requestElevator(3, Direction.UP);
        controller.requestElevator(8, Direction.DOWN);

        controller.runStep();

        controller.selectFloor(1, 7, 60);
        controller.selectFloor(2, 1, 80);

        controller.runStep();
        controller.runStep();
        controller.runStep();
    }
}