package com.springboot.ecommerce.config;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.springboot.ecommerce.entity.Country;
import com.springboot.ecommerce.entity.Order;
import com.springboot.ecommerce.entity.Product;
import com.springboot.ecommerce.entity.ProductCategory;
import com.springboot.ecommerce.entity.State;


@Configuration
public class MyDataRestConfig implements RepositoryRestConfigurer {

    @Value("${allowed.origins}")
   private String[] theAllowedOrigins;
   
    private EntityManager entityManager;

    @Autowired
    public MyDataRestConfig(EntityManager thEntityManager) {
        entityManager =thEntityManager;

    }

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {

final HttpMethod[] theUnsupportedActions = {HttpMethod.PUT, HttpMethod.POST, 
                                       HttpMethod.DELETE, HttpMethod.PATCH};

//disable HTTP methods for ProductCategory: PUT, POST and DELETE
disableHttpMethods( Product.class,config, theUnsupportedActions);
disableHttpMethods( ProductCategory.class,config, theUnsupportedActions);
disableHttpMethods( Country.class,config, theUnsupportedActions);
disableHttpMethods( State.class,config, theUnsupportedActions);
disableHttpMethods( Order.class,config, theUnsupportedActions);


//call an internal helper method
exposedIds(config);

//configure cors mapping

cors.addMapping(config.getBasePath() + "/**").allowedOrigins(theAllowedOrigins);



    }

    private void disableHttpMethods(Class theClass, RepositoryRestConfiguration config, HttpMethod[] theUnsupportedActions) {
        config.getExposureConfiguration() 
        .forDomainType(theClass) 
        .withItemExposure((metadata, httpMethods) -> httpMethods.disable(theUnsupportedActions))
        .withCollectionExposure((metadata, httpMethods) -> httpMethods.disable(theUnsupportedActions));
    }

    private void exposedIds(RepositoryRestConfiguration config) {

//expose entity ids
//
//- get a list of all entity classes from the entity manager
    Set<EntityType<?>>  entities = entityManager.getMetamodel().getEntities();

    //create an array of the entity type
    List<Class> entityClasses = new ArrayList<>();

    //get the entity types for the entities
    for(EntityType temEntityType : entities) {
        entityClasses.add(temEntityType.getJavaType());
    }

    //expose the entity the ids for the array of entity/domain type

    Class[] domainTypes = entityClasses.toArray(new Class[0]);
    config.exposeIdsFor(domainTypes);

    }
    
}
