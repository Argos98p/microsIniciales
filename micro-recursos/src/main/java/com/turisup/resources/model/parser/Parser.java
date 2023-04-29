package com.turisup.resources.model.parser;

import com.complexible.common.base.Bool;
import com.complexible.stardog.plan.filter.functions.rdfterm.Str;
import com.turisup.resources.model.*;
import com.turisup.resources.utils.Utils;
import org.apache.jena.base.Sys;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.quartz.SimpleTrigger;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

public class Parser {

    public static PlaceResponse QueryResult2Place(QuerySolution soln){
        PlaceResponse placeResponse = new PlaceResponse();

        System.out.println(soln);

        if (soln.getLiteral("orgName") == null){
            return null;
        }
        String orgName = soln.getLiteral("orgName").toString();
        String orgId= soln.getResource("org").getLocalName();


        String creadoPor= soln.getResource("creador").toString().replace("http://turis-ucuenca/user/","");
        String nombreCreador="";
        String imageUser = "";
        if(soln.getLiteral("nombre")!=null){
             nombreCreador=soln.getLiteral("nombre").toString();
            imageUser = soln.getResource("creadorImage").toString();
        }


        String region = soln.getLiteral("regionTitulo").toString();
        String regionId= soln.getResource("region").getLocalName();

        String placeId = soln.getResource("place").toString().replace("http://turis-ucuenca/lugar/","");

        String status = soln.getLiteral("status").toString();
        String titulo = soln.getLiteral("titulo").toString();
        String descripcion = soln.getLiteral("descripcion").toString();
        String favorito = soln.getLiteral("favorito").toString();



        ArrayList<String> facebookImagesIds= new ArrayList( Arrays.asList( soln.getLiteral("fbIDs").toString().split(",") ) );
        facebookImagesIds.remove("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag");
        //facebookImagesIds.remove(facebookImagesIds.size()-1);

        ArrayList<String> facebookImagesUrls= new ArrayList( Arrays.asList( soln.getLiteral("imagenes").toString().split(",") ) );

        Point2D.Double point= Utils.literalToPoint(soln.getLiteral("point"));
        PlacePoint mypoint = new PlacePoint(point.x,point.y);

        PlaceResponse myPlaceResponse = new PlaceResponse(placeId,titulo,status,mypoint,descripcion,facebookImagesUrls,facebookImagesIds,new Organization(orgId,orgName),new Region(regionId,region),new User(creadoPor,nombreCreador,imageUser),favorito);

        if(soln.getLiteral("date")!=null){
            String date = soln.getLiteral("date").getString();
            myPlaceResponse.setFecha(date);
        }else{
            myPlaceResponse.setFecha("2023-02-23T02:52:03.851807Z");
        }
        if(soln.getLiteral("distance")!=null){
            Double distancia = soln.getLiteral("distance").getDouble();
            myPlaceResponse.setDistancia(distancia);
        }else{
            myPlaceResponse.setDistancia(null);
        }
        if(soln.getLiteral("rate")!=null){
            Double rate = soln.getLiteral("rate").getDouble();
            myPlaceResponse.setRate(rate);
        }else{
            myPlaceResponse.setRate(99.0);
        }
        if(soln.getLiteral("fbVideoIDs")!=null && !soln.getLiteral("fbVideoIDs").toString().equalsIgnoreCase("")){
            ArrayList<String> facebookVideoIds= new ArrayList( Arrays.asList( soln.getLiteral("fbVideoIDs").toString().split(",") ) );
            facebookVideoIds.remove("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag");
            myPlaceResponse.setFbVideoIds(facebookVideoIds);
        }else{
            myPlaceResponse.setFbVideoIds(new ArrayList<>());
        }


        if(soln.getLiteral("categoria")!=null){
            String categoria = soln.getLiteral("categoria").toString();
            myPlaceResponse.setCategoria(categoria);
        }else{
            myPlaceResponse.setCategoria("sin categoria");
        }

        return myPlaceResponse;
    }
}
