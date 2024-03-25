package org.opendcs.testing.rpc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.opendcs.testing.kiwi.TestCase;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class KiwiClient
{
    private final OkHttpClient client;
    private final String baseUrl;

    public KiwiClient(String url, String username, String password) throws IOException
    {
        File cache = Files.createTempDirectory("kiwiokhttpcace").toFile();
        cache.deleteOnExit();
        client = new OkHttpClient.Builder().cache(new Cache(cache,30*1024*1024)) // 30MB
                                 .authenticator(new Authenticator()
                                 {

                                    @Override
                                    public Request authenticate(Route route, Response response) throws IOException
                                    {
                                        if (response.request().header("Authorization") == null)
                                        {
                                            return null;
                                        }

                                        String credentials = Credentials.basic(username, password);
                                        return response.request().newBuilder()
                                                       .header("Authorization", credentials)
                                                       .build();
                                    }
                                    
                                 })
                                 .build();
        baseUrl = url;
    }

    public void writeTestCase(TestCase tc) throws IOException
    {
        Request req = new Request.Builder()
                                 .url(baseUrl+"/test")
                                 .post(RequestBody.create(tc.getSummary(), MediaType.get("application/json")))
                                 .build();
        Response res = client.newCall(req).execute();
        
        System.out.println("HTTP result: " + res.code());
    }
}
