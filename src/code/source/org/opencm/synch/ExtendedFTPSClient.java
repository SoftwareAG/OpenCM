package org.opencm.synch;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Locale;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;

import org.apache.commons.net.ftp.FTPSClient;

public class ExtendedFTPSClient extends FTPSClient {

  @Override
  protected void _prepareDataSocket_(final Socket socket) throws IOException {
    if(socket instanceof SSLSocket) {
      final SSLSession session = ((SSLSocket) _socket_).getSession();
      final SSLSessionContext context = session.getSessionContext();
      try {
        final Field sessionHostPortCache = context.getClass().getDeclaredField("sessionHostPortCache");
        sessionHostPortCache.setAccessible(true);
        final Object cache = sessionHostPortCache.get(context);
        final Method putMethod = cache.getClass().getDeclaredMethod("put", Object.class, Object.class);
        putMethod.setAccessible(true);
        final Method getHostMethod = socket.getClass().getDeclaredMethod("getHost");
        getHostMethod.setAccessible(true);
        Object host = getHostMethod.invoke(socket);
        final String key = String.format("%s:%s", host, String.valueOf(socket.getPort())).toLowerCase(Locale.ROOT);
        putMethod.invoke(cache, key, session);
      } catch(Exception e) {
    	  throw new RuntimeException(e);
      }
    }
  }

}