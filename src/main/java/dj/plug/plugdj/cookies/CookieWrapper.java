package dj.plug.plugdj.cookies;

import android.os.Build;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpCookie;

public class CookieWrapper implements Serializable {
    private transient HttpCookie cookie;

    public CookieWrapper(HttpCookie cookie) {
        this.cookie = cookie;
    }

    public HttpCookie getCookie() {
        return cookie;
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        output.defaultWriteObject();
        output.writeInt((Build.VERSION.SDK_INT));
        output.writeObject(cookie.getName());
        output.writeObject(cookie.getValue());
        output.writeObject(cookie.getComment());
        output.writeObject(cookie.getCommentURL());
        output.writeBoolean(cookie.getDiscard());
        output.writeObject(cookie.getDomain());
        if (Build.VERSION.SDK_INT >= 24) {
            output.writeBoolean(cookie.isHttpOnly());
        }
        output.writeLong(cookie.getMaxAge());
        output.writeObject(cookie.getPath());
        output.writeObject(cookie.getPortlist());
        output.writeBoolean(cookie.getSecure());
        output.writeInt(cookie.getVersion());
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        int storedSdkInt = input.readInt();
        String name = (String) input.readObject();
        String value = (String) input.readObject();
        cookie = new HttpCookie(name, value);
        cookie.setComment((String) input.readObject());
        cookie.setCommentURL((String) input.readObject());
        cookie.setDiscard(input.readBoolean());
        cookie.setDomain((String) input.readObject());
        if (Build.VERSION.SDK_INT >= 24 && storedSdkInt >= 24) {
            cookie.setHttpOnly(input.readBoolean());
        }
        cookie.setMaxAge(input.readLong());
        cookie.setPath((String) input.readObject());
        cookie.setPortlist((String) input.readObject());
        cookie.setSecure(input.readBoolean());
        cookie.setVersion(input.readInt());
    }

    @Override
    public int hashCode() {
        return cookie.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return this == other ||
                other instanceof CookieWrapper && cookie.equals(((CookieWrapper) other).getCookie());
    }

    @Override
    public String toString() {
        return cookie.toString();
    }
}
