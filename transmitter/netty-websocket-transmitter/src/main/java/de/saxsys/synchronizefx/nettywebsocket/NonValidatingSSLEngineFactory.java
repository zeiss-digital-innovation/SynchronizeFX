/**
 * This file is part of SynchronizeFX.
 * 
 * Copyright (C) 2013 Saxonia Systems AG
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

package de.saxsys.synchronizefx.nettywebsocket;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.saxsys.synchronizefx.core.exceptions.SynchronizeFXException;

/**
 * A factory for {@link SSLEngine}s which doesn't validate the SSL certificate of the server.
 * 
 * @author Raik Bieniek <raik.bieniek@saxsys.de>
 */
class NonValidatingSSLEngineFactory implements X509TrustManager {

    private final SSLContext context;

    /**
     * Initializes this factory.
     * 
     * @throws SynchronizeFXException If TLS is not supported by the {@link SSLContext} of this JVM.
     */
    public NonValidatingSSLEngineFactory() throws SynchronizeFXException {
        context = createClientContext();
    }
    
    /**
     * Creates a new engine for TLS communication in client mode.
     * 
     * @return The new engine
     */
    public SSLEngine createClientEngine() {
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(true);
        return engine;
    }
    
    private SSLContext createClientContext() throws SynchronizeFXException {
        try {
            //as JDK >=7 is required there is no need to support older TLS versions.
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, new TrustManager[] {this }, null);
            return context;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new SynchronizeFXException("Could not initialize the encryption for the encrypted "
                    + "web socket connection to the server.", e);
        }
    }

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
