package com.github.steeldev.deathnote.util;


// Thank you Jay/Ankoki for this <3
public class Timespan {
    // CALCULATE TICKS:
    // SECONDS: 20 * 3 = 60 ticks (3 seconds)
    // MINUTES: 20 * 60 * 50 = 60k ticks (50 minutes)
    // HOURS: 20 * 60 * 60 * 3 = 216k ticks (3 hours)
    // DAYS: 20 * 60 * 60 * 24 * 1 = 1728000 ticks (1 day)
    public static long parse(String s) {
        String[] split = s.split(" ");
        if (split.length != 2) return -1;
        long amount;
        try {
            amount = Long.parseLong(split[0]);
        } catch (NumberFormatException ex) {
            return -1;
        }
        switch (split[1].toUpperCase()) {
            case "TICK":
            case "TICKS":
                return amount;
            case "SECONDS":
            case "SECOND":
                return (amount * 20);
            case "MINUTE":
            case "MINUTES":
                return (60 * amount) * 20;
            case "HOUR":
            case "HOURS":
                return (3600 * amount) * 20;
            case "DAY":
            case "DAYS":
                return (86400 * amount) * 20;
        }
        return -1;
    }
}
