package com.terracom.qrpttbeta.servers;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.Proxy.Type.HTTP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpRequest {

    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String ENCODING_GZIP = "gzip";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_SERVER = "Server";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_GET = "GET";
    public static final String PARAM_CHARSET = "charset";
    private static SSLSocketFactory TRUSTED_FACTORY;
    private static HostnameVerifier TRUSTED_VERIFIER;

    private static String getValidCharset(final String charset) {
        if (charset != null && charset.length() > 0)
            return charset;
        else
            return CHARSET_UTF8;
    }

    private static SSLSocketFactory getTrustedFactory()
            throws HttpRequestException {
        if (TRUSTED_FACTORY == null) {
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }
            }};
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustAllCerts, new SecureRandom());
                TRUSTED_FACTORY = context.getSocketFactory();
            } catch (GeneralSecurityException e) {
                IOException ioException = new IOException(
                        "Security exception configuring SSL context");
                ioException.initCause(e);
                throw new HttpRequestException(ioException);
            }
        }
        return TRUSTED_FACTORY;
    }

    private static HostnameVerifier getTrustedVerifier() {
        if (TRUSTED_VERIFIER == null)
            TRUSTED_VERIFIER = new HostnameVerifier() {

                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
        return TRUSTED_VERIFIER;
    }

    private static StringBuilder addPathSeparator(final String baseUrl,
                                                  final StringBuilder result) {
        if (baseUrl.indexOf(':') + 2 == baseUrl.lastIndexOf('/'))
            result.append('/');
        return result;
    }

    private static StringBuilder addParamPrefix(final String baseUrl,
                                                final StringBuilder result) {
        final int queryStart = baseUrl.indexOf('?');
        final int lastChar = result.length() - 1;
        if (queryStart == -1)
            result.append('?');
        else if (queryStart < lastChar && baseUrl.charAt(lastChar) != '&')
            result.append('&');
        return result;
    }

    private static StringBuilder addParam(final Object key, Object value,
                                          final StringBuilder result) {
        if (value != null && value.getClass().isArray())
            value = arrayToList(value);
        if (value instanceof Iterable<?>) {
            Iterator<?> iterator = ((Iterable<?>) value).iterator();
            while (iterator.hasNext()) {
                result.append(key);
                result.append("[]=");
                Object element = iterator.next();
                if (element != null)
                    result.append(element);
                if (iterator.hasNext())
                    result.append("&");
            }
        } else {
            result.append(key);
            result.append("=");
            if (value != null)
                result.append(value);
        }
        return result;
    }

    public interface ConnectionFactory {
        HttpURLConnection create(URL url) throws IOException;

        ConnectionFactory DEFAULT = new ConnectionFactory() {
            public HttpURLConnection create(URL url) throws IOException {
                return (HttpURLConnection) url.openConnection();
            }
        };
    }

    private static ConnectionFactory CONNECTION_FACTORY = ConnectionFactory.DEFAULT;

    public interface UploadProgress {
        void onUpload(long uploaded, long total);

        UploadProgress DEFAULT = new UploadProgress() {
            public void onUpload(long uploaded, long total) {
            }
        };
    }

    public static class Base64 {
        private Base64() {
        }
    }

    public static class HttpRequestException extends RuntimeException {

        private static final long serialVersionUID = -1170466989781746231L;

        public HttpRequestException(final IOException cause) {
            super(cause);
        }

        @Override
        public IOException getCause() {
            return (IOException) super.getCause();
        }
    }

    protected static abstract class Operation<V> implements Callable<V> {
        protected abstract V run() throws HttpRequestException, IOException;

        protected abstract void done() throws IOException;

        public V call() throws HttpRequestException {
            boolean thrown = false;
            try {
                return run();
            } catch (HttpRequestException e) {
                thrown = true;
                throw e;
            } catch (IOException e) {
                thrown = true;
                throw new HttpRequestException(e);
            } finally {
                try {
                    done();
                } catch (IOException e) {
                    if (!thrown)
                        throw new HttpRequestException(e);
                }
            }
        }
    }

    protected static abstract class CloseOperation<V> extends Operation<V> {

        private final Closeable closeable;
        private final boolean ignoreCloseExceptions;

        protected CloseOperation(final Closeable closeable,
                                 final boolean ignoreCloseExceptions) {
            this.closeable = closeable;
            this.ignoreCloseExceptions = ignoreCloseExceptions;
        }

        @Override
        protected void done() throws IOException {
            if (closeable instanceof Flushable)
                ((Flushable) closeable).flush();
            if (ignoreCloseExceptions)
                try {
                    closeable.close();
                } catch (IOException e) {
                }
            else
                closeable.close();
        }
    }

    public static class RequestOutputStream extends BufferedOutputStream {

        public RequestOutputStream(final OutputStream stream, final String charset,
                                   final int bufferSize) {
            super(stream, bufferSize);
        }
    }

    private static List<Object> arrayToList(final Object array) {
        if (array instanceof Object[])
            return Arrays.asList((Object[]) array);

        List<Object> result = new ArrayList<Object>();
        if (array instanceof int[])
            for (int value : (int[]) array) result.add(value);
        else if (array instanceof boolean[])
            for (boolean value : (boolean[]) array) result.add(value);
        else if (array instanceof long[])
            for (long value : (long[]) array) result.add(value);
        else if (array instanceof float[])
            for (float value : (float[]) array) result.add(value);
        else if (array instanceof double[])
            for (double value : (double[]) array) result.add(value);
        else if (array instanceof short[])
            for (short value : (short[]) array) result.add(value);
        else if (array instanceof byte[])
            for (byte value : (byte[]) array) result.add(value);
        else if (array instanceof char[])
            for (char value : (char[]) array) result.add(value);
        return result;
    }

    public static String encode(final CharSequence url)
            throws HttpRequestException {
        URL parsed;
        try {
            parsed = new URL(url.toString());
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }

        String host = parsed.getHost();
        int port = parsed.getPort();
        if (port != -1)
            host = host + ':' + Integer.toString(port);

        try {
            String encoded = new URI(parsed.getProtocol(), host, parsed.getPath(),
                    parsed.getQuery(), null).toASCIIString();
            int paramsStart = encoded.indexOf('?');
            if (paramsStart > 0 && paramsStart + 1 < encoded.length())
                encoded = encoded.substring(0, paramsStart + 1)
                        + encoded.substring(paramsStart + 1).replace("+", "%2B");
            return encoded;
        } catch (URISyntaxException e) {
            IOException io = new IOException("Parsing URI failed");
            io.initCause(e);
            throw new HttpRequestException(io);
        }
    }

    public static String append(final CharSequence url, final Map<?, ?> params) {
        final String baseUrl = url.toString();
        if (params == null || params.isEmpty())
            return baseUrl;

        final StringBuilder result = new StringBuilder(baseUrl);

        addPathSeparator(baseUrl, result);
        addParamPrefix(baseUrl, result);
        Entry<?, ?> entry;
        Iterator<?> iterator = params.entrySet().iterator();
        entry = (Entry<?, ?>) iterator.next();
        addParam(entry.getKey().toString(), entry.getValue(), result);

        while (iterator.hasNext()) {
            result.append('&');
            entry = (Entry<?, ?>) iterator.next();
            addParam(entry.getKey().toString(), entry.getValue(), result);
        }
        return result.toString();
    }

    public static String append(final CharSequence url, final Object... params) {
        final String baseUrl = url.toString();
        if (params == null || params.length == 0)
            return baseUrl;

        if (params.length % 2 != 0)
            throw new IllegalArgumentException(
                    "Must specify an even number of parameter names/values");

        final StringBuilder result = new StringBuilder(baseUrl);

        addPathSeparator(baseUrl, result);
        addParamPrefix(baseUrl, result);
        addParam(params[0], params[1], result);

        for (int i = 2; i < params.length; i += 2) {
            result.append('&');
            addParam(params[i], params[i + 1], result);
        }
        return result.toString();
    }

    public static HttpRequest get(final CharSequence url)
            throws HttpRequestException {
        return new HttpRequest(url, METHOD_GET);
    }

    public static HttpRequest get(final URL url) throws HttpRequestException {
        return new HttpRequest(url, METHOD_GET);
    }

    public static HttpRequest get(final CharSequence baseUrl,
                                  final Map<?, ?> params, final boolean encode) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    public static HttpRequest get(final CharSequence baseUrl,
                                  final boolean encode, final Object... params) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    public static HttpRequest delete(final CharSequence url)
            throws HttpRequestException {
        return new HttpRequest(url, METHOD_DELETE);
    }

    public static HttpRequest delete(final URL url) throws HttpRequestException {
        return new HttpRequest(url, METHOD_DELETE);
    }

    public static HttpRequest delete(final CharSequence baseUrl,
                                     final Map<?, ?> params, final boolean encode) {
        String url = append(baseUrl, params);
        return delete(encode ? encode(url) : url);
    }

    public static HttpRequest delete(final CharSequence baseUrl,
                                     final boolean encode, final Object... params) {
        String url = append(baseUrl, params);
        return delete(encode ? encode(url) : url);
    }

    private HttpURLConnection connection = null;
    private final URL url;
    private final String requestMethod;
    private RequestOutputStream output;
    private boolean ignoreCloseExceptions = true;
    private boolean uncompress = false;
    private int bufferSize = 8192;
    private long totalSize = -1;
    private long totalWritten = 0;
    private UploadProgress progress = UploadProgress.DEFAULT;

    public HttpRequest(final CharSequence url, final String method)
            throws HttpRequestException {
        try {
            this.url = new URL(url.toString());
        } catch (MalformedURLException e) {
            throw new HttpRequestException(e);
        }
        this.requestMethod = method;
    }

    public HttpRequest(final URL url, final String method)
            throws HttpRequestException {
        this.url = url;
        this.requestMethod = method;
    }

    private HttpURLConnection createConnection() {
        try {
            final HttpURLConnection connection;
            connection = CONNECTION_FACTORY.create(url);
            connection.setRequestMethod(requestMethod);
            return connection;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    @Override
    public String toString() {
        return method() + ' ' + url();
    }

    public HttpURLConnection getConnection() {
        if (connection == null)
            connection = createConnection();
        return connection;
    }

    public int code() throws HttpRequestException {
        try {
            closeOutput();
            return getConnection().getResponseCode();
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest disconnect() {
        getConnection().disconnect();
        return this;
    }

    protected ByteArrayOutputStream byteStream() {
        final int size = contentLength();
        if (size > 0)
            return new ByteArrayOutputStream(size);
        else
            return new ByteArrayOutputStream();
    }

    public String body(final String charset) throws HttpRequestException {
        final ByteArrayOutputStream output = byteStream();
        try {
            copy(buffer(), output);
            return output.toString(getValidCharset(charset));
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public String body() throws HttpRequestException {
        return body(charset());
    }

    public BufferedInputStream buffer() throws HttpRequestException {
        return new BufferedInputStream(stream(), bufferSize);
    }

    public InputStream stream() throws HttpRequestException {
        InputStream stream;
        if (code() < HTTP_BAD_REQUEST)
            try {
                stream = getConnection().getInputStream();
            } catch (IOException e) {
                throw new HttpRequestException(e);
            }
        else {
            stream = getConnection().getErrorStream();
            if (stream == null)
                try {
                    stream = getConnection().getInputStream();
                } catch (IOException e) {
                    if (contentLength() > 0)
                        throw new HttpRequestException(e);
                    else
                        stream = new ByteArrayInputStream(new byte[0]);
                }
        }

        if (!uncompress || !ENCODING_GZIP.equals(contentEncoding()))
            return stream;
        else
            try {
                return new GZIPInputStream(stream);
            } catch (IOException e) {
                throw new HttpRequestException(e);
            }
    }

    public String header(final String name) throws HttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderField(name);
    }

    public int intHeader(final String name) throws HttpRequestException {
        return intHeader(name, -1);
    }

    public int intHeader(final String name, final int defaultValue)
            throws HttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldInt(name, defaultValue);
    }


    public String parameter(final String headerName, final String paramName) {
        return getParam(header(headerName), paramName);
    }

    protected String getParam(final String value, final String paramName) {
        if (value == null || value.length() == 0)
            return null;

        final int length = value.length();
        int start = value.indexOf(';') + 1;
        if (start == 0 || start == length)
            return null;

        int end = value.indexOf(';', start);
        if (end == -1)
            end = length;

        while (start < end) {
            int nameEnd = value.indexOf('=', start);
            if (nameEnd != -1 && nameEnd < end
                    && paramName.equals(value.substring(start, nameEnd).trim())) {
                String paramValue = value.substring(nameEnd + 1, end).trim();
                int valueLength = paramValue.length();
                if (valueLength != 0)
                    if (valueLength > 2 && '"' == paramValue.charAt(0)
                            && '"' == paramValue.charAt(valueLength - 1))
                        return paramValue.substring(1, valueLength - 1);
                    else
                        return paramValue;
            }

            start = end + 1;
            end = value.indexOf(';', start);
            if (end == -1)
                end = length;
        }
        return null;
    }

    public String charset() {
        return parameter(HEADER_CONTENT_TYPE, PARAM_CHARSET);
    }

    public String contentEncoding() {
        return header(HEADER_CONTENT_ENCODING);
    }

    public String server() {
        return header(HEADER_SERVER);
    }

    public int contentLength() {
        return intHeader(HEADER_CONTENT_LENGTH);
    }

    protected HttpRequest copy(final InputStream input, final OutputStream output)
            throws IOException {
        return new CloseOperation<HttpRequest>(input, ignoreCloseExceptions) {

            @Override
            public HttpRequest run() throws IOException {
                final byte[] buffer = new byte[bufferSize];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    totalWritten += read;
                    progress.onUpload(totalWritten, totalSize);
                }
                return HttpRequest.this;
            }
        }.call();
    }


    protected HttpRequest copy(final Reader input, final Writer output)
            throws IOException {
        return new CloseOperation<HttpRequest>(input, ignoreCloseExceptions) {

            @Override
            public HttpRequest run() throws IOException {
                final char[] buffer = new char[bufferSize];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    totalWritten += read;
                    progress.onUpload(totalWritten, -1);
                }
                return HttpRequest.this;
            }
        }.call();
    }

    public HttpRequest progress(final UploadProgress callback) {
        if (callback == null)
            progress = UploadProgress.DEFAULT;
        else
            progress = callback;
        return this;
    }

    protected HttpRequest closeOutput() throws IOException {
        progress(null);
        if (output == null)
            return this;
        if (ignoreCloseExceptions)
            try {
                output.close();
            } catch (IOException ignored) {
            }
        else
            output.close();
        output = null;
        return this;
    }

    protected HttpRequest closeOutputQuietly() throws HttpRequestException {
        try {
            return closeOutput();
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest trustAllCerts() throws HttpRequestException {
        final HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection)
            ((HttpsURLConnection) connection)
                    .setSSLSocketFactory(getTrustedFactory());
        return this;
    }

    public HttpRequest trustAllHosts() {
        final HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection)
            ((HttpsURLConnection) connection)
                    .setHostnameVerifier(getTrustedVerifier());
        return this;
    }

    public URL url() {
        return getConnection().getURL();
    }

    public String method() {
        return getConnection().getRequestMethod();
    }
}