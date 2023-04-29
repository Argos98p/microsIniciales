package com.turisup.resources.utils;

import org.apache.jena.rdf.model.Literal;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import javax.activation.MimetypesFileTypeMap;
import java.awt.geom.Point2D;
import java.io.File;

public class Utils {

    public static Point2D.Double literalToPoint(Literal pointLiteral){
        String myPoint = pointLiteral.toString();
        String aux = myPoint.substring(myPoint.indexOf("(")+1, myPoint.indexOf(")"));
        String [] aux2 = aux.split(" ");
        return new Point2D.Double(Double.parseDouble(aux2[0]), Double.parseDouble(aux2[1]));
    }

    public static String convertXMLtoJSON(String xml){
        System.out.println(xml);
        try {
            JSONObject json = XML.toJSONObject(xml);
            String jsonString = json.toString(4);
            System.out.println(jsonString);
            return jsonString;

        }catch (JSONException e) {
// TODO: handle exception
            System.out.println(e.toString());
            return "error";
        }

    }

    public static String getTypeOfFile(String filepath){

        File f = new File(filepath);
        String mimetype= new MimetypesFileTypeMap().getContentType(f);
        String type = mimetype.split("/")[0];
        System.out.println(type);
        if(type.equals("image"))
            return "isImage";
        else if(type.equals("application"))
            return "isVideo";
        return "noValid";
    }
}
