package com.turisup.resources.service;

import com.turisup.resources.property.FileStorageProperties;
import com.turisup.resources.utils.Slug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import org.apache.commons.io.FileUtils;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {

        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't create the directory where the upload files will be saved.", ex);
        }

    }
    public String storeFile(MultipartFile file, String idPlace) {

        //
        //Path placePath = Paths.get(String.valueOf(this.fileStorageLocation),idPlace);
        String placePath = String.valueOf(this.fileStorageLocation).concat("/").concat(new Slug().makeSlug(idPlace));
        try {
            Files.createDirectories(Paths.get(placePath));
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! File name which contains invalid path sequence " + fileName);
            }
            Path targetLocation = Paths.get(placePath,fileName) ;
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return targetLocation.toString();
            //return targetLocation.toString().replace("/home/argos98/Tes/web-server-tesis/media/","localhost:9090/");

        } catch (IOException ex) {

            throw new RuntimeException("Sorry! File name which contains invalid path sequence " );
        }
    }

    void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
    public void deleteImages(String directory) {


        System.out.println(directory);

            try
            {
                FileUtils.deleteDirectory(new File(fileStorageLocation.toString()+"/"+directory));

                /*
                File f= new File(path);           //file to be delete
                if(f.delete())                      //returns Boolean value
                {
                    System.out.println(f.getName() + " deleted");   //getting and printing the file name
                }
                else
                {
                    System.out.println("failed");
                }*/
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

    }
}
