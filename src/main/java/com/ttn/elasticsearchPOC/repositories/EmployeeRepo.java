package com.ttn.elasticsearchPOC.repositories;

import com.ttn.elasticsearchPOC.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;

import java.util.List;

public interface EmployeeRepo /*extends ElasticsearchRepository<Employee, String>*/ {

    @Query("{ \"match_all\": {}}")
    List<Employee> searchQuery();

    Page<Employee> findByTitle(String name, Pageable pageable);

    Page<Employee> findByTitleContaining(String name, Pageable pageable);

    Page<Employee> findByCompetency(String name, Pageable pageable);

    Page<Employee> findByName(String name, Pageable pageable);

//    @Query("{\"range\": {\"authors.age\": {\"gte\": \"?0\", \"lte\": \"?1\"}}}")
//    Page<Employee> findByAuthorsAgeInRange(int minAge, int maxAge, Pageable pageable);

    @Query("{\"multi_match\" : {\"query\": \"?0\", \"fields\": [ \"id\", \"title\", \"publishedDate\", \"description\", \"categories\", \"authors.id\", \"authors.name\", \"authors.age\"]}}")
    Page<Employee> searchOnAllFields(String query, Pageable pageable);
}
