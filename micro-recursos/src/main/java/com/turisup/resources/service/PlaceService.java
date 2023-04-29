package com.turisup.resources.service;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.jena.SDJenaFactory;
import com.turisup.resources.model.PlacePoint;
import com.turisup.resources.model.PlaceResponse;
import com.turisup.resources.model.parser.Parser;
import com.turisup.resources.model.parser.QueryOptions;
import com.turisup.resources.model.request.get.InteractionModel;
import com.turisup.resources.model.request.post.AddRoute;
import com.turisup.resources.repository.FBConnection;
import com.turisup.resources.repository.SparqlTemplates;
import com.turisup.resources.utils.Utils;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.jena.geosparql.implementation.WKTLiteralFactory;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import com.turisup.resources.model.Place;
import com.turisup.resources.model.request.post.PlaceRequest;
import com.turisup.resources.repository.DBConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PlaceService {

     final String vcard="http://www.w3.org/2006/vcard/ns";
     final String geo="http://www.opengis.net/ont/geosparql#";
     final String BASE="http://turis-ucuenca";
     final String tp ="http://tour-pedia.org/download/tp.owl";
     YoutubeService youtubeService = new YoutubeService();

     @Autowired
     FileStorageService fileStorageService;
     public Place save(PlaceRequest pr, MultipartFile[] files) throws IOException {

          Place newPlace = new Place(UUID.randomUUID().toString(),pr.getNombre(),new PlacePoint(pr.getLatitud(),pr.getLongitud()), pr.getDescripcion(), pr.getUsuarioId());
          if(pr.getCategoria()==null){
               newPlace.setCategoria("sin categoria");
          }else{
               newPlace.setCategoria(pr.getCategoria());
          }

          ArrayList<String> imageIds = new ArrayList<>();
          ArrayList<String> urlImages = new ArrayList<>();
          ArrayList<String> videoIds = new ArrayList<>();



          for (MultipartFile file : files) {
               String routeFile = fileStorageService.storeFile(file, newPlace.getId());
               String checkFile = Utils.getTypeOfFile(routeFile);
               if(checkFile.equals("isImage")){
                    String imageId = FacebookService.UploadImage(routeFile);
                    imageIds.add(imageId);
                    String urlImage = FacebookService.urlImageByIdImage(imageId);
                    urlImages.add(urlImage);
               }else if(checkFile.equals("isVideo")){
                    String response = FacebookService.UploadVideo(routeFile, newPlace.getId());
                    videoIds.add(response);
               }
          }


          try (Connection myConnection = DBConnection.createConnection()){

               Model myModel = SDJenaFactory.createModel(myConnection);
               myModel.begin();

               Bag facebookImageIds = myModel.createBag();
               Bag facebookVideoIds = myModel.createBag();
               Resource placeModel = myModel.createResource("http://turis-ucuenca/lugar/"+newPlace.getId());
               placeModel.addProperty(RDF.type,myModel.createProperty(tp,"POI"));
               placeModel.addProperty(RDF.type, OWL2.NamedIndividual);
               placeModel.addProperty(DC.title, newPlace.getNombre());
               placeModel.addProperty(RDFS.label,newPlace.getNombre());
               placeModel.addProperty(DC.description,newPlace.getDescripcion());
               placeModel.addProperty(DC.date,  myModel.createTypedLiteral(ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT ), "http://www.w3.org/2001/XMLSchema#dateTime"));
               placeModel.addProperty(myModel.createProperty(BASE,"#status"),"revisar");
               placeModel.addProperty(myModel.createProperty(BASE,"#category"), newPlace.getCategoria());
               //La siguiente propiedad indica que el recurso no es valido, es para borrar despues de la ontologia
               placeModel.addProperty(myModel.createProperty(BASE,"#isValid"),myModel.createTypedLiteral(Boolean.FALSE));
               placeModel.addProperty(myModel.createProperty(BASE,"#facebookId"),facebookImageIds);
               placeModel.addProperty(myModel.createProperty(BASE,"#facebookVideoId"),facebookVideoIds);
               placeModel.addProperty(DC.creator,myModel.createResource(BASE+"/user/"+newPlace.getUsuarioId()));
               //ArrayList<ArrayList<String>> srcAndIds = FBConnection.ToFacebook(imagesPaths);
               newPlace.setImagenesPaths(urlImages);
               newPlace.setFbImagenesIds(imageIds);
               newPlace.setFbVideoIds(videoIds);

               for (int i=0; i< imageIds.size();i++){
                    placeModel.addProperty(VCARD4.hasPhoto,(newPlace.getImagenesPaths().get(i)));
                    facebookImageIds.add(newPlace.getFbImagenesIds().get(i));
               }
               for(int i=0; i< videoIds.size();i++){
                    facebookVideoIds.add(newPlace.getFbVideoIds().get(i));
               }

               placeModel.addProperty(Geo.HAS_GEOMETRY_PROP,myModel.createResource()
                       .addProperty(RDF.type, myModel.createResource(geo+"Geometry"))
                       .addProperty(Geo.AS_WKT_PROP, WKTLiteralFactory.createPoint(newPlace.getCoordenadas().getLongitud(),newPlace.getCoordenadas().getLatitud())));
               myModel.commit();
          }finally {
               fileStorageService.deleteImages(newPlace.getId());
          }
          return newPlace;
     }

     public PlaceResponse get(String placeId){
          PlaceResponse myNewPlace=new PlaceResponse();
          try(Connection myConnection = DBConnection.createConnection()){
               Model myModel = SDJenaFactory.createModel(myConnection);
               String queryString = SparqlTemplates.getPlace(placeId);
               Query query = QueryFactory.create(queryString);
               QueryExecution qexec = QueryExecutionFactory.create(query,myModel);
               try {
                    ResultSet results= qexec.execSelect();
                    while(results.hasNext()){
                         QuerySolution soln = results.nextSolution();
                         myNewPlace= Parser.QueryResult2Place(soln);
                         myNewPlace.setId(placeId);
                    }
               }finally {
                    qexec.close();
               }
          }
          return myNewPlace;
     }

     public List<PlaceResponse> all ( QueryOptions queryOptions ){
          List<PlaceResponse> places = new ArrayList<>();
          try(Connection myConnection = DBConnection.createConnection()){
               Model myModel = SDJenaFactory.createModel(myConnection);
               String queryString = SparqlTemplates.defaultQuery(queryOptions);
               System.out.println(queryString);

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
          }
          return places;
     }

     public String addRoute(AddRoute nuevaRutaInfo) {
          try (Connection myConnection = DBConnection.createConnection()){
               Model myModel = SDJenaFactory.createModel(myConnection);
               myModel.begin();
               String rutaId=UUID.randomUUID().toString();
               Resource rutaResource = myModel.createResource("http://turis-ucuenca/ruta/"+rutaId);
               rutaResource.addProperty(RDF.type,myModel.createProperty(BASE,"/Route"));
               rutaResource.addProperty(RDF.type, OWL2.NamedIndividual);
               rutaResource.addProperty(DC.title, nuevaRutaInfo.getNombre());
               rutaResource.addProperty(RDFS.label,nuevaRutaInfo.getNombre());
               rutaResource.addProperty(DC.date,  myModel.createTypedLiteral(ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT ), "http://www.w3.org/2001/XMLSchema#dateTime"));
               rutaResource.addProperty(DC.description, nuevaRutaInfo.getDescripcion());
               rutaResource.addProperty(DC.creator,myModel.createResource(BASE+"/user/"+nuevaRutaInfo.getUserId()));
               Seq places = myModel.createSeq();
               for (int i = 0 ;i < nuevaRutaInfo.getLugares().size();i++){
                    places.add(i+1,myModel.createResource("http://turis-ucuenca/lugar/"+nuevaRutaInfo.getLugares().get(i)));
               }
               rutaResource.addProperty(myModel.createProperty(BASE,"/hasPlaces"),places);
               Resource userResource = myModel.createResource(BASE+"/user/"+nuevaRutaInfo.getUserId());
               userResource.addProperty(myModel.createProperty(BASE,"/hasRoute"),rutaResource);
               myModel.commit();
               return rutaId;
          }finally {
          }
     }


     public ArrayList<InteractionModel> getInteractions(String fechaInicio, String fechaFin, String userId) {
          ArrayList<InteractionModel> userInteractions = new ArrayList<>();
          try(Connection myConnection = DBConnection.createConnection()){
               Model myModel = SDJenaFactory.createModel(myConnection);
               String queryString = SparqlTemplates.getInteractions(fechaInicio,fechaFin, userId);

               Query query = QueryFactory.create(queryString);
               QueryExecution qexec = QueryExecutionFactory.create(query,myModel);
               try {
                    ResultSet results= qexec.execSelect();
                    while(results.hasNext()){
                         QuerySolution soln = results.nextSolution();
                         if(soln.getLiteral("date")!=null){
                              InteractionModel interactionModel = new InteractionModel(
                                      ZonedDateTime.parse(soln.getLiteral("date").getValue().toString()),
                                      soln.getLiteral("rating").getInt(),
                                      soln.getLiteral("contenido").getString(),
                                      soln.getLiteral("commentId").getString(),
                                      soln.getLiteral("placeId").getString()
                              );

                              PlaceResponse placeResponse = null;
                              String queryString2 = SparqlTemplates.getRecursoById(interactionModel.getPlaceId(), userId);
                              Query query2 = QueryFactory.create(queryString2);
                              QueryExecution qexec2 = QueryExecutionFactory.create(query2,myModel);
                              try{
                                   ResultSet results2= qexec2.execSelect();
                                   while(results2.hasNext()){
                                        QuerySolution soln2 = results2.nextSolution();

                                        placeResponse = Parser.QueryResult2Place(soln2);
                                   }
                              } catch (Exception e) {
                                   throw new RuntimeException(e);
                              }


                              if(placeResponse!=null){
                                   interactionModel.setPlace(placeResponse);
                              }
                              userInteractions.add(interactionModel);
                         }
                    }
               }finally {
                    qexec.close();
               }
          }
          return  userInteractions;
     }
}




