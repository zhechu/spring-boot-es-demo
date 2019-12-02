package com.wise.demo.services.elasticsearch.controller;

import com.alibaba.fastjson.JSON;
import com.wise.demo.services.elasticsearch.model.Employee;
import com.wise.demo.services.elasticsearch.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@Slf4j
public class EmployeeController {

    @Autowired
    EmployeeRepository repository;

    @Autowired
    ElasticsearchTemplate template;

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

    /**
     * 更新文档（只更新有值的字段）
     *
     * @param employee
     * @return java.lang.Boolean
     * @author lingyuwang
     * @date 2019/11/30 11:31
     */
    @PutMapping
    public Boolean update(@RequestBody Employee employee) {
        UpdateQuery updateQuery = new UpdateQuery();

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.retryOnConflict(3);

        String jsonString = JSON.toJSONString(employee);
        updateRequest.doc(jsonString, XContentType.JSON);

        // _id
        updateQuery.setId(String.valueOf(employee.getId()));

        updateQuery.setUpdateRequest(updateRequest);
        updateQuery.setClazz(Employee.class);

        UpdateResponse updateResponse = template.update(updateQuery);

        DocWriteResponse.Result result = updateResponse.getResult();
        if (DocWriteResponse.Result.UPDATED.equals(result) || DocWriteResponse.Result.NOOP.equals(result)) {
            return true;
        }

        return false;
    }

    @GetMapping("/ageAvg")
    public double ageAvg() {

        /*
        POST employees/_search
        {
          "size": 0,
          "aggs": {
            "avg_age": {
              "avg": {
                "field": "age"
              }
            }
          }
        }
        */
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.avg("avg_age").field("age"))
                .build();

        double ageAvg = template.query(searchQuery, response -> {
            InternalAvg avg = (InternalAvg) response.getAggregations().asList().get(0);
            return avg.getValue();
        });

        return ageAvg;
    }

}
