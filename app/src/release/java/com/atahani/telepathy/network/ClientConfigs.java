package com.atahani.telepathy.network;

/**
 * the client configs class that store all of the client access information
 * PRODUCTION
 */
public class ClientConfigs {

    //TODO: should get network ip address like telepathy.mobi and replace inside REST_API_BASE_URL
    public static final String REST_END_POINT_URL = "http://IP_ADDRESS:5000/api/v1";

    //TODO: create new Client with in backed and set these values with client_id and client_key
    //TELEPATHY CLIENT CONFIG INFO
    public static final String TELEPATHY_APP_ID = "";
    public static final String TELEPATHY_APP_KEY = "";

    //TODO: replace the GOOGLE_SERVICE_CLINET_ID for google sign in
    //GOOGLE SERVER CLIENT INFO
    public static final String GOOGLE_SERVER_CLIENT_ID = "";

    public static final String SLACK_END_POINT = "https://slack.com/api";

    //TODO: for logging application error we use from Slack api, create the slack bot user and set the `Access Token`
    public static final String SLACK_BOT_TOKEN = "";

}
