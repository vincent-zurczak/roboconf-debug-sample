/**
 * Copyright 2014 Linagora, Université Joseph Fourier, Floralis
 *
 * The present code is developed in the scope of their joint LINAGORA -
 * Université Joseph Fourier - Floralis research program and is designated
 * as a "Result" pursuant to the terms and conditions of the LINAGORA
 * - Université Joseph Fourier - Floralis research program. Each copyright
 * holder of Results enumerated here above fully & independently holds complete
 * ownership of the complete Intellectual Property rights applicable to the whole
 * of said Results, and may freely exploit it in any manner which does not infringe
 * the moral rights of the other copyright holders.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.roboconf.debug.sample;

import java.util.Dictionary;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import net.roboconf.core.logging.RoboconfLogFormatter;

/**
 * @author Vincent Zurczak - Linagora
 */
public class DebugUtils {

	/**
	 * Private constructor.
	 */
	private DebugUtils() {
		// nothing
	}


	/**
	 * Updates the logging configuration
	 * @param logLevel the log level to use by default
	 */
	public static void updateLoggingConfiguration( Level logLevel ) {

		Filter logFilter = new Filter() {
			@Override
			public boolean isLoggable( LogRecord record ) {
				return record.getLoggerName().startsWith( "net.roboconf" );
			}
		};

		Logger logger = Logger.getLogger( "" );
		logger.setLevel( logLevel );
		logger.setFilter( logFilter );

		for( Handler logHandler : logger.getHandlers()) {
			logHandler.setLevel( logLevel );
			logHandler.setFilter( logFilter );
			logHandler.setFormatter( new RoboconfLogFormatter() {
				@Override
				public String format( LogRecord record ) {
					return "\n" + super.format( record );
				}
			});
		}
	}


	/**
	 * Found at https://github.com/ow2-chameleon/fuchsia/master/testing/src/test/java/org/ow2/chameleon/fuchsia/testing/common/services/HttpServiceImpl.java
	 * <p>
	 * Completed from Jetty tutorials.
	 * </p>
	 */
	public static class HttpServiceImpl implements HttpService {

	    private final Server server;
	    private final ServletContextHandler context;


	    /**
	     * Constructor.
	     * @param port
	     * @throws Exception
	     */
	    public HttpServiceImpl( int port ) throws Exception {
	        this.server = new Server( port );

	        this.context = new ServletContextHandler( ServletContextHandler.SESSIONS );
	        this.context.setContextPath( "/" );
	        this.server.setHandler( this.context );

	    }


	    @Override
		public void registerServlet( String context, Servlet servlet, Dictionary dictionary, HttpContext httpContext )
	    throws ServletException, NamespaceException {

	    	if( ! context.endsWith( "/*" ))
	    		context += "/*";

	        ServletHolder servletHolder = new ServletHolder( servlet );
	        servletHolder.setName((String) dictionary.get( "servlet-name" ));

	        this.context.addServlet( servletHolder, context );
	        System.out.println( "New servlet reachable at " + context );
	    }

	    @Override
		public void registerResources(String s, String s2, HttpContext httpContext) throws NamespaceException {
	        throw new UnsupportedOperationException( "Resource registering is not allowed in the mock implementation of httpservice" );
	    }

	    @Override
		public void unregister(String s) {
	    	// nothing
	    }

	    @Override
		public HttpContext createDefaultHttpContext() {
	        return null;
	    }

	    public Server getServer() {
	        return this.server;
	    }
	}
}
