package com.couchbase.cblite.phonegap;

import android.annotation.SuppressLint;
import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.javascript.JavaScriptReplicationFilterCompiler;
import com.couchbase.lite.javascript.JavaScriptViewCompiler;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.router.URLStreamHandlerFactory;
import com.couchbase.lite.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class CBLite extends CordovaPlugin {

    private static final int DEFAULT_LISTEN_PORT = 5984;
    private boolean initFailed = false;
    private int listenPort;
    private Credentials allowedCredentials;

    private String baseUrl = "";
    private Database database = null;
    private LiteListener listener = null;
    private Manager manager;

    public CBLite() {
        super();
        System.out.println("CBLite() constructor called");
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        System.out.println("initialize() called");

        super.initialize(cordova, webView);
        initCBLite();
    }

    private void initCBLite() {
        try {
            allowedCredentials = new Credentials();

            URLStreamHandlerFactory.registerSelfIgnoreError();

            View.setCompiler(new JavaScriptViewCompiler());
            Database.setFilterCompiler(new JavaScriptReplicationFilterCompiler());

            Manager server = startCBLite(this.cordova.getActivity());

            listenPort = startCBLListener(DEFAULT_LISTEN_PORT, server, allowedCredentials);

            System.out.println("initCBLite() completed successfully");

        } catch (final Exception e) {
            e.printStackTrace();
            initFailed = true;
        }

    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callback) {

        System.out.println("--- action ---");
        System.out.println(action);

        if (action.equals("getURL")) {
            return getUrl(callback);
        }

        if (action.equals("getSampleSets")) {
            return getSampleSets(callback, args);
        }

        return false;
    }

    private boolean getUrl(final CallbackContext callback) {
        try {

            if (initFailed) {
                callback.error("Failed to initialize couchbase lite.  See console logs");
                return false;
            } else {
                @SuppressLint("DefaultLocale")
                String callbackRespone = String.format(
                        "http://%s:%s@localhost:%d/",
                        allowedCredentials.getLogin(),
                        allowedCredentials.getPassword(),
                        listenPort
                );
                baseUrl = callbackRespone;
                callback.success(callbackRespone);

                return true;
            }

        } catch (final Exception e) {
            e.printStackTrace();
            callback.error(e.getMessage());
        }
        return false;
    }

    private boolean getSampleSets(final CallbackContext callback, final JSONArray args) {

        String viewName = "sampleset";
        String dbName = "";

        try {
            dbName = args.getString(0);
            viewName = args.getString(1);
            System.out.println("--- dbName ---");
            System.out.println(dbName);
            System.out.println(manager.getAllDatabaseNames());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final Database database = getDb(dbName);
        final QueryOptions queryOptions = new QueryOptions();

        Query query = database.getView(viewName).createQuery();

        try {
            Map<String, Object> docs = database.getAllDocs(queryOptions);
            System.out.println("--- all docs ---");
            System.out.println(docs);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("--- queries ---");
            query.setMapOnly(true);
            QueryEnumerator result = query.run();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                System.out.println(row.getKey() + " " + row.getValue().toString());
            }
            callback.success(" ");
            return true;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    private Database getDb(final String dbName) {
        try {
            database = manager.getExistingDatabase(dbName);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return database;
    }

    protected Manager startCBLite(Context context) {
        try {
            Manager.enableLogging(Log.TAG, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_CHANGE_TRACKER, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_BLOB_STORE, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_LISTENER, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_MULTI_STREAM_WRITER, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_REMOTE_REQUEST, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_ROUTER, Log.VERBOSE);
            manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return manager;
    }

    private int startCBLListener(int listenPort, Manager manager, Credentials allowedCredentials) {
        listener = new LiteListener(manager, listenPort, allowedCredentials);
        int boundPort = listener.getListenPort();
        Thread thread = new Thread(listener);
        thread.start();
        return boundPort;
    }

    public void onResume(boolean multitasking) {
        System.out.println("CBLite.onResume() called");
    }

    public void onPause(boolean multitasking) {
        System.out.println("CBLite.onPause() called");
    }

    public void onDestroy() {
        System.out.println("CBLite.onDestroy() called");
        listener.stop();
    }

}