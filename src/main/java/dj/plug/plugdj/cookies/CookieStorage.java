package dj.plug.plugdj.cookies;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookieStorage {
    public static final String TAG = "CookieStorage";
    public static final String FILENAME = "cookiemap.obj";

    public static void loadCookies(Context context) {
        if (CookieHandler.getDefault() == null) {
            CookieHandler.setDefault(loadCookieManager(getCookieFile(context)));
            Log.v(TAG, "CookieManager set.");
        } else {
            Log.v(TAG, "CookieManager already set.");
        }
    }

    public static void storeCookies(Context context) {
        CookieHandler cookieHandler = CookieHandler.getDefault();
        if (cookieHandler instanceof CookieManager) {
            storeCookieManager(getCookieFile(context), (CookieManager) cookieHandler);
            Log.v(TAG, "CookieManager stored.");
        } else {
            Log.e(TAG, "Default CookieManager not set.");
        }
    }

    private static File getCookieFile(Context context) {
        return new File(context.getCacheDir(), FILENAME);
    }

    // Can't check type because of type erasure, and we already catch the potential ClassCastException
    @SuppressWarnings("unchecked")
    private static CookieManager loadCookieManager(File file) {
        if (!file.canRead()) return new CookieManager();
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(file));
            return mapToCookieManager((Map<URI, List<CookieWrapper>>) input.readObject());
        } catch (IOException | ClassCastException | ClassNotFoundException e) {
            Log.e(TAG, "Can't read object: " + e.getMessage());
            return new CookieManager();
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException e) {
                Log.e(TAG, "Can't close input: " + e.getMessage());
            }
        }
    }

    private static void storeCookieManager(File file, CookieManager cookieManager) {
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(file));
            output.writeObject(cookieManagerToMap(cookieManager));
        } catch (IOException e) {
            Log.e(TAG, "Can't write object: " + e.getMessage());
        } finally {
            if (output != null) try {
                output.close();
            } catch (IOException e) {
                Log.e(TAG, "Can't close output: " + e.getMessage());
            }
        }
    }

    private static Map<URI, List<CookieWrapper>> cookieManagerToMap(CookieManager cookieManager) {
        CookieStore cookieStore = cookieManager.getCookieStore();
        Map<URI, List<CookieWrapper>> uriIndex = new HashMap<>();
        List<CookieWrapper> indexedCookies = new ArrayList<>();
        for (URI uri : cookieStore.getURIs()) {
            List<CookieWrapper> wrappedCookies = wrapCookies(cookieStore.get(uri));
            uriIndex.put(uri, wrappedCookies);
            indexedCookies.addAll(wrappedCookies);
        }
        List<CookieWrapper> remainingCookies = wrapCookies(cookieStore.getCookies());
        remainingCookies.removeAll(indexedCookies);
        uriIndex.put(null, remainingCookies);
        return uriIndex;
    }

    private static CookieManager mapToCookieManager(Map<URI, List<CookieWrapper>> uriIndex) {
        CookieManager cookieManager = new CookieManager();
        CookieStore cookieStore = cookieManager.getCookieStore();
        for (Map.Entry<URI, List<CookieWrapper>> entry : uriIndex.entrySet()) {
            URI uri = entry.getKey();
            for (CookieWrapper cookieWrapper : entry.getValue()) {
                HttpCookie cookie = cookieWrapper.getCookie();
                if (!cookie.hasExpired()) {
                    cookieStore.add(uri, cookie);
                }
            }
        }
        return cookieManager;
    }

    private static List<CookieWrapper> wrapCookies(List<HttpCookie> cookies) {
        List<CookieWrapper> wrappedCookies = new ArrayList<>(cookies.size());
        for (HttpCookie cookie : cookies) {
            if (!cookie.hasExpired()) {
                wrappedCookies.add(new CookieWrapper(cookie));
            }
        }
        return wrappedCookies;
    }
}
