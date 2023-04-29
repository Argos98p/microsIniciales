package com.turisup.resources.service;

import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.json.JsonValue;
import com.restfb.types.FacebookType;
import com.restfb.types.GraphResponse;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class FacebookService {

    String APPSECRET = "a368875014f7654717a2e1f24c07b98c";
    static String PAGEID= "165980483492633";
    static DefaultFacebookClient facebookClient = new DefaultFacebookClient(ACCESSTOKEN,  Version.LATEST);
    public static String UploadVideo(String filePath, String name) throws IOException {


        InputStream in2 = new FileInputStream(filePath);
        GraphResponse response = facebookClient.publish(PAGEID + "/videos", GraphResponse.class, BinaryAttachment.with("video_test.mp4", IOUtils.toByteArray(in2)),
                Parameter.with("description", " Video Description "));
        return response.getId();

    }

    public static String UploadImage(String filePath) throws FileNotFoundException {
        InputStream is = new FileInputStream(filePath);
        FacebookType publishVideoResponse =facebookClient.publish("me/photos",FacebookType.class,
                BinaryAttachment.with("myphoto.jpg", is),
                Parameter.with("message", "MY PHOTO POST"));
        return publishVideoResponse.getId();
    }

    public static String urlImageByIdImage(String imageID){
        JsonObject jsonObject = facebookClient.fetchObject("/" + imageID , JsonObject.class,
                Parameter.with("fields", "images"));
        JsonArray images = (JsonArray) jsonObject.get("images");
        JsonObject bestImage = (JsonObject) images.get(0);
        return bestImage.get("source").asString().replace("\"","");
    }
}
