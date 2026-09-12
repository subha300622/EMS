package com.example.ems.common.exception;

public class ModuleDisabledException extends RuntimeException {

    private final String moduleCode;

    public ModuleDisabledException(String moduleCode, String message) {
        super(message);
        this.moduleCode = moduleCode;
    }

    public String getModuleCode() {
        return moduleCode;
    }
}
