package com.example.ems.employee.service;

import com.example.ems.employee.dto.DepartmentRequest;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.DepartmentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public Department createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Department name already exists");
        }
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Department code already exists");
        }
        Department d = new Department();
        d.setName(request.getName());
        d.setCode(request.getCode().trim().toUpperCase());
        d.setDescription(request.getDescription());
        return departmentRepository.save(d);
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    public Department updateDepartment(Long id, DepartmentRequest request) {
        Department d = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        Optional<Department> optName = departmentRepository.findByName(request.getName());
        if (optName.isPresent() && !optName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Department name already exists");
        }

        Optional<Department> optCode = departmentRepository.findByCode(request.getCode());
        if (optCode.isPresent() && !optCode.get().getId().equals(id)) {
            throw new IllegalArgumentException("Department code already exists");
        }

        d.setName(request.getName());
        d.setCode(request.getCode().trim().toUpperCase());
        d.setDescription(request.getDescription());
        return departmentRepository.save(d);
    }

    public boolean deleteDepartment(Long id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
