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

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.rules.TemporaryFolder;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;

import net.roboconf.dm.internal.test.TestTargetResolver;
import net.roboconf.dm.management.Manager;
import net.roboconf.dm.rest.services.internal.RestApplication;
import net.roboconf.messaging.api.MessagingConstants;
import net.roboconf.messaging.api.internal.client.test.TestClientFactory;

/**
 * @author Vincent Zurczak - Linagora
 */
public class ManagerLauncher {

	private static final String REST_URI = "http://localhost:8090";
	private static final String APP_LOCATION = "/home/vzurczak/workspaces/roboconf/roboconf-examples/mongo-replicaset/target/mongo-replicaset-0.2-SNAPSHOT";

	private final TemporaryFolder folder = new TemporaryFolder();
	private Manager manager;
	private HttpServer httpServer;


	/**
	 * Launches a manager in memory and wait for 's' to be typed in into the console.
	 * <p>
	 * The web console can be used (it must be launched separately).
	 * </p>
	 *
	 * @param args
	 */
	public static void main( String[] args ) {

		ManagerLauncher launcher = new ManagerLauncher();
		try {
			launcher.initialize();
			for( char c = (char) System.in.read(); c != 's'; )
				Thread.sleep( 5000 );

		} catch( Exception e ) {
			e.printStackTrace();

		} finally {
			launcher.clean();
		}
	}


	/**
	 * Launches the DM and registers an application.
	 * @throws IOException
	 */
	private void initialize() throws Exception {

		DebugUtils.updateLoggingConfiguration( Level.FINE );
		this.folder.create();

		// Configure the DM
		this.manager = new Manager();
		this.manager.setTargetResolver( new TestTargetResolver());
		this.manager.configurationMngr().setWorkingDirectory( this.folder.newFolder());
		this.manager.setMessagingType( MessagingConstants.FACTORY_TEST );
		this.manager.start();

		// Reconfigure
		this.manager.addMessagingFactory( new TestClientFactory());
		this.manager.reconfigure();

		// this.manager.targetAppears( new Ec2IaasHandler());
		//this.manager.loadNewApplication( new File( APP_LOCATION ));

		URI uri = UriBuilder.fromUri( REST_URI ).build();
		RestApplication restApp = new RestApplication( this.manager );
		this.httpServer = GrizzlyServerFactory.createHttpServer( uri, restApp );
	}


	/**
	 * Clean all the resources.
	 */
	private void clean() {

		if( this.manager != null )
			this.manager.stop();

		this.folder.delete();
		if( this.httpServer != null )
			this.httpServer.stop();
	}
}
