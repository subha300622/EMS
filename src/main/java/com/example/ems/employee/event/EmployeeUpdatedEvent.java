package com.example.ems.employee.event;

import com.example.ems.employee.entity.Employee;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an employee profile is updated.
 */
public class EmployeeUpdatedEvent extends ApplicationEvent {
    private final Employee employee;

    public EmployeeUpdatedEvent(Object source, Employee employee) {
        super(source);
        this.employee = employee;
    }

    public Employee getEmployee() {
        return employee;
    }
}
