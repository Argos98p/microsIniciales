package com.turisup.resources.service;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.jena.SDJenaFactory;
import com.complexible.stardog.plan.filter.functions.rdfterm.Str;
import com.turisup.resources.model.parser.Parser;
import com.turisup.resources.model.request.post.AddComment;
import com.turisup.resources.repository.DBConnection;
import com.turisup.resources.repository.FBConnection;
import com.turisup.resources.repository.SparqlTemplates;
import com.turisup.resources.repository.StardogHttpQueryConn;
import com.turisup.resources.utils.Utils;
import org.apache.jena.geosparql.implementation.WKTLiteralFactory;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.turisup.resources.repository.SparqlTemplates.deleteComent;

@Service
public class CommentService {
    @Autowired
    FileStorageService fileStorageService;
    StardogHttpQueryConn stardogHttpQueryConn;
    final String vcard="http://www.w3.org/2006/vcard/ns";
    final String geo="http://www.opengis.net/ont/geosparql#";
    final String BASE="http://turis-ucuenca";
    final String tp ="http://tour-pedia.org/download/tp.owl";
    public HashMap<String, Object> save(AddComment newComment, MultipartFile[] files) throws IOException {
        try (Connection myConnection = DBConnection.createConnection()){
            Model myModel = SDJenaFactory.createModel(myConnection);
            myModel.begin();


            Bag facebookIds = myModel.createBag();
            Bag facebookVideoIds = myModel.createBag();
            Resource userId = myModel.getResource("http://turis-ucuenca/user/"+newComment.getUserId());
            Resource placeResource = myModel.getResource("http://turis-ucuenca/lugar/"+newComment.getLugarId());



            if(userId.getProperty(FOAF.name) == null){
                return new HashMap<>(){{
                    put("error","Usuario no encontrado");
                }};
            }
            if(placeResource.getProperty(DC.title)== null){
                return new HashMap<>(){{
                    put("error","Lugar no encontrado");
                }};
            }
            if(files == null){
                files = new MultipartFile[]{};
            }
            ArrayList<String> imagesUrl= new ArrayList<>();
            ArrayList<String> imageIds= new ArrayList<>();
            ArrayList<String> videoIds = new ArrayList<>();

            for (MultipartFile file : files) {
                String routeFile = fileStorageService.storeFile(file,  newComment.getLugarId());
                String checkFile = Utils.getTypeOfFile(routeFile);
                if(checkFile.equals("isImage")){
                    String imageId = FacebookService.UploadImage(routeFile);
                    imageIds.add(imageId);
                    String urlImage = FacebookService.urlImageByIdImage(imageId);
                    imagesUrl.add(urlImage);
                }else if(checkFile.equals("isVideo")){
                    String response = FacebookService.UploadVideo(routeFile, newComment.getLugarId());
                    videoIds.add(response);
                }
            }

            String comentarioId = UUID.randomUUID().toString();
            Resource commentResource = myModel.createResource("http://turis-ucuenca/comentario/"+comentarioId );
            commentResource.addProperty(RDF.type, myModel.createResource("http://turis-ucuenca/Comentario"));
            commentResource.addProperty(myModel.createProperty(BASE,"#place"),placeResource);
            commentResource.addLiteral(myModel.createProperty(BASE,"#text"), newComment.getComentario());
            commentResource.addLiteral(myModel.createProperty(BASE,"#rating"),newComment.getPuntaje());
            commentResource.addProperty(DC.date,  myModel.createTypedLiteral(ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT ), "http://www.w3.org/2001/XMLSchema#dateTime"));

            if(imagesUrl.size()>0){
                commentResource.addProperty(myModel.createProperty(BASE,"#facebookImageId"),facebookIds);
                for (int i=0; i<imagesUrl.size();i++){
                    commentResource.addProperty(VCARD4.hasPhoto,(imagesUrl.get(i)));
                    facebookIds.add(imageIds.get(i));
                }
            }
            for (String videoId : videoIds) {
                facebookVideoIds.add(videoId);
            }
            commentResource.addProperty(myModel.createProperty(BASE,"#facebookVideoId"),facebookVideoIds);
            placeResource.addProperty(myModel.createProperty(BASE,"#hasComment"),commentResource);
            userId.addProperty(myModel.createProperty(BASE,"#comment"),commentResource);
            userId.addProperty(myModel.createProperty(BASE,"#visit"), placeResource);
            placeResource.addProperty(myModel.createProperty(BASE,"#wasVisited"),userId);

            userId.getProperty(FOAF.name);
            userId.getProperty(FOAF.depiction);
            myModel.commit();


            return new HashMap<String, Object>() {{
                put("id",comentarioId );
                put("comentario", newComment.getComentario());
                put("puntaje",newComment.getPuntaje());
                put("imagenes",imagesUrl);
                put("videos",videoIds);
                put("user", new HashMap<String,String>(){{
                    put("nombre",userId.getProperty(FOAF.name).getObject().toString());
                    put("imagen",userId.getProperty(FOAF.depiction).getObject().toString());
                }});

            }};
        }finally {
        }
    }

    public ArrayList<Map<String, Object>> getByPlaces(String lugarId) {
            ArrayList<Map<String,Object>> comentarios=new ArrayList<>();
        try (Connection myConnection = DBConnection.createConnection()) {
            Model myModel = SDJenaFactory.createModel(myConnection);
            Resource lugarResource = myModel.getResource("http://turis-ucuenca/lugar/"+lugarId );
            if(lugarResource.getProperty(DC.title)==null){
                comentarios.add(new HashMap<>() {{put("error","No se encontro el lugar");}});
                return comentarios;
            }
            String queryString = SparqlTemplates.getComentsInPlace(lugarId);
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query,myModel);
            try {
                ResultSet results= qexec.execSelect();
                while(results.hasNext()){
                    QuerySolution soln = results.nextSolution();
                    Map<String, Object> comentario = new HashMap<>();
                    Resource userId = myModel.getResource(soln.getResource("user").toString());
                    Resource coment = myModel.getResource(soln.getResource("com").toString());
                    comentario.put("user",new HashMap<>(){{put("id",userId.toString().replace("http://turis-ucuenca/user/",""));
                    put("nombre",soln.getLiteral("nombreUser").toString());
                    put("foto",soln.getResource("fotoUser").toString());}});
                    if(soln.getLiteral("fbVideoIDs")!=null   ){

                        if( soln.getLiteral("fbVideoIDs").toString().equalsIgnoreCase("\"\"") || soln.getLiteral("fbVideoIDs").toString().isEmpty()) {
                            comentario.put("video", new ArrayList<>());
                        }else {
                            ArrayList<String> videos = new ArrayList( Arrays.asList( soln.getLiteral("fbVideoIDs").toString().split(",") ) );
                            if(videos.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag")){
                                videos.remove("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag");
                            }
                            comentario.put("video", videos);
                        }


                    }else{
                        comentario.put("video", new ArrayList<>());
                    }

                    if(soln.getLiteral("fbImageIDs")!=null){
                        if(soln.getLiteral("fbImageIDs").toString().equalsIgnoreCase("\"\"") || soln.getLiteral("fbImageIDs").toString().isEmpty()){
                            comentario.put("imageFbId", new ArrayList<>());
                        }else{
                            ArrayList<String> imageId = new ArrayList( Arrays.asList( soln.getLiteral("fbImageIDs").toString().split(",") ) );
                            if(imageId.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag")){
                                imageId.remove("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag");
                            }
                            comentario.put("imageFbId", imageId);
                        }


                    }else{
                        comentario.put("video", new ArrayList<>());
                    }

                    if(soln.getLiteral("imagenes").toString().isEmpty()){
                        comentario.put("imagenes", new ArrayList<>());
                    }else{
                        comentario.put("imagenes",new ArrayList( Arrays.asList( soln.getLiteral("imagenes").toString().split(",") ) ));
                    }


                    comentario.put("comentario",soln.getLiteral("comentario").toString());
                    comentario.put("puntaje", soln.getLiteral("puntaje").getInt());
                    comentario.put("id",coment.toString().replace("http://turis-ucuenca/comentario/",""));
                    comentarios.add(comentario);

                }
            }finally {
                qexec.close();
            }

        }finally {

        }
        return comentarios;
    }

    public boolean delteComent(String comentarioId) {
        try (Connection myConnection = DBConnection.createConnection()) {
            Model myModel = SDJenaFactory.createModel(myConnection);
            Resource comentario = myModel.getResource(BASE + "/comentario/" + comentarioId);
            if (comentario.getProperty(myModel.createProperty(BASE, "#text")) == null) {
                return false;
            }
            String queryDelete = SparqlTemplates.deleteComent(comentarioId);
            stardogHttpQueryConn = new StardogHttpQueryConn();
            stardogHttpQueryConn.PostToTriplestore(queryDelete);
            /*
            myModel.begin();
            myModel.removeAll((Resource) null, myModel.createProperty(BASE, "#comment"),comentario);

            myModel.commit();*/


        }
        return true;
    }

    public Boolean updateImages() {

        try (Connection myConnection = DBConnection.createConnection()) {
            Model myModel = SDJenaFactory.createModel(myConnection);
            myModel.begin();
            String queryString = SparqlTemplates.getAllComments();
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query,myModel);
            try {
                ResultSet results= qexec.execSelect();
                while(results.hasNext()){
                    QuerySolution soln = results.nextSolution();
                    Resource comentario = soln.getResource("com");
                    StmtIterator nameIterator = comentario.listProperties(VCARD4.hasPhoto);
                    while (nameIterator.hasNext()) {
                        Statement s = nameIterator.nextStatement();
                        //comentario hasPhoto url
                        myModel.remove(s);
                        System.out.println(s);
                    }
                    System.out.println(comentario.toString());


                    if(soln.getResource("images_id_node")!=null){
                        Bag imagesId = myModel.getBag(soln.getResource("images_id_node"));
                        NodeIterator iter = imagesId.iterator();
                        while (iter.hasNext()) {
                            RDFNode ImageBagItem = iter.nextNode();
                            System.out.println(ImageBagItem.toString());
                            String urlImage = FacebookService.urlImageByIdImage(ImageBagItem.toString());
                            comentario.addProperty(VCARD4.hasPhoto,urlImage);
                        }
                    }



                }
            }finally {
                myModel.commit();

                qexec.close();
                myModel.close();
            }

        }finally {


        }

        return true;
    }
}
