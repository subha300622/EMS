package com.example.ems.reports.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedExportFormatException extends RuntimeException {
    public UnsupportedExportFormatException(String message) {
        super(message);
    }
}
