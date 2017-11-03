package com.couchbase.cblite.phonegap;

import android.annotation.SuppressLint;
import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
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

import java.io.IOException;
import java.util.List;
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
        if (action.equals("getURL")) {
            return getUrl(callback);
        }

        if (action.equals("getSampleSets")) {
            return getSampleSets(callback);
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

    private boolean getSampleSets(CallbackContext callback) {
        final Database database = getDb();
        final String viewName = "sampleset";
        final QueryOptions queryOptions = new QueryOptions();
        final View sampleSetsView = database.getView(viewName);

        if (sampleSetsView.getMap() == null) {
            sampleSetsView.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if ("sampleset".equals(type)) {
                        emitter.emit(document.get("name"), null);
                    }
                }
            }, "1.0");
        }

        try {
            List<QueryRow> sampleSets = sampleSetsView.query(queryOptions);
            System.out.println("--- view ---");
            System.out.println(sampleSets);
            return true;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Database getDb() {
        try {
            database = manager.getDatabase("app");
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