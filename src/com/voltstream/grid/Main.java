package com.voltstream.grid;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        EVNetworkController controller = new EVNetworkController(400.0);

        controller.addBay(new UltraFastBay("BAY-01 (Ultra-Fast 350kW)"));
        controller.addBay(new UltraFastBay("BAY-02 (Ultra-Fast 350kW)"));
        controller.addBay(new FastDCBay("BAY-03 (Fast-DC 150kW)"));
        
        boolean running = true;
        while (running) {
            com.voltstream.util.VisualUtility.clearScreen();

            System.out.println("\n--- 🎛️ CORE OPERATION CONTROLS ---");
            System.out.println("1. ➕ Dock New Electric Vehicle");
            System.out.println("2. 🕒 Simulate Time Elapse (Step 12-Min Grid Tick)");
            System.out.println("3. 📊 Print Live Telemetry Dashboard Status");
            System.out.println("4. ❌ Terminate Core Engine Simulation");
            System.out.print("Select Operational Option [1-4]: ");
            
            String choiceInput = scanner.nextLine().trim();
            int choice = 0;
            
            try {
                choice = Integer.parseInt(choiceInput);
            } catch (NumberFormatException e) {
                System.out.println("[⚠️] Invalid Entry. Please enter a numerical option.");
                System.out.print("\nPress Enter to try again...");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.println("\n--- 🧬 CONFIGURE VEHICLE INFRASTRUCTURE PROFILES ---");
                    VehicleType[] types = VehicleType.values();
                    
                    for (int i = 0; i < types.length; i++) {
                        System.out.printf("%d. %-18s (Intake Bound: %5.1f kW | Battery Capacity: %3.0f kWh)%n", 
                            (i + 1), types[i].getDisplayName(), types[i].getMaxChargingRate(), types[i].getBatteryCapacity());
                    }
                    
                    System.out.printf("Select Vehicle Category [1-%d]: ", types.length);
                    String typeInput = scanner.nextLine().trim();
                    int typeChoice = 0;
                    
                    try {
                        typeChoice = Integer.parseInt(typeInput);
                    } catch (NumberFormatException e) {
                        System.out.println("[⚠️] Invalid selection type. Returning to cockpit.");
                        System.out.print("\nPress Enter to return to main menu...");
                        scanner.nextLine();
                        break;
                    }

                    if (typeChoice < 1 || typeChoice > types.length) {
                        System.out.println("[⚠️] Selection outside operational boundaries.");
                        System.out.print("\nPress Enter to return to main menu...");
                        scanner.nextLine();
                        break;
                    }

                    VehicleType selectedType = types[typeChoice - 1];

                    System.out.print("Enter Unique Vehicle License Plate Number (e.g. MH-12-EV-77): ");
                    String plate = scanner.nextLine().toUpperCase().trim();
                    if (plate.isEmpty()) {
                        System.out.println("[⚠️] Identifier missing. Aborting allocation mapping.");
                        System.out.print("\nPress Enter to return to main menu...");
                        scanner.nextLine();
                        break;
                    }

                    System.out.print("Enter Initial State-of-Charge (SoC) Percentage (0-99): ");
                    String socInput = scanner.nextLine().trim();
                    double soc = 0.0;
                    
                    try {
                        soc = Double.parseDouble(socInput);
                    } catch (NumberFormatException e) {
                        System.out.println("[⚠️] Numeric parsing anomaly. Invalid percentage format.");
                        System.out.print("\nPress Enter to return to main menu...");
                        scanner.nextLine();
                        break;
                    }

                    try {
                        ElectricVehicle newEV = new GenericEV(plate, selectedType, soc);
                        boolean success = controller.dispatchVehicle(newEV);
                        if (success) {
                            System.out.println("[✔] Infrastructure Routing Complete: " + plate + " successfully routed to open hardware bay.");
                        } else {
                            System.out.println("[⚠️] Allocation Refused: All active hardware terminal slots currently saturated.");
                        }
                    } catch (com.voltstream.exception.InvalidVehicleException e) {
                        System.out.println("\n[❌ GRID REJECTION] " + e.getMessage());
                    }
                    
                    System.out.print("\nPress Enter to return to main menu...");
                    scanner.nextLine();
                    break;

                case 2:
                    System.out.println("\n[🕒] Executing 0.2 Hour (12-Minute) Real-Time Simulation Step...");
                    controller.executeTimeStep(0.2);
                    
                    System.out.print("\nPress Enter to return to main menu...");
                    scanner.nextLine();
                    break;

                case 3:
                    controller.executeTimeStep(0.0);
                    
                    System.out.print("\nPress Enter to return to main menu...");
                    scanner.nextLine(); 
                    break;

                case 4:
                    System.out.println("\n[❌] Shutting down VoltStream Microgrid Simulation. Clearing registers...");
                    running = false;
                    break;

                default:
                    System.out.println("[⚠️] Operational boundary exception. Option outside limits.");
                    System.out.print("\nPress Enter to try again...");
                    scanner.nextLine();
            }
        }
        scanner.close();
        System.out.println("================ ENGINE HALTED CLEANLY ================");
    }
}