package org.opendcs.testing.rpc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.opendcs.testing.kiwi.TestCase;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class KiwiClient
{
    public static final MediaType APPLICATION_JSON = MediaType.get("application/json");

    private final OkHttpClient client;
    private final String baseUrl;

    public KiwiClient(String url, String username, String password) throws IOException
    {

        baseUrl = url+"/json-rpc/";
        File cache = Files.createTempDirectory("kiwiokhttpcace").toFile();
        cache.deleteOnExit();
        /** For the love of sanity make sure this get removed before an actual release
         * this is just so I can focus on writing the actual RPC code without fiddling with
         * custom certificates.
         */
        TrustManager[] trustAllCerts = new TrustManager[]
        {
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
        };
        try
        {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            ConnectionSpec spec = new ConnectionSpec.Builder(true)        
                                .build();
            CookieJar cookieJar = new CookieJar() {
                Map<HttpUrl, List<Cookie>> cookies = Collections.synchronizedMap(new HashMap<>());
                @Override
                public List<Cookie> loadForRequest(HttpUrl url) 
                {
                    return cookies.computeIfAbsent(url, key -> new ArrayList<Cookie>());
                }

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookiesForUrl)
                {
                    cookies.put(url, cookiesForUrl);   
                }
            };
            client = new OkHttpClient.Builder().cache(new Cache(cache,30*1024*1024)) // 30MB
                                 .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager)trustAllCerts[0])
                                 .hostnameVerifier((hostname, session) -> true)
                                 .cookieJar(cookieJar)
                                 .build();

            this.login(username, password);
        }
        catch (Exception ex)
        {
            throw new IOException("Trust creation error.", ex);
            
        }
        
        
    }

    public void writeTestCase(TestCase tc) throws IOException
    {
        JSONRPC2Request rpcReq = new JSONRPC2Request("TestCase.create",1);
        Map<String,Object> params = new HashMap<>();
        params.put("summary", tc.getSummary());
        params.put("text", tc.getSteps());
        rpcReq.setPositionalParams(Arrays.asList(params));
        //rpcReq.setNamedParams(params);
        Request req = new Request.Builder()
                                 .url(baseUrl)
                                 .post(RequestBody.create(rpcReq.toString(),APPLICATION_JSON))
                                 .build();
        Response res = client.newCall(req).execute();
        
        if (res.code() != 200 || res.code() != 201)
        {
            System.out.println("HTTP result: " + res.code());
            System.out.println(res.body().string());
        }
    }

    private void login(String user, String password) throws IOException
    {
        JSONRPC2Request rpcRequest = new JSONRPC2Request("Auth.login", 0);
        Map<String,Object> params = new HashMap<>();
        params.put("username",user);
        params.put("password",password);
        rpcRequest.setNamedParams(params);
        Request req = new Request.Builder()
                                 .url(baseUrl)
                                 .post(RequestBody.create(rpcRequest.toString(),APPLICATION_JSON))
                                 .build();
        Response res = client.newCall(req).execute();
        JSONRPC2Response rpcResponse = new JSONRPC2Response(res.body().string(), 0);
        if (rpcResponse.getError() != null)
        {
            System.out.println(res.body().string());
        }
    }
}
