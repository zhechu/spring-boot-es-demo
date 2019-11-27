package com.wise.demo.services.elasticsearch.controller;

import com.wise.demo.services.elasticsearch.repository.EmployeeRepository;
import com.wise.demo.services.elasticsearch.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    EmployeeRepository repository;

    @PostMapping
    public Employee add(@RequestBody Employee employee) {
        return repository.save(employee);
    }

    @GetMapping("/{name}")
    public List<Employee> findByName(@PathVariable("name") String name) {
        return repository.findByName(name);
    }

    @PostMapping("/findByIds")
    public Iterable<Employee> findByIds(@RequestBody List<Long> ids) {
        return repository.findAllById(ids);
    }

    @GetMapping("/organization/{organizationName}")
    public List<Employee> findByOrganizationName(@PathVariable("organizationName") String organizationName) {
        return repository.findByOrganizationName(organizationName);
    }

    @DeleteMapping(value = "/{id}")
    public String delete(@PathVariable Long id) {
        repository.deleteById(id);
        return "success";
    }

    @PutMapping
    public Employee update(@RequestBody Employee employee) {
        return repository.save(employee);
    }

}
