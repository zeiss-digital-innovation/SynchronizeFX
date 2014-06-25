/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013-2014 Saxonia Systems AG
 *
 * SynchronizeFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SynchronizeFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SynchronizeFX. If not, see <http://www.gnu.org/licenses/>.
 */

package de.saxsys.synchronizefx.netty.base;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

import io.netty.channel.Channel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for {@link SSLEngine}s which doesn't validate the SSL certificate of the server.
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 */
public final class NonValidatingSSLEngineFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NonValidatingSSLEngineFactory.class);
    
    private static SSLContext context;

    private NonValidatingSSLEngineFactory() {
    }

    /**
     * Creates a new {@link SslHandler} in client or server mode.
     * 
     * @param clientMode if <code>true</code> a client engine is created, if <code>false</code> a server engine.
     * @return The new handler
     */
    public static SslHandler createSslHandler(final boolean clientMode) {
        final SSLEngine engine = createEngine(clientMode);
        final SslHandler handler = new SslHandler(engine);
        handler.handshakeFuture().addListener(new GenericFutureListener<Future<? super Channel>>() {
            @Override
            public void operationComplete(final Future<? super Channel> future) throws Exception {
                LOG.debug("Using cipher " + engine.getSession().getCipherSuite()
                        + " for the encrypted connection to the server.");
            }
        });
        return handler;
    }

    /**
     * Creates a new engine for TLS communication in client or server mode.
     * 
     * @param clientMode if <code>true</code> a client engine is created, if <code>false</code> a server engine.
     * @return The new engine
     */
    public static SSLEngine createEngine(final boolean clientMode) {
        if (context == null) {
            context = createContext();
        }
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(clientMode);
        return engine;
    }

    private static SSLContext createContext() throws SynchronizeFXException {
        try {
            // as JDK >=7 is required there is no need to support older TLS versions.
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, new TrustManager[] {new NonValidatingTrustManager() }, null);
            return context;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new SynchronizeFXException("Could not initialize the encryption for the encrypted "
                    + "web socket connection to the server.", e);
        }
    }

    /**
     * A trust manager that trusts every certificate.
     */
    private static final class NonValidatingTrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // accept everything
            return null;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
            // nothing to do here
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
            // nothing to do here
        }
    }
}
