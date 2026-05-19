// Copyright 2012-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

package com.amazonaws.samples;

import java.io.File;
import java.util.Iterator;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LoadA1 {

    public static void main(String[] args) throws Exception {

//        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
//                .withEndpointConfiguration(
//                        new AwsClientBuilder.EndpointConfiguration(
//                                "http://localhost:8000",
//                                Regions.US_EAST_1.getName()))
//                .build();

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("music");

        JsonParser parser = new JsonFactory().createParser(new File("a1.json"));

        JsonNode rootNode = new ObjectMapper().readTree(parser);
//        System.out.println(rootNode.path("songs").toString());

        Iterator<JsonNode> iter = rootNode.path("songs").iterator();

        ObjectNode currentNode;
        int idCount = 0;

        while (iter.hasNext()) {
            currentNode = (ObjectNode) iter.next();

            int id = idCount;
            String title = currentNode.path("title").asText();
            String artist = currentNode.path("artist").asText();
            int year = currentNode.path("year").asInt();
            String web_url = currentNode.path("web_url").asText();
            String img_url = currentNode.path("img_url").asText();

            try {
                table.putItem(new Item().withPrimaryKey("id", id)
                        .withString("title", title)
                        .withString("artist", artist)
                        .withInt("year", year)
                        .withString("web_url", web_url)
                        .withString("img_url", img_url)
                );
                System.out.println("PutItem succeeded: " + year + " " + title);
                idCount++;
            }
            catch (Exception e) {
                System.err.println("Unable to add music: " + year + " " + title);
                System.err.println(e.getMessage());
                break;
            }
        }
        parser.close();
    }
}