package com.ttn.elasticsearchPOC.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ttn.elasticsearchPOC.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EmployeeService {

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    Gson gson;
    /*@Autowired
    EmployeeRepo employeeRepo;*/

    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndex() {
        log.info("Setuping initial data");
        Type type = new TypeToken<ArrayList<Employee>>() {
        }.getType();
        try (Reader reader = Files.newBufferedReader(Paths.get("src", "main", "resources", "EmployeeData.json"))) {
            List<Employee> employees = gson.fromJson(reader, type);
            List<IndexQuery> queries = new ArrayList<>();
            IndexCoordinates indexCoordinates = elasticsearchRestTemplate.getIndexCoordinatesFor(Employee.class);

            for (Employee employee : employees) {
                IndexQuery indexQuery = new IndexQuery();
                indexQuery.setId(String.valueOf(employee.getId()));
                indexQuery.setObject(employee);
                queries.add(indexQuery);
            }
            elasticsearchRestTemplate.bulkIndex(queries, indexCoordinates);
        } catch (IOException e) {
            log.error("Error in reading json file: {}", "EmployeeData.json", e);
        }
    }

    public Employee getEmployeeById(Long id) {
        Criteria criteria = new Criteria("id").is(id.toString());
        Query query = new CriteriaQuery(criteria);
        return (Employee) elasticsearchRestTemplate.search(query, Employee.class);
    }

    public SearchHits<Employee> getEmployee() {
        return elasticsearchRestTemplate.search(Query.findAll(), Employee.class);
    }
}
