package com.voltstream.grid;

import java.util.Scanner;

public class Main {
    private static final double GRID_CAPACITY = 400.0;
    private static final int TOTAL_BAYS = 3;
    private static final double TIME_STEP = 0.2;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        EVNetworkController controller = new EVNetworkController(GRID_CAPACITY);

        controller.addBay(new UltraFastBay("BAY-01 (Ultra-Fast 350kW)"));
        controller.addBay(new UltraFastBay("BAY-02 (Ultra-Fast 350kW)"));
        controller.addBay(new FastDCBay("BAY-03 (Fast-DC 150kW)"));
        
        
        boolean running = true;
        while (running) {
            com.voltstream.util.VisualUtility.clearScreen();

            System.out.println("==========================================");
            System.out.println("          VoltStream Simulator");
            System.out.println("==========================================");
            System.out.printf("Grid Capacity : %.0f kW%n", GRID_CAPACITY);
            System.out.printf("Charging Bays : %d%n", TOTAL_BAYS);

            System.out.println("\n========== Main Menu ==========");
            System.out.println("1. Add Vehicle");
            System.out.println("2. Simulate 12-Minute Time Step");
            System.out.println("3. View Dashboard");
            System.out.println("4. Exit Simulation");

            System.out.print("\nSelect an option [1-4]: ");
            
            String choiceInput = scanner.nextLine().trim();
            int choice = 0;
            
            try {
                choice = Integer.parseInt(choiceInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                System.out.print("\nPress Enter to try again...");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                	handleVehicleDocking(scanner, controller);
                    break;

                case 2:
                    System.out.println("\nAdvancing simulation by 12 minutes...");
                    controller.executeTimeStep(TIME_STEP);
                    
                    System.out.print("\nPress Enter to return to main menu...");
                    scanner.nextLine();
                    break;

                case 3:
                    controller.executeTimeStep(0);
                    
                    System.out.print("\nPress Enter to return to main menu...");
                    scanner.nextLine(); 
                    break;

                case 4:
                    System.out.println("\nShutting down VoltStream...");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid menu option.");
                    System.out.print("\nPress Enter to try again...");
                    scanner.nextLine();
            }
        }
        scanner.close();
        System.out.println("\nVoltStream terminated successfully.");
    }
    
    private static void handleVehicleDocking(Scanner scanner, EVNetworkController controller) {

        System.out.println("\n========== Add Vehicle ==========");
        VehicleType[] types = VehicleType.values();
        System.out.println();

        for (int i = 0; i < types.length; i++) {
            System.out.printf("%d. %-18s (Max Rate: %.1f kW | Battery: %.0f kWh)%n",
                    i + 1,
                    types[i].getDisplayName(),
                    types[i].getMaxChargingRate(),
                    types[i].getBatteryCapacity());
        }

        System.out.printf("Select Vehicle Type [1-%d]: ", types.length);

        int typeChoice;

        try {
            typeChoice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            waitForEnter(scanner);
            return;
        }

        if (typeChoice < 1 || typeChoice > types.length) {
            System.out.println("Selection out of range.");
            waitForEnter(scanner);
            return;
        }

        VehicleType selectedType = types[typeChoice - 1];

        System.out.print("Enter license plate: ");
        String plate = scanner.nextLine().trim().toUpperCase();

        if (plate.isEmpty()) {
            System.out.println("License plate cannot be empty.");
            waitForEnter(scanner);
            return;
        }

        System.out.print("Enter initial charge (0–99%): ");

        double soc;

        try {
            soc = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid percentage.");
            waitForEnter(scanner);
            return;
        }

        try {
            ElectricVehicle newEV = new GenericEV(plate, selectedType, soc);

            if (controller.dispatchVehicle(newEV)) {
                System.out.println("Vehicle successfully assigned to a charging bay.");
            } else {
                System.out.println("No charging bays are currently available.");
            }

        } catch (com.voltstream.exception.InvalidVehicleException e) {
            System.out.println(e.getMessage());
        }

        waitForEnter(scanner);
    }
    
    private static void waitForEnter(Scanner scanner) {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}

