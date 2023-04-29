package com.turisup.resources.service;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.jena.SDJenaFactory;
import com.turisup.resources.model.PlaceResponse;
import com.turisup.resources.model.parser.Parser;
import com.turisup.resources.repository.DBConnection;
import com.turisup.resources.repository.SparqlTemplates;
import com.turisup.resources.repository.StardogHttpQueryConn;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class FavoriteService {
    final String BASE="http://turis-ucuenca";
    StardogHttpQueryConn stardogHttpQueryConn;
    public HashMap<String, Object> insertFavorite(String userId, String placeId) {
        try (Connection myConnection = DBConnection.createConnection()){
            Model myModel = SDJenaFactory.createModel(myConnection);
            myModel.begin();
            Resource userResource = myModel.getResource("http://turis-ucuenca/user/"+userId);
            Resource placeResource = myModel.getResource("http://turis-ucuenca/lugar/"+placeId);
            //myModel.remove(userResource,myModel.createProperty(BASE,"#favorite"),placeResource);
            //myModel.remove(placeResource,(myModel.createProperty(BASE,"#isFavoriteOf")),userResource);

            if(userResource.getProperty(FOAF.name) == null){
                return new HashMap<>(){{
                    put("error","Usuario no encontrado");
                }};
            }
            if(placeResource.getProperty(DC.title)== null){
                return new HashMap<>(){{
                    put("error","Lugar no encontrado");
                }};
            }

            placeResource.addProperty(myModel.createProperty(BASE,"#isFavoriteOf"),userResource);
            userResource.addProperty(myModel.createProperty(BASE,"#favorite"),placeResource);

            myModel.commit();

            return new HashMap<String, Object>() {{
                put("ok","ok" );
            }};
        }finally {
        }


    }

    public HashMap<String, String> removeFavorite(String userId, String placeId){
        try (Connection myConnection = DBConnection.createConnection()){
            Model myModel = SDJenaFactory.createModel(myConnection);
            myModel.begin();
            Resource userResource = myModel.getResource("http://turis-ucuenca/user/"+userId);
            Resource placeResource = myModel.getResource("http://turis-ucuenca/lugar/"+placeId);


            if(userResource.getProperty(FOAF.name) == null){
                return new HashMap<>(){{
                    put("error","Usuario no encontrado");
                }};
            }
            if(placeResource.getProperty(DC.title)== null){
                return new HashMap<>(){{
                    put("error","Lugar no encontrado");
                }};
            }

            myModel.remove(userResource,myModel.createProperty(BASE,"#favorite"),placeResource);
            myModel.remove(placeResource,(myModel.createProperty(BASE,"#isFavoriteOf")),userResource);

            myModel.commit();

            return new HashMap<String, String>() {{
                put("ok","ok" );


            }};
        }finally {
        }
    }

    public HashMap<String, Object> getFavorites(String userId) {
        List<PlaceResponse> places = new ArrayList<>();
        try (Connection myConnection = DBConnection.createConnection()) {
            Model myModel = SDJenaFactory.createModel(myConnection);
            myModel.begin();
            Resource userResource = myModel.getResource("http://turis-ucuenca/user/" + userId);
            if (userResource.getProperty(FOAF.name) == null) {
                return new HashMap<>() {{
                    put("error", "Usuario no encontrado");
                }};
            }

            String queryString = SparqlTemplates.getFavorites(userId);

            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query,myModel);
            try {
                ResultSet results= qexec.execSelect();
                while(results.hasNext()){
                    QuerySolution soln = results.nextSolution();
                    PlaceResponse placeResponse = Parser.QueryResult2Place(soln);
                    if(placeResponse!=null){
                        places.add(placeResponse);
                    }
                }
            }finally {
                qexec.close();
            }
        } finally {
        }
        return new HashMap<>() {{
            put("places", places);
        }};
    }

}
