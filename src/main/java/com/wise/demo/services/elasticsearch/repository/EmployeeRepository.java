package com.wise.demo.services.elasticsearch.repository;

import com.wise.demo.services.elasticsearch.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends ElasticsearchCrudRepository<Employee, Long> {

    List<Employee> findByOrganizationName(String name);

    Page<Employee> findByNameLike(String name, Pageable pageable);

}
