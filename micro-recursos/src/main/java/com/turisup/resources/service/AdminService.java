package com.turisup.resources.service;

import com.turisup.resources.utils.Utils;
import org.apache.jena.geosparql.implementation.WKTLiteralFactory;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.jena.SDJenaFactory;
import com.turisup.resources.model.Organization;
import com.turisup.resources.model.Place;
import com.turisup.resources.model.PlaceResponse;
import com.turisup.resources.model.Region;
import com.turisup.resources.model.admin.AdminUserResource;
import com.turisup.resources.model.parser.Parser;
import com.turisup.resources.model.parser.QueryOptions;
import com.turisup.resources.model.request.get.AdminPlaceRequest;
import com.turisup.resources.model.request.get.AdminUser;
import com.turisup.resources.model.request.post.PlaceRequest;
import com.turisup.resources.model.request.post.UpdatePlaceRequest;
import com.turisup.resources.repository.DBConnection;
import com.turisup.resources.repository.SparqlTemplates;
import com.turisup.resources.repository.StardogHttpQueryConn;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.VCARD4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.turisup.resources.repository.FBConnection.getImagesSrcById;
import static com.turisup.resources.repository.SparqlTemplates.updateImagesUrls;
import static com.turisup.resources.repository.SparqlTemplates.updatePlaceQuery;

@Service
public class AdminService {
    @Autowired
    PlaceService placeService;
    @Autowired
    FileStorageService fileStorageService;
    StardogHttpQueryConn stardogHttpQueryConn;
    final String vcard="http://www.w3.org/2006/vcard/ns";
    final String geo="http://www.opengis.net/ont/geosparql#";
    final String BASE="http://turis-ucuenca";
    final String tp ="http://tour-pedia.org/download/tp.owl";

    public List<Place> placesByRegion(AdminPlaceRequest adminPlaceRequest) {
        List<Place> places = new ArrayList<>();

        try (Connection myConnection = DBConnection.createConnection()) {
            Model myModel = SDJenaFactory.createModel(myConnection);


            String queryString = SparqlTemplates.getPlacesByRegionOrgStatusUser(adminPlaceRequest);

            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, myModel);
            try {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    //myNewPlace= Parser.QueryResult2Place(soln,true);
                    // myNewPlace.setId(placeId);
                }
            } finally {
                qexec.close();
            }
        }
        return places;
    }


    public ResponseEntity<?> updatePlace(PlaceRequest placeUpdateInfo) {
        String query = updatePlaceQuery(placeUpdateInfo);
        query= query.replaceAll("\n"," ");
        stardogHttpQueryConn = new StardogHttpQueryConn();
        return stardogHttpQueryConn.PostToTriplestore(query);
    }

    public List<AdminUserResource> userAdminResources(String userId) {
        List<AdminUserResource> resources = new ArrayList<>();
        try(Connection myConnection = DBConnection.createConnection()){
            Model myModel = SDJenaFactory.createModel(myConnection);


            String queryString = SparqlTemplates.getUserAdminResources(userId);
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query,myModel);
            try {
                ResultSet results= qexec.execSelect();
                while(results.hasNext()){
                    QuerySolution soln = results.nextSolution();
                    Organization myOrg = new Organization(soln.getResource("org").getLocalName(),soln.getLiteral("orgTitle").toString());
                    Region myRegion= new Region(soln.getResource("region").getLocalName(),soln.getLiteral("regionTitle").toString());
                    resources.add(new AdminUserResource(myOrg,myRegion));
                }
            }finally {
                qexec.close();
            }
        }
        return  resources;
    }

    public ResponseEntity<?> actualizarImagenesFB() {
        QueryOptions queryOptions = new QueryOptions();
        List<PlaceResponse> places = placeService.all(queryOptions);

        for(PlaceResponse placeResponse: places){

            List<String> imagesUrls = getImagesSrcById(placeResponse.getFbImagenesIds());

            String queryUpdate = updateImagesUrls(imagesUrls,placeResponse.getId());

            stardogHttpQueryConn = new StardogHttpQueryConn();
            stardogHttpQueryConn.PostToTriplestore(queryUpdate);
        }


        return new ResponseEntity<>(HttpStatus.OK);
    }


    public List<AdminUser> getUsersInOrg(String orgId) {
        List<AdminUser> usuarios = new ArrayList<>();
        try(Connection myConnection = DBConnection.createConnection()){
            Model myModel = SDJenaFactory.createModel(myConnection);


            String queryString = SparqlTemplates.usersInOrg(orgId);
            System.out.println(queryString);
            Query query = QueryFactory.create(queryString);

            QueryExecution qexec = QueryExecutionFactory.create(query,myModel);
            try {
                ResultSet results= qexec.execSelect();
                while(results.hasNext()){
                    QuerySolution soln = results.nextSolution();
                    AdminUser user = new AdminUser(soln.getLiteral("nombre").toString(),soln.getLiteral("nick").toString(),soln.getLiteral("correoM").toString(),soln.getLiteral("userM").toString());
                    usuarios.add(user);
                }
            }finally {
                qexec.close();
            }
        }
        return usuarios;
    }

    public Boolean updatePlacefromWeb(UpdatePlaceRequest placeUpdateRequest, MultipartFile[] files) {




        try (Connection myConnection = DBConnection.createConnection()) {
            Model myModel = SDJenaFactory.createModel(myConnection);
            Resource placeResource = myModel.getResource(BASE + "/lugar/" + placeUpdateRequest.getPlaceid());
            System.out.println(placeUpdateRequest.getPlaceid());
            myModel.begin();

            if(placeResource==null)
                return false;

            if(files!=null){
                for (MultipartFile file : files) {
                    String routeFile = fileStorageService.storeFile(file, placeUpdateRequest.getPlaceid());
                    String checkFile = Utils.getTypeOfFile(routeFile);
                    if(checkFile.equals("isImage")){

                        String imageId = FacebookService.UploadImage(routeFile);
                        System.out.println(imageId);
                        placeUpdateRequest.getFbImagesId().add(imageId);

                    }else if(checkFile.equals("isVideo")){
                        String response = FacebookService.UploadVideo(routeFile, placeUpdateRequest.getPlaceid());
                        placeUpdateRequest.getFbVideoId().add(response);
                    }
                }
            }

            Statement titleSt = placeResource.getProperty(DC.title);
            titleSt.changeObject(placeUpdateRequest.getNombre());
            Statement title2St = placeResource.getProperty(RDFS.label);
            title2St.changeObject(placeUpdateRequest.getNombre());
            Statement categorySt = placeResource.getProperty(myModel.getProperty(BASE,"#category"));
            categorySt.changeObject(placeUpdateRequest.getCategoria());
            Statement descriptionSt = placeResource.getProperty(DC.description);
            descriptionSt.changeObject(placeUpdateRequest.getDescripcion());

            Statement nodeGeometry = placeResource.getProperty(myModel.getProperty("http://www.opengis.net/ont/geosparql","#hasGeometry"));
            Resource geometryObject = nodeGeometry.getObject().asResource();
            Statement geometrySt = geometryObject.getProperty(myModel.getProperty("http://www.opengis.net/ont/geosparql","#asWKT"));
            geometrySt.changeObject(WKTLiteralFactory.createPoint(placeUpdateRequest.getLongitud(),placeUpdateRequest.getLatitud()));

            //Borro las url de las imagenes
            StmtIterator nameIterator = placeResource.listProperties(VCARD4.hasPhoto);
            while (nameIterator.hasNext()) {
                Statement s = nameIterator.nextStatement();
                myModel.remove(s);
                System.out.println(s);
            }

            //Borro los contenedores previamente creado (no los actualizo xq no hay metodo para eliminar)
            Bag fbImagesId = placeResource.getProperty(myModel.getProperty(BASE,"#facebookId")).getBag();
            myModel.remove(placeResource,myModel.getProperty(BASE,"#facebookId"),fbImagesId);


                Bag fbVideoId = placeResource.getProperty(myModel.getProperty(BASE,"#facebookVideoId")).getBag();
                myModel.remove(placeResource,myModel.getProperty(BASE,"#fbVideoId"),fbVideoId);


            //Creo nuevamente los contenedores
            Bag facebookImageIds = myModel.createBag();
            Bag facebookVideoIds = myModel.createBag();
            placeResource.addProperty(myModel.createProperty(BASE,"#facebookId"),facebookImageIds);
            placeResource.addProperty(myModel.createProperty(BASE,"#facebookVideoId"),facebookVideoIds);

            for (int i=0; i< placeUpdateRequest.getFbVideoId().size();i++){
                facebookVideoIds.add(placeUpdateRequest.getFbVideoId().get(i));
            }
            for(int i=0; i< placeUpdateRequest.getFbImagesId().size();i++){
                System.out.println("debe entrar");
                placeResource.addProperty(VCARD4.hasPhoto,(FacebookService.urlImageByIdImage(placeUpdateRequest.getFbImagesId().get(i))));
                facebookImageIds.add(placeUpdateRequest.getFbImagesId().get(i));
            }

            myModel.commit();
            myModel.close();
            return  true;


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}