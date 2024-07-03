package org.opendcs.testing.rpc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.opendcs.testing.util.ThrowingFunction;
import org.opendcs.testing.util.ThrowingSupplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import okhttp3.Cache;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class manages the HTTP/JsonRPC work to communicate with a given KiwiTCMS instance.
 */
public final class KiwiClient
{
    public static final MediaType APPLICATION_JSON = MediaType.get("application/json");

    private final OkHttpClient client;
    private final String baseUrl;

    private final TestCaseRpc testCaseRpc;
    private final ComponentRpc componentRpc;
    private final ProductRpc productRpc;
    private final PriorityRpc priorityRpc;
    private final CategoryRpc categoryRpc;
    private final TestPlanRpc testPlanRpc;
    private final TypeRpc typeRpc;
    private final VersionRpc versionRpc;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    public KiwiClient(String url, String username, String password) throws IOException
    {

        baseUrl = url + "/json-rpc/";
        File cache = Files.createTempDirectory("kiwiokhttpcache").toFile();
        cache.deleteOnExit();
        /**
         * For the love of sanity make sure this get removed before an actual release
         * this is just so I can focus on writing the actual RPC code without fiddling with
         * custom certificates.
         */
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager()
                {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return new java.security.cert.X509Certificate[] {};
                    }
                }
        };
        try
        {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CookieJar cookieJar = new CookieJar()
            {
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
            client = new OkHttpClient.Builder().cache(new Cache(cache, 30 * 1024 * 1024)) // 30MB
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .cookieJar(cookieJar)
                    .build();
            this.login(username, password);
            testCaseRpc = new TestCaseRpc(this);
            componentRpc = new ComponentRpc(this);
            productRpc = new ProductRpc(this);
            priorityRpc = new PriorityRpc(this);
            categoryRpc = new CategoryRpc(this);
            testPlanRpc = new TestPlanRpc(this);
            typeRpc = new TypeRpc(this);
            versionRpc = new VersionRpc(this);
        }
        catch (Exception ex)
        {
            throw new IOException("Trust creation error.", ex);
        }
    }

    /**
     * Retrieve RPC for test case operations.
     *
     * @return TestCaseRpc instance to use.
     */
    public TestCaseRpc testcase()
    {
        return testCaseRpc;
    }

    /**
     * Retrieve RPC for test plan operations.
     *
     * @return TestPlanRPC instance to use.
     */
    public TestPlanRpc testplan()
    {
        return testPlanRpc;
    }

    /**
     * Retrieve RPC for component operations.
     *
     * @return Component RPC instance to use.
     */
    public ComponentRpc component()
    {
        return componentRpc;
    }

    /**
     * Retrieve RPC for product operations.
     *
     * @return Product RPC instance to use
     */
    public ProductRpc product()
    {
        return productRpc;
    }

    /**
     * Retrieve RPC for priority operations.
     *
     * @return A priority RPC instance to use.
     */
    public PriorityRpc priority()
    {
        return priorityRpc;
    }

    /**
     * Retrieve RPC for category operations.
     *
     * @return A category RPC instance to use.
     */
    public CategoryRpc category()
    {
        return categoryRpc;
    }

    public TypeRpc type()
    {
        return typeRpc;
    }

    public VersionRpc version()
    {
        return versionRpc;
    }

    /**
     * Handle login to the Kiwi Instance.
     *
     * @param user username to use.
     * @param password password to use.
     * @throws IOException HTTP connection issues, or invalid credentials.
     */
    private void login(String user, String password) throws IOException
    {
        JSONRPC2Request rpcRequest = new JSONRPC2Request("Auth.login", 0);
        Map<String, Object> params = new HashMap<>();
        params.put("username", user);
        params.put("password", password);
        rpcRequest.setNamedParams(params);
        rpcRequest(rpcRequest);
    }

    /**
     * Performance an RPC request sequence.
     *
     * @param request
     *            prefilled request.
     * @return a valid JSONRPC2Response object
     * @throws IOException
     *             If there is an error with the HTTP request, or the JSONRPC2Response returns a failure.
     */
    JSONRPC2Response rpcRequest(JSONRPC2Request request) throws IOException
    {
        Request httpRequest = new Request.Builder()
                .url(baseUrl)
                .post(RequestBody.create(request.toString(), APPLICATION_JSON))
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

    /**
     * Creates a JSONRPC2Request with a random ID.
     *
     * @param method
     *            The JSON RPC method name.
     * @return
     */
    JSONRPC2Request createRequest(String method)
    {
        return new JSONRPC2Request(method, UUID.randomUUID().toString());
    }

    /**
     * Creates a JSONRPC2Request with a random ID and the given positional parameters.
     *
     * @param method
     *            The JSON RPC method name
     * @param positionalParams
     *            Positional parameters for the method
     * @return
     */
    JSONRPC2Request createRequest(String method, List<Object> positionalParams)
    {
        return createRequest(method, positionalParams, null);
    }

    /**
     * Creates a JSONRPC2Request with a random ID and given named parameters.
     *
     * @param method
     *            The JSON RPC method name.
     * @param namedParams
     *            named parameters for the method.
     * @return
     */
    JSONRPC2Request createRequest(String method, Map<String, Object> namedParams)
    {
        return createRequest(method, null, namedParams);
    }

    /**
     * Creates a JSONRPC2Request with a random ID and given named and positional parameters
     *
     * @param method
     *            The JSON RPC method name.
     * @param positionalParams
     *            The required positional parameters for the method.
     * @param namedParams
     *            THe required named parameters for the method.
     * @return
     */
    JSONRPC2Request createRequest(String method, List<Object> positionalParams, Map<String, Object> namedParams)
    {
        JSONRPC2Request rpcReq = createRequest(method);
        if (positionalParams != null)
        {
            rpcReq.setPositionalParams(positionalParams);
        }
        if (namedParams != null)
        {
            rpcReq.setNamedParams(namedParams);
        }
        return rpcReq;
    }

    /**
     * Call a KiwiTCMS JSON RPC method responsible for creating a new element in the database.
     *
     * @param <R>
     *            The return type
     * @param method
     *            JSON RPC method that will be called.
     * @param supplyNamed
     *            function that provides named parameter for the call. Can be null if none are used.
     * @param mapResult
     *            function that will map the returned JSONNode and turn it into type R
     * @param positionalArgs
     *            Variable list of positional arguments
     * @return a valid instance of type R
     * @throws IOException Any error during HTTP operations
     */
    public <R> R create(String method, ThrowingSupplier<Map<String, Object>> supplyNamed,
            ThrowingFunction<JsonNode, R> mapResult, Object... positionalArgs) throws IOException
    {
        List<Object> positional = Arrays.asList(positionalArgs);
        Map<String, Object> named = supplyNamed != null ? supplyNamed.get() : null;
        JSONRPC2Request rpcReq = createRequest(method, positional, named);
        JSONRPC2Response response = rpcRequest(rpcReq);
        String jsonString = response.getResult().toString();
        JsonNode node = jsonMapper.readTree(jsonString);
        try
        {
            return mapResult.apply(node);
        }
        catch (Throwable ex)
        {
            if (ex instanceof IOException)
            {
                throw (IOException) ex;
            }
            else
            {
                throw new IOException(ex);
            }
        }
    }

    /**
     * Call a KiwiTCMS JSON RPC method responsible for performing an search, returning a list of type R
     *
     * @param <R>
     *            Datatype of results.
     * @param method
     *            JSON RPC Method to be called.
     * @param mapResult
     *            Function that will take an individual JSONNode for each element and return an object of type R.
     * @param query
     *            map of query parameters used by the KiwiTCMS API backend
     * @return a List of type R, always valid. For no results an empty list is returned.
     * @throws IOException
     *             any issues with the Inputs, HTTP errors, or problems mapping a result.
     */
    public <R> List<R> filter(String method, ThrowingFunction<JsonNode, R> mapResult, Map<String, String> query)
            throws IOException
    {
        List<R> items = new ArrayList<>();
        JSONRPC2Request rpcReq = createRequest(method, Arrays.asList(query));
        JSONRPC2Response response = rpcRequest(rpcReq);
        String jsonString = response.getResult().toString();
        JsonNode node = jsonMapper.readTree(jsonString);
        for (JsonNode e : node)
        {
            try
            {
                R item = mapResult.apply(e);
                items.add(item);
            }
            catch (Throwable ex)
            {
                if (ex instanceof IOException)
                {
                    throw (IOException) ex;
                }
                else
                {
                    throw new IOException(ex);
                }
            }
        }
        return items;
    }

    /**
     * Call a KiwiTCMS JSON RPC method responsible for performing an update, return a result of type R
     *
     * @param <R>
     *            Datatype of result.
     * @param method
     *            JSON RPC Method to be called.
     * @param supplyNamed
     *            Function that supplies any named parameters required. May be null if none are used.
     * @param mapResult
     *            Function that takes the return JSON and converts it to an object of type R
     * @param id
     *            ID field of the given object to update.
     * @param positionalArgs
     *            Any positional arguments required by the function.
     * @return a valid instance of type R
     * @throws IOException
     *             any issues with the Inputs, HTTP errors, or mapping the result.
     */
    public <R> R update(String method,
            ThrowingSupplier<Map<String, Object>> supplyNamed, ThrowingFunction<JsonNode, R> mapResult, long id,
            Object... positionalArgs) throws IOException
    {
        if (id <= 0)
        {
            throw new IOException("Cannot update TestCase without ID.");
        }
        List<Object> positional = new ArrayList<>();
        positional.add(id);
        for (Object arg : positionalArgs)
        {
            positional.add(arg);
        }
        Map<String, Object> named = supplyNamed != null ? supplyNamed.get() : null;
        JSONRPC2Request rpcReq = createRequest(method, positional, named);
        JSONRPC2Response response = rpcRequest(rpcReq);
        JsonNode node = jsonMapper.readTree(response.getResult().toString());
        try
        {
            return mapResult.apply(node);
        }
        catch (Throwable ex)
        {
            if (ex instanceof IOException)
            {
                throw (IOException) ex;
            }
            else
            {
                throw new IOException(ex);
            }
        }
    }

    /**
     * Call a KiwiTCMS JSON RPC method responsible for performing an update, returning nothing.
     *
     * @param method
     *            JSON RPC Method to be called.
     * @param query
     *            JSON RPC query parameters. Same style of query as @see KiwiClient#filter.
     * @throws IOException any errors during HTTP communications.
     */
    public void remove(String method, Map<String, String> query) throws IOException
    {
        JSONRPC2Request rpcReq = createRequest(method, Arrays.asList(query));
        rpcRequest(rpcReq);
    }

    public void remove(String method, Object... args) throws IOException
    {
        JSONRPC2Request rpcReq = createRequest(method, Arrays.asList(args));
        rpcRequest(rpcReq);
    }
}
