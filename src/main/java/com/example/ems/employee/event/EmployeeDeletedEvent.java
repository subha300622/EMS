package com.example.ems.employee.event;

import com.example.ems.employee.entity.Employee;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an employee profile is deleted.
 */
public class EmployeeDeletedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final transient Employee employee;

    public EmployeeDeletedEvent(Object source, Employee employee) {
        super(source);
        this.employee = employee;
    }

    public Employee getEmployee() {
        return employee;
    }
}
