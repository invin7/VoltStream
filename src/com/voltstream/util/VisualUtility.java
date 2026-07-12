package com.voltstream.util;

public class VisualUtility {
    public static void printSeparator() {
        System.out.println("==========================================================================");
    }
    
    public static void printStatusHeader(double utilization) {
        printSeparator();
        System.out.printf(" 📊 SYSTEM TELEMETRY SUMMARY | GRID STABILITY INDEX: %.1f%%%n", (1.0 - utilization) * 100);
        printSeparator();
    }

    // Clear the terminal console screen using standard ANSI Escape Sequences
    public static void clearScreen() {
        // \033[H moves cursor to home position, \033[2J clears the entire screen screen
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}