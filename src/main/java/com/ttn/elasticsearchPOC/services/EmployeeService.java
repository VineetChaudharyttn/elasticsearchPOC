package com.ttn.elasticsearchPOC.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ttn.elasticsearchPOC.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
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

    public static final String INDEX_NAME = "bluebell";
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

    public List<Employee> searchByTitle() {
        QueryBuilder queryBuilder =
                QueryBuilders
                        .multiMatchQuery("Software Engineer", "title")
                        .fuzziness(Fuzziness.AUTO);

        SourceFilter sourceFilter = new FetchSourceFilter(new String[]{"code"}, null);
        Query searchQuery = new NativeSearchQueryBuilder()
                .withSourceFilter(sourceFilter)
                .withPageable(PageRequest.of(3, 30))
                .withFilter(queryBuilder)
                .build();
        List<Employee> employeeCode = new ArrayList<>();
        elasticsearchRestTemplate.search(searchQuery,
                Employee.class,
                IndexCoordinates.of(INDEX_NAME))
                .forEach(employeeSearchHit ->
                        employeeCode.add(employeeSearchHit.getContent())
                );
        return employeeCode;
    }

    public List<Employee> searchByName() {
        QueryBuilder queryBuilder = QueryBuilders
                .wildcardQuery("name", "Vineet" + "*");

        Query searchQuery = new NativeSearchQueryBuilder()
                .withFilter(queryBuilder)
                .withPageable(PageRequest.of(0, 5))
                .build();

        SearchHits<Employee> searchSuggestions =
                elasticsearchRestTemplate.search(searchQuery,
                        Employee.class,
                        IndexCoordinates.of(INDEX_NAME));

        List<Employee> employeeCode = new ArrayList<>();
        searchSuggestions.getSearchHits().forEach(searchHit -> {
            employeeCode.add(searchHit.getContent());
        });
        return employeeCode;
    }

    public void simpleQuery() {
        Criteria criteria = new Criteria("price")
                .greaterThan(10.0)
                .lessThan(100.0);

        Query searchQuery = new CriteriaQuery(criteria);

        SearchHits<Employee> products = elasticsearchRestTemplate
                .search(searchQuery,
                        Employee.class,
                        IndexCoordinates.of(INDEX_NAME));
    }
}
