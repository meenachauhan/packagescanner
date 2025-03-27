package org.rs.controller;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import io.quarkus.logging.Log;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/scan/image")
public class VisionController {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response analyzeImage(@MultipartForm ImageUploadForm form) {
        try {
            List<String> labels = detectLabels(form.getImageData());
            List<String> texts = detectText(form.getImageData());
            return Response.ok(new ScanResult(labels, texts)).build();
        } catch (Exception e) {
            Log.error("Error processing image: " + e.getMessage());
            return Response.serverError().entity("Failed to analyze image").build();
        }
    }

    private List<String> detectLabels(byte[] imageBytes) throws IOException {
        List<String> labelDescriptions = new ArrayList<>();
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            Image image = Image.newBuilder().setContent(ByteString.copyFrom(imageBytes)).build();
            Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            for (EntityAnnotation annotation : response.getResponses(0).getLabelAnnotationsList()) {
                labelDescriptions.add(annotation.getDescription());
            }
        }
        return labelDescriptions;
    }

    private List<String> detectText(byte[] imageBytes) throws IOException {
        List<String> textResults = new ArrayList<>();
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            Image image = Image.newBuilder().setContent(ByteString.copyFrom(imageBytes)).build();
            Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            for (EntityAnnotation annotation : response.getResponses(0).getTextAnnotationsList()) {
                textResults.add(annotation.getDescription());
            }
        }
        return textResults;
    }

    public static class ScanResult {
        public List<String> labels;
        public List<String> texts;
        public ScanResult(List<String> labels, List<String> texts) {
            this.labels = labels;
            this.texts = texts;
        }
    }
}
