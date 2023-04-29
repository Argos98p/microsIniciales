package com.turisup.resources.service;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.jena.SDJenaFactory;
import com.turisup.resources.model.PlaceResponse;
import com.turisup.resources.model.parser.Parser;
import com.turisup.resources.model.parser.QueryOptions;
import com.turisup.resources.repository.DBConnection;
import com.turisup.resources.repository.FBConnection;
import com.turisup.resources.repository.SparqlTemplates;
import com.turisup.resources.repository.StardogHttpQueryConn;
import com.turisup.resources.utils.Utils;
import org.apache.jena.geosparql.implementation.WKTLiteralFactory;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.util.*;

@Service
public class RutaService {

    StardogHttpQueryConn stardogHttpQueryConn;
    final String BASE="http://turis-ucuenca";
    final String tp ="http://tour-pedia.org/download/tp.owl";
    public  ArrayList<Map<String, String>> getRutasUser(String userId) {
        ArrayList<Map<String,String>> rutas = new ArrayList<>();
        try (Connection myConnection = DBConnection.createConnection()){
            Model myModel = SDJenaFactory.createModel(myConnection);
            String queryString = SparqlTemplates.rutasByUser(userId);
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query,myModel);
            try {
                ResultSet results= qexec.execSelect();
                while(results.hasNext()){
                    QuerySolution soln = results.nextSolution();
                    Map<String, String> ruta = new HashMap<>();
                    ruta.put("rutaId",soln.getResource("ruta").toString().replace("http://turis-ucuenca/ruta/",""));
                    ruta.put("nombre",soln.getLiteral("nombre").toString());
                    rutas.add(ruta);

                }
            }finally {
                qexec.close();
            }
        }finally {
        }
        return rutas;
    }

    public Map<String, Object> getOneRuta(String rutaId, String userId) {
        Map<String, Object> ruta = new HashMap<>();
        PlaceService placeService = new PlaceService();
        ArrayList<PlaceResponse> placesInRouteArray = new ArrayList<>();
        try (Connection myConnection = DBConnection.createConnection()){

            Model myModel = SDJenaFactory.createModel(myConnection);

            Resource oneRoute = myModel.getResource("http://turis-ucuenca/ruta/"+rutaId);
            ruta.put("id",rutaId);
            ruta.put("nombre", oneRoute.getProperty(DC.title).getObject().toString());
            ruta.put("descripcion", oneRoute.getProperty(DC.description).getObject().toString());
            ruta.put("creador", oneRoute.getProperty(DC.creator).getObject().toString().replace("http://turis-ucuenca/user/",""));
            Statement st = oneRoute.getProperty(myModel.createProperty("http://turis-ucuenca", "/hasPlaces"));

            Bag placesInRoute = myModel.getBag(st.getObject().asResource());
            NodeIterator iter = placesInRoute.iterator();
            while (iter.hasNext()) {
                RDFNode placeItem = iter.nextNode();
                String placeId = placeItem.toString().replace("http://turis-ucuenca/lugar/","");
                System.out.println(placeId);
                QueryOptions queryOptions = new QueryOptions();
                queryOptions.setUserId(userId);
                queryOptions.setLugarId(placeId);
                List<PlaceResponse> response = placeService.all(queryOptions);
                System.out.println(response);
                if(!response.isEmpty()){
                    PlaceResponse onePlace = response.get(0);
                    placesInRouteArray.add(onePlace);
                }
            }
            ruta.put("lugares",placesInRouteArray);
            System.out.println(placesInRoute);



            /*
            String queryString = SparqlTemplates.getRuta(rutaId);
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query,myModel);
            String node_id="";
            try {
                ResultSet results= qexec.execSelect();
                ArrayList<Map<String, Object>> placesInRoute = new ArrayList<>();
                while(results.hasNext()){
                    System.out.println("entra una vez");
                    QuerySolution soln = results.nextSolution();
                    ruta.put("id",rutaId);
                    ruta.put("nombre",soln.getLiteral("nombre").toString());
                    ruta.put("descripcion",soln.getLiteral("descripcion").toString());
                    ruta.put("creador",soln.getResource("creador").toString().replace("http://turis-ucuenca/user/",""));
                    node_id=soln.getResource("placeNode").toString();
                }
                if(!node_id.equals("")){
                    QueryOptions queryOptions = new QueryOptions();
                    queryOptions.setNodeForRouteBag(node_id);
                    queryOptions.setCreadorId(userId);

                    List<PlaceResponse> placeResponseList = new ArrayList<>();
                    try{
                        String getPlacesInRoute = SparqlTemplates.defaultQuery(queryOptions);
                        Query query3 = QueryFactory.create(getPlacesInRoute);
                        System.out.println(getPlacesInRoute);
                        QueryExecution qexec3 = QueryExecutionFactory.create(query3,myModel);
                        ResultSet results3= qexec3.execSelect();
                        PlaceResponse placeResponse = null;
                        int i = 0 ;
                        while(results3.hasNext()){
                            System.out.println(i);
                            QuerySolution soln3 = results3.nextSolution();

                            placeResponse = Parser.QueryResult2Place(soln3);
                            placeResponseList.add(placeResponse);
                            i++;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println(placeResponseList.size());
                    ruta.put("lugares",placeResponseList);


                }
            }finally {
                qexec.close();
            }*/


        }finally {
        }
        return ruta;
    }

    public Map<String, Object> agregarLugar(String rutaId, ArrayList<String> lugarId, String userId) {

        try (Connection myConnection = DBConnection.createConnection()) {
            Model myModel = SDJenaFactory.createModel(myConnection);
            myModel.begin();
            Resource myRoute = myModel.getResource("http://turis-ucuenca/ruta/"+rutaId);
            Statement st = myRoute.getProperty(myModel.createProperty(BASE,"/hasPlaces"));
            Seq placesSequ = st.getSeq();
            for(int i=0;i<lugarId.size();i++){
                placesSequ.add(myModel.getResource("http://turis-ucuenca/lugar/"+lugarId.get(i)));
            }


            myModel.commit();
        }
        return getOneRuta(rutaId, userId);
    }

    public Map<String, Object> eliminarLugar(String rutaId, String lugarId, String userId) {

        try (Connection myConnection = DBConnection.createConnection()) {
            Model myModel = SDJenaFactory.createModel(myConnection);
            String queryString = SparqlTemplates.eliminarLugarEnRuta(rutaId,lugarId);
            stardogHttpQueryConn = new StardogHttpQueryConn();
            stardogHttpQueryConn.PostToTriplestore(queryString);
        }
        return getOneRuta(rutaId, userId);

    }


    public ArrayList<Map<String, String>> removeRuta(String userId, String rutaId) {
        String queryString = SparqlTemplates.eliminarRuta(rutaId);
        stardogHttpQueryConn = new StardogHttpQueryConn();
        stardogHttpQueryConn.PostToTriplestore(queryString);
        return getRutasUser(userId);
    }
}
