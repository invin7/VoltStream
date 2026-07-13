package com.voltstream.util;

public class VisualUtility {
    public static void printSeparator() {
        System.out.println("================================================================");
    }
    
    public static void printStatusHeader(double utilization) {
        printSeparator();
        System.out.println("                 VoltStream Dashboard");
        printSeparator();
        System.out.printf("Grid Utilization : %.1f%%%n", (1.0 - utilization) * 100);
        printSeparator();
    }


    public static void clearScreen() {
        // Move cursor to the top-left corner and clear the terminal.
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
