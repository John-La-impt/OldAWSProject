package com.amazonaws.samples;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Iterator;

public class DownUpImgs {

    public static void main(String[] args) throws Exception {


        Regions clientRegion = Regions.US_EAST_1;
        String bucketName = "s#-artistimg"; // the s3 bucket
        String filePath = "downloadedImg/downloaded";
        String fileExt = ".jpg";
        String fileName = "downloadedImg/downloaded0.jpg";//e.g., sample.txt
        String fileObjKey1 = "downloaded";
        String fileObjKeyName = "downloaded0.jpg";//This part can be empty

        // create bucket
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new ProfileCredentialsProvider())
                    .withRegion(clientRegion)
                    .build();

            if (!s3Client.doesBucketExistV2(bucketName)) {
                // Because the CreateBucketRequest object doesn't specify a region, the
                // bucket is created in the region specified in the client.
                s3Client.createBucket(new CreateBucketRequest(bucketName));

                // Verify that the bucket was created by retrieving it and checking its location.
                String bucketLocation = s3Client.getBucketLocation(new GetBucketLocationRequest(bucketName));
                System.out.println("Bucket location: " + bucketLocation);
            }
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it and returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
        ////////////////////////////////////////////////////////////////////////////

        // download images
        JsonParser parser = new JsonFactory().createParser(new File("a1.json"));
        JsonNode rootNode = new ObjectMapper().readTree(parser);

        Iterator<JsonNode> iter = rootNode.path("songs").iterator();

        ObjectNode currentNode;

        int imgCount = 0;
        while (iter.hasNext()) {
            currentNode = (ObjectNode) iter.next();

            String img_url = currentNode.path("img_url").asText();

            URL url = new URL(img_url);
            BufferedImage img = ImageIO.read(url);
            File file = new File("downloadedImg/downloaded" + imgCount + ".jpg");
            ImageIO.write(img, "jpg", file);

            imgCount++;
        }
        parser.close();
        ///////////////////////////////////////////////

        // upload images to s3
        // count file number
        int fileCount = new File("downloadedImg").list().length;
        System.out.println("fileCount: " + fileCount);
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();

            for (int i = 0; i < fileCount; i++) {
                fileName = filePath + i + fileExt;
                fileObjKeyName = fileObjKey1 + i + fileExt;
                // Upload a file as a new object with ContentType and title specified.
                PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, new File(fileName));
                ObjectMetadata metadata = new ObjectMetadata();
//            metadata.setContentType("image");
//            metadata.addUserMetadata("title", "someTitle");
//            request.setMetadata(metadata);
                s3Client.putObject(request);
            }
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
        /////////////////////////////////////
    }
}
