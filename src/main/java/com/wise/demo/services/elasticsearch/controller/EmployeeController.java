package com.wise.demo.services.elasticsearch.controller;

import com.wise.demo.services.elasticsearch.model.Employee;
import com.wise.demo.services.elasticsearch.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @GetMapping("/findByName")
    public Page<Employee> findByName(@RequestParam String name,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        return repository.findByNameLike(name, PageRequest.of(page - 1, size, Sort.Direction.DESC, "age"));
    }

    @PostMapping("/findByIds")
    public Iterable<Employee> findByIds(@RequestBody List<Long> ids) {
        return repository.findAllById(ids);
    }

    @GetMapping("/selectByPage")
    public Page<Employee> selectByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 页码从 0 开始，表示第一页，为了方便，前端传参统一使用从 1 开始，所以这里页码要减 1
        return repository.findAll(PageRequest.of(page - 1, size, Sort.Direction.DESC, "age"));
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
