package org.rs.controller;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import jakarta.ws.rs.FormParam;
import java.io.InputStream;

public class ImageUploadForm {

    @FormParam("image")
    @PartType("application/octet-stream")
    private InputStream imageStream;

    public byte[] getImageData() {
        try {
            return imageStream.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
