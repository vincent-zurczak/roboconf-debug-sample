/**
 * Copyright 2014 Linagora, Université Joseph Fourier
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

import java.io.File;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.roboconf.core.logging.RoboconfLogFormatter;
import net.roboconf.core.model.helpers.InstanceHelpers;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.dm.environment.iaas.IaasResolver;
import net.roboconf.dm.management.ManagedApplication;
import net.roboconf.dm.management.Manager;
import net.roboconf.dm.management.ManagerConfiguration;
import net.roboconf.dm.rest.client.test.RestTestUtils;
import net.roboconf.iaas.api.IaasException;
import net.roboconf.iaas.api.IaasInterface;

import org.junit.Test;

import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly2.web.GrizzlyWebTestContainerFactory;

/**
 * @author Vincent Zurczak - Linagora
 */
public class DebugSample extends JerseyTest {

	@Override
	protected AppDescriptor configure() {
		return RestTestUtils.buildTestDescriptor();
	}


	@Override
    public TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }


	@Test
	public void testApplications() throws Exception {

		try {
			loadApplication();

		} catch( Exception e ) {
			e.printStackTrace();
		}

		for( ;; ) {
			Thread.sleep( 60000 );
		}
	}


	/**
	 * Initializes and loads an application.
	 * @throws Exception
	 */
	public void loadApplication() throws Exception {

		// Change the logger settings
		Level defaultLevel = Level.INFO;
		Filter logFilter = new Filter() {
			@Override
			public boolean isLoggable( LogRecord record ) {
				return record.getLoggerName().startsWith( "net.roboconf" );
			}
		};

		Logger logger = Logger.getLogger( "" );
		logger.setLevel( defaultLevel );
		logger.setFilter( logFilter );

		for( Handler logHandler : logger.getHandlers()) {
			logHandler.setLevel( defaultLevel );
			logHandler.setFilter( logFilter );
			logHandler.setFormatter( new RoboconfLogFormatter() {
				@Override
				public String format( LogRecord record ) {
					return "\n" + super.format( record );
				}
			});
		}


		// Create default settings
		File temporaryDirectory = new File( System.getProperty( "java.io.tmpdir" ), "roboconf_config" );
		ManagerConfiguration conf = ManagerConfiguration.createConfiguration( temporaryDirectory, "RabbitMQ's IP", "RabbitMQ's user name", "RabbitMQ's password" );

		// Or, if you use a local installation of RabbitMQ
		// conf = ManagerConfiguration.createConfiguration( temporaryDirectory );

		// Or if you static installation (in the user's directory...
		// ... or if you have set the ROBOCONF_DM_DIR envrionment variable)
		// File configurationDirectory = ManagerConfiguration.findConfigurationDirectory();
		// conf = ManagerConfiguration.loadConfiguration( configurationDirectory );

		// Initialize the DM.
		Manager.INSTANCE.initialize( conf );

		// Use in-memory agents.
		// This is just an example of model manipulation.
		Manager.INSTANCE.setIaasResolver( new IaasResolver() {
			@Override
			public IaasInterface findIaasInterface( ManagedApplication ma, Instance instance ) throws IaasException {
				return super.findIaasHandler( IaasResolver.IAAS_IN_MEMORY );
			}
		});

		// Load an application
		ManagedApplication ma = Manager.INSTANCE.loadNewApplication( new File( "D:/lamp-legacy-1" ));

		// All the sub-instances will use the logger plug-in.
		// Another example of model manipulation.
		for( Instance instance : InstanceHelpers.getAllInstances( ma.getApplication())) {
			if( instance.getParent() != null )
				instance.getComponent().setInstallerName( "logger" );
		}

		// Deploy and start everything
		Manager.INSTANCE.deployAndStartAll( ma, null );
	}
}
