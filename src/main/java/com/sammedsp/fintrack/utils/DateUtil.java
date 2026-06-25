package com.sammedsp.fintrack.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.sammedsp.fintrack.exceptions.BadRequestException;

public class DateUtil {
    public static String validateAndGetDateString(Optional<String> date, String key) {
        if (date.isEmpty() || date.get().trim().isEmpty()) {
            throw new BadRequestException("Invalid " + key + "(Date)");
        }

        try {
            LocalDate.parse(date.get());
            return date.get();
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid date format (" + date + ") for " + key);
        }
    }

    public static void validateDateRange(String startDateString, String endDateString) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        if (ChronoUnit.DAYS.between(startDate, endDate) > 365) {
            throw new BadRequestException("Date range cannot exceed 1 year");
        }
    }

}
