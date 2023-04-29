package com.turisup.resources.repository;

import com.turisup.resources.model.parser.QueryOptions;
import com.turisup.resources.model.request.get.AdminPlaceRequest;
import com.turisup.resources.model.request.post.PlaceRequest;
import org.checkerframework.checker.units.qual.Prefix;

import java.util.List;

public class SparqlTemplates {

    static final String prefixes = "prefix : <http://turis-ucuenca/>"+
            "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
            "prefix org: <http://www.w3.org/TR/vocab-org/>"+
            "prefix myorg: <http://turis-ucuenca/org/>"+
            "prefix myregiones: <http://turis-ucuenca/region/>"+
            "prefix lugar: <http://turis-ucuenca/lugar/>"+
            "prefix myusers: <http://turis-ucuenca/user/>"+
            "prefix foaf: <http://xmlns.com/foaf/0.1/>"+
            "prefix tp: <http://tour-pedia.org/download/tp.owl>"+
            "prefix vcard: <http://www.w3.org/2006/vcard/ns#>"+
            "prefix geo: <http://www.opengis.net/ont/geosparql#>"+
            "prefix dc: <http://purl.org/dc/elements/1.1/>"+
            "prefix xsd: <http://www.w3.org/2001/XMLSchema>"+
            "prefix base2:<http://turis-ucuenca#>"+
            "prefix baseProperty: <http://turis-ucuenca#>" +
            "prefix base3:<http://turis-ucuenca/>"+
            "prefix fb:<http://turis-ucuenca#>"+
            "prefix unit: <http://qudt.org/vocab/unit#>"+
            "prefix geof: <http://www.opengis.net/def/function/geosparql/>"+
            "base  <http://turis-ucuenca/>";

    public static String getPlace(String placeId){
        String lugar = "lugar:"+placeId;
            return prefixes+"" +
                    "SELECT  ?titulo ?descripcion (GROUP_CONCAT(DISTINCT ?imagenes2 ; SEPARATOR = ',') AS ?imagenes)  ?creador ?point  (GROUP_CONCAT(DISTINCT ?idFacebook ; SEPARATOR = ',') AS ?fbIDs)  WHERE{"+
                    lugar +" dc:title ?titulo."+
                    lugar +" dc:description ?descripcion."+
                    lugar +" fb:facebookId ?faceboook_id_node."+
                    lugar +" dc:creator ?creador."+
                    lugar +" vcard:hasPhoto ?imagenes2."+

                    lugar +" geo:hasGeometry ?geometry_node."+
                    "?geometry_node geo:asWKT ?point."+
                    "?faceboook_id_node ?prop ?idFacebook."+
                    "FILTER"+
                    "FILTER(?prop != rdf:type)"+
                    "} GROUP BY ?titulo ?descripcion ?creador ?point";
    }

    public static String getAllPlace() {
        return prefixes+"" +
                "SELECT ?id ?titulo ?descripcion (GROUP_CONCAT(DISTINCT ?imagenes2 ; SEPARATOR = ',') AS ?imagenes)  ?creador ?point  (GROUP_CONCAT(DISTINCT ?idFacebook ; SEPARATOR = ',') AS ?fbIDs)  WHERE{"+
                "?id a tp:POI."+
                "?id dc:title ?titulo."+
                "?id dc:description ?descripcion."+
                "?id fb:facebookId ?faceboook_id_node."+
                "?id dc:creator ?creador."+
                "?id vcard:hasPhoto ?imagenes2."+
                "?id geo:hasGeometry ?geometry_node."+
                "?geometry_node geo:asWKT ?point."+
                "?faceboook_id_node ?prop ?idFacebook."+
                "FILTER(?prop != rdf:type)"+
                "} GROUP BY ?id ?titulo ?descripcion ?creador ?point";
    }

    public static String getPlacesByRegionOrgStatusUser(AdminPlaceRequest props) {

        return "";
    }

    public static String updateImagesUrls(List<String> newImagesUrls,String placeId){
        StringBuilder insert= new StringBuilder();
        for(String url:newImagesUrls){
            insert.append("\n").append("lugar:").append(placeId).append(" vcard:hasPhoto \"").append(url).append("\".");
        }
        return prefixes+
                "DELETE{"
                + "?place vcard:hasPhoto ?photo."
                + "}"
                + "INSERT{"
                    + insert
                + "}"
                + " WHERE{"
                    + "?place vcard:hasPhoto ?photo."
                    + "FILTER(str(?place) = 'http://turis-ucuenca/lugar/"+placeId+"')"
                + " }";
    }
    public static String getUserAdminResources(String userId) {
        String userUri= "http://turis-ucuenca/user/"+userId;
        return prefixes+
                "SELECT * WHERE {"+
                "?user a foaf:Person."+
                "?user foaf:memberOf ?org."+

                "?org dc:title ?orgTitle."+
                "?org :admin ?region."+

                "?region dc:title ?regionTitle."+

                "FILTER(str(?user)='"+userUri+"')"+
                "}";
    }
    public static String  defaultQuery(QueryOptions queryOptions){

        String filters ="";
        String routeBagNode="";
        String filterNode="";
        if(queryOptions.getCreadorId()!= null){
            filters = filters + "FILTER(str(?creador)= 'http://turis-ucuenca/user/"+queryOptions.getCreadorId()+"').\n";
        }if(queryOptions.getEstadoLugar()!= null){
            filters = filters +"FILTER(str(?status) = '"+queryOptions.getEstadoLugar()+"').\n";
        }if(queryOptions.getLugarId()!= null){
            filters = filters + "FILTER(str(?place)='http://turis-ucuenca/lugar/"+queryOptions.getLugarId()+"').\n";
        }if(queryOptions.getOrganizacionId()!=null){
            filters = filters +  "FILTER(str(?org)='http://turis-ucuenca/org/"+queryOptions.getOrganizacionId()+"').\n";
        }if(queryOptions.getLatitud()!=null && queryOptions.getLongitud()!=null){
            filters = filters + " BIND(geof:distance(?geom, \"POINT("+queryOptions.getLongitud()+" "+queryOptions.getLatitud()+")\"^^geo:wktLiteral, unit:Kilometer) as ?distance)\n";
        }if(queryOptions.getDistanciaMax()!=null){
            filters = filters +"FILTER(geof:distance(?geom, \"POINT("+queryOptions.getLongitud()+" "+queryOptions.getLatitud()+")\"^^geo:wktLiteral, unit:Kilometer) < "+queryOptions.getDistanciaMax()+")\n";
        }if(queryOptions.getRegionId()!=null){
            filters = filters + "FILTER(str(?region)='http://turis-ucuenca/region/"+queryOptions.getRegionId()+"').\n";
        }if(queryOptions.getBuscar()!=null){
            filters = filters + "FILTER contains(lcase(str(?titulo)),lcase(\""+queryOptions.getBuscar()+"\")).\n ";
        }


        if(queryOptions.getNodeForRouteBag()!= null){
           // routeBagNode = "<_:"+queryOptions.getNodeForRouteBag()+"> ?g ?place . ";
            routeBagNode = " ?nodo ?g ?place .";
            filters = filters + " "+routeBagNode+ "     FILTER(str(?nodo)=\""+queryOptions.getNodeForRouteBag()+"\")\n";
        }

        return "prefix : <http://turis-ucuenca/>"+
                "prefix org: <http://www.w3.org/TR/vocab-org/>"+
                "prefix myorg: <http://turis-ucuenca/org/>"+
                "prefix myregiones: <http://turis-ucuenca/region/>"+
                "prefix lugar: <http://turis-ucuenca/lugar/>"+
                "prefix myusers: <http://turis-ucuenca/user/>"+
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"+
                "prefix tp: <http://tour-pedia.org/download/tp.owl>"+
                "prefix vcard: <http://www.w3.org/2006/vcard/ns#>"+
                "prefix geo: <http://www.opengis.net/ont/geosparql#>"+
                "prefix dc: <http://purl.org/dc/elements/1.1/>"+
                "prefix fb:<http://turis-ucuenca#>"+
                "prefix base2:<http://turis-ucuenca#>"+
                "prefix unit: <http://qudt.org/vocab/unit#>" +
                "prefix geof: <http://www.opengis.net/def/function/geosparql/>"+
                "base  <http://turis-ucuenca/>"+
                "SELECT ?org ?orgName ?creadorImage  ?region ?regionTitulo ?place ?titulo  ?categoria  ?distance ?favorito ?status ?date ?descripcion (AVG(?rating) as ?rate)  ?imagenes  ?creador ?nombre ?point ?fbIDs ?fbVideoIDs WHERE {"
        +
                "SELECT ?org ?orgName ?creadorImage  ?favorito ?region ?categoria ?rating   ?regionTitulo ?place ?titulo ?distance ?date ?status ?descripcion (GROUP_CONCAT(DISTINCT ?imagenes2 ; SEPARATOR = ',') AS ?imagenes)  ?creador ?nombre ?point (GROUP_CONCAT(DISTINCT ?idVideoFacebook ; SEPARATOR = ',') AS ?fbVideoIDs)  (GROUP_CONCAT(DISTINCT ?idFacebook ; SEPARATOR = ',') AS ?fbIDs)"+
                "WHERE {"+
                "?region a :Region."+
                "?region dc:title ?regionTitulo."+
                "?region geo:hasGeometry ?geoRegion."+
                "?region :isAdminBy ?org."+
                "?geoRegion geo:asWKT ?regionWKT ."+
                "?org dc:title ?orgName."+
                routeBagNode+
                "?place a tp:POI ."+
                "?place dc:title ?titulo."+
                "?place dc:description ?descripcion."+
                "?place fb:facebookId ?faceboook_id_node."+
                "OPTIONAL {?place fb:category ?categoria. }"+
                "?place dc:creator ?creador."+
                "?place vcard:hasPhoto ?imagenes2."+
                "?place base2:status ?status."+
                "?place geo:hasGeometry ?geom."+
                "OPTIONAL { ?place dc:date ?date . }"+
                "BIND (if(EXISTS{?place base2:isFavoriteOf <http://turis-ucuenca/user/"+queryOptions.getUserId()+">}, \"si\",\"no\") as ?favorito)"+
                "?creador foaf:name ?nombre."+
                "    ?creador foaf:depiction ?creadorImage."+

                "?faceboook_id_node ?prop ?idFacebook."+
                "?geom geo:asWKT ?point ."+
                " OPTIONAL{\n" +
                "            \n" +
                "                ?comment a :Comentario.\n" +
                "            ?comment fb:place ?place. \n" +
                "            ?comment fb:rating ?rating.\n" +
                "            \n" +
                "        }"+

                "OPTIONAL{\n" +
                "        ?place fb:facebookVideoId ?videoId.\n" +
                "        ?videoId ?prop2 ?idVideoFacebook\n" +
                "    }"+

                "FILTER geof:within(?point,?regionWKT)."+
                filters+

                "} GROUP BY ?creadorImage ?org ?orgName ?favorito ?region ?rating ?categoria ?regionTitulo ?place ?titulo ?distance ?date ?status ?descripcion ?creador ?point ?nombre"+
                "} GROUP BY ?creadorImage ?fbVideoIDs ?place ?org ?orgName ?favorito ?region ?categoria ?regionTitulo ?place ?titulo ?distance ?status ?date ?descripcion ?imagenes  ?creador ?nombre ?point ?idFacebook ?fbIDs\n" +
                "ORDER BY DESC (?rate )";
    }

    public static String usersInOrg (String orgId){
        String aux = "\"http://turis-ucuenca/org/"+orgId+"\")}";
        return prefixes+
                " SELECT ?nombre ?nick ?correoM ?userM WHERE{"+
    " ?org a org:Organization."+
                    " ?user a foaf:Person."+
                    " ?user base3:role 'admin'."+
                    " ?user foaf:memberOf ?org."+
                    " ?user foaf:name ?nombre."+
                    " ?user  foaf:nick ?nick."+
                    " ?user foaf:mbox ?correo."+
                    " BIND(REPLACE(STR(?user),'http://turis-ucuenca/user/','') AS ?userM)."+
                    " BIND(REPLACE(STR(?correo),'http://turis-ucuenca/','') AS ?correoM) ."+

            " FILTER(str(?org) ="+ aux;
        }

    public static String rutasByUser(String userId){

        return prefixes+
                "SELECT  * where{"+
    "<http://turis-ucuenca/user/"+userId+"> :hasRoute ?ruta."+
                                   "?ruta dc:title ?nombre;"+

        "}";
    }

    public static  String updatePlaceQuery(PlaceRequest placeUpdateInfo){
        String placeid = "'http://turis-ucuenca/lugar/"+placeUpdateInfo.getPlaceid()+"'";
        return prefixes

                +"DELETE{"
    +"?place dc:title ?titulo;"
           + "dc:description ?descripcion;"
           + "base2:status ?status;"
           + "rdfs:label ?titulo2;"
       + "}"
       + "INSERT{"
        +    "lugar:"+placeUpdateInfo.getPlaceid()+" dc:title '"+ placeUpdateInfo.getNombre()+"';"
        +    "dc:description '"+placeUpdateInfo.getDescripcion()+"';"
       +     "base2:status '"+placeUpdateInfo.getEstado()+"';"
       +     "rdfs:label '"+placeUpdateInfo.getNombre()+"'."
       + "}"
        +"WHERE{"
   +"?place dc:title ?titulo;"
         +   "dc:description ?descripcion;"
        +    "base2:status ?status;"
        +    "rdfs:label ?titulo2;"
       +     "FILTER(str(?place) = "+placeid+")"
      +  "}";
    }

    public static String getRuta(String rutaId) {
        return  prefixes +
                "SELECT  ?nombre ?descripcion ?creador ?placeNode {\n" +
                "    <http://turis-ucuenca/ruta/"+rutaId+"> dc:title ?nombre;\n" +
                "                                                                     dc:description ?descripcion;\n" +
                "                                                                     dc:creator ?creador;\n" +
                "                                                                     :hasPlaces ?placeNode.\n" +
                "}";
    }


    public static String getRutaPlaces(String node_id){
        return prefixes+
                "SELECT  ?placeId ?nombre ?descripcion (GROUP_CONCAT(DISTINCT ?imagenes2 ; SEPARATOR = ',') AS ?imagenes) ?point ?creador{\n" +
                "    ?nodo ?g ?placeId.\n" +
                "    ?placeId dc:title ?nombre.\n" +
                "    ?placeId dc:description ?descripcion.\n" +
                "    ?placeId dc:creator ?creador.               \n" +
                "    ?placeId vcard:hasPhoto ?imagenes2.    \n" +
                "    ?placeId geo:hasGeometry ?geometry_node.      \n" +
                "    ?geometry_node geo:asWKT ?point.       \n" +
                "     FILTER(str(?nodo)=\""+node_id+"\")\n"+
                "} GROUP BY ?placeId ?nombre ?descripcion ?point ?creador";
    }

    public static String getComentsInPlace(String idPlace){
       return  prefixes+ "\nSELECT ?user ?nombreUser ?fotoUser ?com ?comentario ?puntaje (GROUP_CONCAT(DISTINCT ?img ; SEPARATOR = ',') AS ?imagenes) (GROUP_CONCAT(DISTINCT ?idImageFacebook ; SEPARATOR = ',') AS ?fbImageIDs) (GROUP_CONCAT(DISTINCT ?idVideoFacebook ; SEPARATOR = ',') AS ?fbVideoIDs) WHERE{\n" +
                "    ?user a foaf:Person.\n" +
                "    ?user foaf:name ?nombreUser.\n" +
                "    ?user foaf:depiction ?fotoUser.\n" +
                "    ?user baseProperty:comment ?com.\n" +
                "    ?com a :Comentario.\n" +
                "    ?com baseProperty:place <http://turis-ucuenca/lugar/"+idPlace+">.\n" +
                "    ?com baseProperty:text ?comentario.\n" +
                "    ?com baseProperty:rating ?puntaje.\n" +
                "\n" +
                "    OPTIONAL { ?com vcard:hasPhoto ?img }    \n" +
               "OPTIONAL { ?com baseProperty:facebookId ?faceboook_id_node. \n" +
               "              ?faceboook_id_node ?prop ?idFacebook.}"+
               "OPTIONAL { ?com baseProperty:facebookVideoId ?faceboook_id_node. \n" +
               "              ?faceboook_id_node ?prop ?idVideoFacebook.}\n" +
               "   OPTIONAL { ?com baseProperty:facebookImageId ?faceboook_id_node2. \n" +
               "   ?faceboook_id_node2 ?prop ?idImageFacebook.}"+
                "} GROUP BY ?user ?nombreUser ?fotoUser ?com ?comentario ?puntaje";
    }

    public static String getAllComments(){
        return prefixes + " SELECT ?place ?com  ?images_id_node ?videos_id_node (GROUP_CONCAT(DISTINCT ?img ; SEPARATOR = ',') AS ?imagenes) (GROUP_CONCAT(DISTINCT ?idImageFacebook ; SEPARATOR = ',') AS ?fbImageIDs) (GROUP_CONCAT(DISTINCT ?idVideoFacebook ; SEPARATOR = ',') AS ?fbVideoIDs) WHERE{            \n" +
                "                    \n" +
                "   ?com a :Comentario.\n" +
                "   ?com baseProperty:place ?place.\n" +
                "   \n" +
                "\n" +
                "   OPTIONAL { ?com vcard:hasPhoto ?img }   \n" +
                "   OPTIONAL { ?com baseProperty:facebookVideoId ?videos_id_node. \n" +
                "              ?videos_id_node ?prop2 ?idVideoFacebook.}\n" +
                "   OPTIONAL { ?com baseProperty:facebookImageId ?images_id_node. \n" +
                "   ?images_id_node ?prop ?idImageFacebook.}\n" +
                "   \n" +
                "} GROUP BY  ?place ?com ?comentario  ?images_id_node ?videos_id_node";
    }

    public static String getRecursoById(String placeId,String userId){

        placeId = " <http://turis-ucuenca/lugar/"+placeId+"> ";

        return  prefixes +"SELECT ?place ?org ?orgName ?favorito ?region ?regionTitulo ?favorito  ?titulo ?creadorImage  ?distance ?status ?date ?descripcion (AVG(?rating) as ?rate)  ?imagenes  ?creador ?nombre ?point ?fbVideoIDs  ?fbIDs WHERE {\n" +
                "SELECT ?place ?org ?orgName ?favorito ?region ?regionTitulo  ?favorito ?titulo ?distance ?creadorImage ?status ?date ?descripcion (GROUP_CONCAT(DISTINCT ?imagenes2 ; SEPARATOR = ',') AS ?imagenes)  ?creador ?nombre ?point (GROUP_CONCAT(DISTINCT ?idVideoFacebook ; SEPARATOR = ',') AS ?fbVideoIDs)  (GROUP_CONCAT(DISTINCT ?idFacebook ; SEPARATOR = ',') AS ?fbIDs)\n" +
                "WHERE {\n" +
                "\n" +
                "\n" +
                "    ?region a :Region.\n" +
                "    ?region dc:title ?regionTitulo.\n" +
                "    ?region geo:hasGeometry ?geoRegion.\n" +
                "    ?region :isAdminBy ?org.\n" +
                "    ?geoRegion geo:asWKT ?regionWKT .\n" +
                "    ?org dc:title ?orgName.\n" +
                "    "+placeId+" a tp:POI .\n" +
                "    "+placeId+" dc:title ?titulo.\n" +
                "    "+placeId+"dc:description ?descripcion.\n" +
                "    "+placeId+" fb:facebookId ?faceboook_id_node.\n" +
                "    "+placeId+" dc:creator ?creador.\n" +
                "    "+placeId+" vcard:hasPhoto ?imagenes2.\n" +
                "    "+placeId+" base2:status ?status.\n" +
                "    "+placeId+" geo:hasGeometry ?geom.\n" +
                "    BIND("+placeId+" as ?place)."+
                "    BIND (if(EXISTS{"+placeId+" base2:isFavoriteOf <http://turis-ucuenca/user/"+userId+">}, \"si\",\"no\") as ?favorito).\n" +
                "        \n" +
                "    OPTIONAL { \n" +
                "        "+placeId+" dc:date ?date . }\n" +
                "    ?creador foaf:name ?nombre.\n" +
                "    ?creador foaf:depiction ?creadorImage."+
                "    ?faceboook_id_node ?prop ?idFacebook.\n" +
                "    ?geom geo:asWKT ?point .\n" +
                "    \n" +
                "\n" +
                "    \n" +
                "    FILTER geof:within(?point,?regionWKT).\n" +
                "           OPTIONAL{\n" +
                "            \n" +
                "            ?comment a :Comentario.\n" +
                "            ?comment fb:place ?place. \n" +
                "            ?comment fb:rating ?rating.\n" +
                "            \n" +
                "        }\n" +
                "        \n" +
                "OPTIONAL{\n" +
                "        ?place fb:facebookVideoId ?videoId.\n" +
                "        ?videoId ?prop2 ?idVideoFacebook\n" +
                "    }"+
                "} GROUP BY ?place ?org ?orgName ?favorito ?region ?regionTitulo ?favorito  ?titulo ?distance ?date ?status ?descripcion ?creador ?creadorImage ?point ?nombre\n" +
                "} GROUP BY ?place ?org ?orgName ?favorito ?region  ?regionTitulo ?place ?favorito ?titulo ?distance ?status ?date ?descripcion ?imagenes  ?creador ?creadorImage ?nombre ?point ?idFacebook ?fbVideoIDs ?fbIDs\n" +
                "ORDER BY DESC (?rate )";
    }

    public static String deleteComent(String comentarioId) {
        return prefixes+"\nDELETE {\n" +
                "    ?user baseProperty:comment <http://turis-ucuenca/comentario/"+comentarioId+">.\n" +
                "    <http://turis-ucuenca/comentario/"+comentarioId+"> ?f ?b.\n" +
                "    \n" +
                "}WHERE{\n" +
                "    ?user baseProperty:comment <http://turis-ucuenca/comentario/"+comentarioId+">.\n" +
                "    <http://turis-ucuenca/comentario/"+comentarioId+"> ?f ?b.\n" +
                "}";
    }

    public static String eliminarLugarEnRuta(String rutaId, String lugarId) {
        return prefixes+
                "DELETE {\n" +
                "    ?l ?f <http://turis-ucuenca/lugar/"+lugarId+">.\n" +
                "}WHERE{\n" +
                "    FILTER(str(?place)=\"http://turis-ucuenca/ruta/"+rutaId+"\")\n" +
                "    ?place :hasPlaces ?l.\n" +
                "    ?l ?f <http://turis-ucuenca/lugar/"+lugarId+">.\n" +
                "}";
    }

    public static String eliminarRuta(String rutaId) {
        return prefixes+"\nDELETE {\n" +
                "    ?place ?f ?g.\n" +
                "}WHERE{\n" +
                "    ?place ?f ?g.\n" +
                "    FILTER(str(?place)=\"http://turis-ucuenca/ruta/"+rutaId+"\")\n" +
                "}";
    }

    public static String getInteractions (String fechaInicio, String fechaFin, String userId){
        return  prefixes+"" +
                "SELECT ?date  ?rating ?contenido ?commentId ?placeId ?place ?comentario (GROUP_CONCAT(DISTINCT ?imagenes2 ; SEPARATOR = ',') AS ?imagenes) WHERE {\n" +
                "    <http://turis-ucuenca/user/"+userId+"> baseProperty:comment ?comentario.\n" +
                "    ?comentario baseProperty:place ?place.\n" +
                "    ?comentario dc:date ?date.\n" +
                "    ?comentario baseProperty:rating ?rating.\n" +
                "    ?comentario baseProperty:rating ?rating.\n" +
                "    ?comentario baseProperty:text ?contenido.\n" +
                "    ?place vcard:hasPhoto ?imagenes2;\n" +
                "    BIND(REPLACE(str(?comentario), \"http://turis-ucuenca/comentario/\", \"\") AS ?commentId)\n" +
                "    BIND(REPLACE(str(?place), \"http://turis-ucuenca/lugar/\", \"\") AS ?placeId)\n" +
                "    FILTER(?date >\""+fechaInicio+"\"^^xsd:dateTime && ?date < \""+fechaFin+"\"^^xsd:dateTime)\n" +
                "}GROUP BY ?date ?rating ?contenido ?commentId ?placeId ?place ?comentario";
    }


    public static String getFavorites(String userId){
        return prefixes +"\n"+"SELECT ?org ?orgName ?creadorImage ?favorito ?region ?categoria ?regionTitulo ?place ?titulo ?distance ?date ?status ?descripcion (GROUP_CONCAT(DISTINCT ?imagenes2 ; SEPARATOR = ',') AS ?imagenes) (GROUP_CONCAT(DISTINCT ?idVideoFacebook ; SEPARATOR = ',') AS ?fbVideoIDs) ?creador ?nombre ?point  (GROUP_CONCAT(DISTINCT ?idFacebook ; SEPARATOR = ',') AS ?fbIDs)"+
                "WHERE {"+
                "?region a :Region."+
                "?region dc:title ?regionTitulo."+
                "?region geo:hasGeometry ?geoRegion."+
                "?region :isAdminBy ?org."+
                "?geoRegion geo:asWKT ?regionWKT ."+
                "?org dc:title ?orgName."+
                "?place a tp:POI ."+
                "?place dc:title ?titulo."+
                "?place dc:description ?descripcion."+
                "?place fb:facebookId ?faceboook_id_node."+
                "OPTIONAL {?place fb:category ?categoria. }"+
                "?place dc:creator ?creador."+
                "?place vcard:hasPhoto ?imagenes2."+
                "?place base2:status ?status."+
                "?place geo:hasGeometry ?geom."+
                "OPTIONAL { ?place dc:date ?date . }"+
                "<http://turis-ucuenca/user/"+userId+"> base2:favorite ?place."+

                "?creador foaf:name ?nombre."+
                "?creador foaf:depiction ?creadorImage."+

                "?faceboook_id_node ?prop ?idFacebook."+
                "?geom geo:asWKT ?point ."+
                "BIND (if(EXISTS{?place base2:isFavoriteOf <http://turis-ucuenca/user/"+userId+">}, \"si\",\"no\") as ?favorito)"+
                "FILTER geof:within(?point,?regionWKT)."+
                "OPTIONAL{\n" +
                "        ?place fb:facebookVideoId ?videoId.\n" +
                "        ?videoId ?prop2 ?idVideoFacebook\n" +
                "    }"+


                "} GROUP BY ?org ?creadorImage ?orgName ?favorito ?region ?categoria ?regionTitulo ?place ?titulo ?distance ?date ?status ?descripcion ?creador ?point ?nombre";
    }

}
