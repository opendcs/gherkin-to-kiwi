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

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KiwiClient
{
    public static final MediaType APPLICATION_JSON = MediaType.get("application/json");

    private final OkHttpClient client;
    private final String baseUrl;

    private final TestCaseRpc testCaseRpc;

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
            testCaseRpc = new TestCaseRpc(this);
        }
        catch (Exception ex)
        {
            throw new IOException("Trust creation error.", ex);
            
        }
    }

    public TestCaseRpc testcase()
    {
        return testCaseRpc;
    }

    private void login(String user, String password) throws IOException
    {
        JSONRPC2Request rpcRequest = new JSONRPC2Request("Auth.login", 0);
        Map<String,Object> params = new HashMap<>();
        params.put("username",user);
        params.put("password",password);
        rpcRequest.setNamedParams(params);
        rpcRequest(rpcRequest);
    }

    public JSONRPC2Response rpcRequest(JSONRPC2Request request) throws IOException
    {
        Request httpRequest = new Request.Builder()
                                 .url(baseUrl)
                                 .post(RequestBody.create(request.toString(),APPLICATION_JSON))
                                 .build();
        Response httpResponse = client.newCall(httpRequest).execute();
        if (!httpResponse.isSuccessful())
        {
            throw new IOException("HTTP Call failed with error " + httpResponse.code());
        }
        String body = httpResponse.body().string();
        JSONRPC2Response rpcResponse;
        try
        {
            rpcResponse = JSONRPC2Response.parse(body);
            if (rpcResponse.getError() != null)
            {
                throw new IOException("RPC call failed", rpcResponse.getError());
            }
            return rpcResponse;
        }
        catch (JSONRPC2ParseException ex)
        {
            throw new IOException("Invalid response from server", ex);
        }
    }
}
