package com.wise.demo.services.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.wise.demo.services.elasticsearch.model.*;
import com.wise.demo.services.elasticsearch.repository.EmployeeRepository;
import com.wise.demo.services.elasticsearch.repository.TbVideoRepository;
import com.wise.demo.services.elasticsearch.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.AliasQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import javax.annotation.PostConstruct;
import java.util.*;

public class SampleDataSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleDataSet.class);

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    TbVideoRepository tbVideoRepository;

    @Autowired
    ElasticsearchTemplate template;

    @PostConstruct
    public void init() {
        for (int i = 0; i < 2; i++) {
            bulk(i);
        }
    }

    public void bulk(int ii) {
        try {
            // check if the index is existed
            if (!template.indexExists(Constants.EMPLOYEE_INDEX)) {
                template.createIndex(Constants.EMPLOYEE_INDEX);

                // 设置别名
                AliasQuery aliasQuery = new AliasQuery();
                aliasQuery.setIndexName(Constants.EMPLOYEE_INDEX);
                aliasQuery.setAliasName(Constants.EMPLOYEE_ALIAS);
                template.addAlias(aliasQuery);
            }
            ObjectMapper mapper = new ObjectMapper();
            List<IndexQuery> queries = new ArrayList<>();
            List<Employee> employees = rndEmployees();
            for (Employee employee : employees) {
                IndexQuery indexQuery = new IndexQuery();
                indexQuery.setId(employee.getId().toString());
                indexQuery.setSource(mapper.writeValueAsString(employee));
                //Set the index name & doc type
                indexQuery.setIndexName(Constants.EMPLOYEE_INDEX);
                indexQuery.setType(Constants.INDEX_TYPE);
                queries.add(indexQuery);
            }
            if (queries.size() > 0) {
                template.bulkIndex(queries);
            }
            template.refresh(Constants.EMPLOYEE_INDEX);
            LOGGER.info("BulkIndex completed: {}", ii);
        } catch (Exception e) {
            LOGGER.error("Error bulk index", e);
        }

//        try {
//            // check if the index is existed
//            if (!template.indexExists(Constants.VIDEO_INDEX)) {
//                template.createIndex(Constants.VIDEO_INDEX);
//            }
//            ObjectMapper mapper = new ObjectMapper();
//            List<IndexQuery> queries = new ArrayList<>();
//            List<Video> videos = rndVideos();
//            for (Video video : videos) {
//                IndexQuery indexQuery = new IndexQuery();
//                indexQuery.setId(video.getId().toString());
//                indexQuery.setSource(mapper.writeValueAsString(video));
//                //Set the index name & doc type
//                indexQuery.setIndexName(Constants.VIDEO_INDEX);
//                indexQuery.setType(Constants.INDEX_TYPE);
//                queries.add(indexQuery);
//            }
//            if (queries.size() > 0) {
//                template.bulkIndex(queries);
//            }
//            template.refresh(Constants.VIDEO_INDEX);
//            LOGGER.info("BulkIndex video completed: {}", ii);
//        } catch (Exception e) {
//            LOGGER.error("Error bulk video index", e);
//        }

//        try {
//            // check if the index is existed
//            if (!template.indexExists(Constants.TB_VIDEO_INDEX)) {
//                template.createIndex(Constants.TB_VIDEO_INDEX);
//            }
//            ObjectMapper mapper = new ObjectMapper();
//            List<IndexQuery> queries = new ArrayList<>();
//            List<TbVideo> videos = rndTbVideos();
//            for (TbVideo video : videos) {
//                IndexQuery indexQuery = new IndexQuery();
//                indexQuery.setId(video.getId().toString());
//                indexQuery.setSource(mapper.writeValueAsString(video));
//                //Set the index name & doc type
//                indexQuery.setIndexName(Constants.TB_VIDEO_INDEX);
//                indexQuery.setType(Constants.INDEX_DOC_TYPE);
//                queries.add(indexQuery);
//            }
//            if (queries.size() > 0) {
//                template.bulkIndex(queries);
//            }
//            template.refresh(Constants.TB_VIDEO_INDEX);
//            LOGGER.info("BulkIndex video completed: {}", ii);
//        } catch (Exception e) {
//            LOGGER.error("Error bulk video index", e);
//        }
    }

    private List<Employee> rndEmployees(){
        List<Employee> employees = new ArrayList<>();
        int id = (int) employeeRepository.count();
        LOGGER.info("Starting from id: {}", id);
        for (int i = id; i < 100 + id; i++) {
            Random r = new Random();
            Faker faker = new Faker();
            Employee employee = new Employee();
            employee.setId((long) i);
            employee.setName(faker.name().username());
            employee.setAge(r.nextInt(60));
            employee.setPosition(faker.job().position());
            int departmentId1 = r.nextInt(5000);
            int departmentId2 = r.nextInt(5000);
            List<Department> departmentList = new ArrayList<>();
            departmentList.add(new Department((long) departmentId1, faker.company().name()));
            departmentList.add(new Department((long) departmentId2, faker.company().name()));
            employee.setDepartment(departmentList);
            int organizationId = departmentId1 % 100;
            employee.setOrganization(new Organization((long) organizationId, "TestO" + organizationId, "Test Street No. " + organizationId));
            employees.add(employee);
        }
        return employees;
    }

    private List<Video> rndVideos(){
        List<Video> videos = new ArrayList<>();
        int id = (int) videoRepository.count();
        LOGGER.info("Starting from video id: {}", id);
        for (int i = id; i < 100 + id; i++) {
            Random r = new Random();
            Faker faker = new Faker(new Locale("zh-CN"));
            Video video = new Video();
            video.setId((long) i);
            video.setTitle(faker.name().title());
            video.setAddress(faker.address().fullAddress());
            video.setScore(faker.number().randomDigit());
            video.setCreateTime(new Date());
            videos.add(video);
        }
        return videos;
    }

    private List<TbVideo> rndTbVideos(){
        List<TbVideo> videos = new ArrayList<>();
        int id = (int) tbVideoRepository.count();
        LOGGER.info("Starting from tb_video id: {}", id);
        for (int i = id; i < 100 + id; i++) {
            Random r = new Random();
            Faker faker = new Faker(new Locale("zh-CN"));
            TbVideo video = new TbVideo();
            video.setId((long) i);
            video.setVideoTitle(faker.name().title());
            video.setScore((long) faker.number().randomDigit());
            video.setCreateTime(new Date());
            videos.add(video);
        }
        return videos;
    }

}
