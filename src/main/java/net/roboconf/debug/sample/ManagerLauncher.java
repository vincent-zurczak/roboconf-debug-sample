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
import java.util.logging.Level;

import org.junit.rules.TemporaryFolder;

import net.roboconf.debug.sample.DebugUtils.HttpServiceImpl;
import net.roboconf.dm.internal.test.TestTargetResolver;
import net.roboconf.dm.management.Manager;
import net.roboconf.dm.rest.services.internal.ServletRegistrationComponent;
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
	private HttpServiceImpl httpService;
	private ServletRegistrationComponent reg;


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
			try {
				launcher.clean();

			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Launches the DM and registers an application.
	 * @throws IOException
	 */
	private void initialize() throws Exception {

		// Create the HTTP service
		this.httpService = new DebugUtils.HttpServiceImpl( 9023 );

		// Prepare the logging configuration
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

		// Prepare the registration component
		ServletRegistrationComponent reg = new ServletRegistrationComponent();
		reg.setManager( this.manager );
		reg.setHttpService( this.httpService );

		// Enable CORS
		reg.setEnableCors( true );

		// Start the server only once the servlets were registered
		reg.starting();
		this.httpService.getServer().start();
	}


	/**
	 * Clean all the resources.
	 */
	private void clean() throws Exception {

		this.reg.stopping();
		if( this.manager != null )
			this.manager.stop();

		this.httpService.getServer().stop();
		this.folder.delete();
	}
}
