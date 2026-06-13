package com.sammedsp.fintrack.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import com.sammedsp.fintrack.exceptions.BadRequestException;

public class DateUtil {
    public static String validateAndGetDateString(Optional<String> date, String key){
        if (date.isEmpty() || date.get().trim().isEmpty()) {
        throw new BadRequestException("In valid " + key + "(Date)");
    }

    try {
        LocalDate.parse(date.get());
        return date.get();
    } catch (DateTimeParseException ex) {
        throw new BadRequestException("In valid date format (" + date + ") for " + key);
    }
    }
    
}
