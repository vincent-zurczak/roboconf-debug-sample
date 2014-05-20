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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.roboconf.core.actions.ApplicationAction;
import net.roboconf.core.model.helpers.ComponentHelpers;
import net.roboconf.core.model.helpers.InstanceHelpers;
import net.roboconf.core.model.runtime.Component;
import net.roboconf.core.model.runtime.Graphs;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.dm.environment.iaas.IaasResolver;
import net.roboconf.dm.management.ManagedApplication;
import net.roboconf.dm.management.Manager;
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

		loadApplication();
		for( ;; ) {
			// nothing
		}
	}

	public void loadApplication() {

		// Change the logger settings
		Level defaultLevel = Level.FINEST;

		Logger logger = Logger.getLogger( "" );
		logger.setLevel( defaultLevel );
		for( Handler logHandler : logger.getHandlers())
			logHandler.setLevel( defaultLevel );


		// Change the directory location for your own project
		try {
			Manager.INSTANCE.tryToChangeMessageServerIp( "localhost" );
			ManagedApplication ma = Manager.INSTANCE.loadNewApplication( new File( "D:/lamp-legacy-1" ));

			// Use in-memory agents
			Manager.INSTANCE.setIaasResolver( new IaasResolver() {
				@Override
				public IaasInterface findIaasInterface( ManagedApplication ma, Instance instance ) throws IaasException {
					return super.findIaasHandler( IaasResolver.IAAS_IN_MEMORY );
				}
			});

			// All the sub-instances will use the logger plug-in
			for( Instance instance : InstanceHelpers.getAllInstances( ma.getApplication())) {
				if( instance.getParent() != null )
					instance.getComponent().setInstallerName( "logger" );
			}

			// Deploy everything automatically
			for( Instance instance : InstanceHelpers.getAllInstances( ma.getApplication())) {
				Manager.INSTANCE.perform(
						ma.getApplication().getName(),
						ApplicationAction.deploy.toString(),
						InstanceHelpers.computeInstancePath( instance ),
						false );
			}

			// Add fake children instances
			Component warComponent = new Component( "war" ).installerName( "bash" ).alias( "war" );
			Graphs graphs = ma.getApplication().getGraphs();
			Component tomcatComponent = ComponentHelpers.findComponent( graphs, "Tomcat" );
			ComponentHelpers.insertChild( tomcatComponent, warComponent );

			Instance tomcatInstance = InstanceHelpers.findInstanceByPath( ma.getApplication(), "/Tomcat VM 1/Tomcat" );
			Instance war1 = new Instance( "Hello World!" ).component( warComponent );
			InstanceHelpers.insertChild( tomcatInstance, war1 );

			for( int i=0; i<45; i++ ) {
				Instance war2 = new Instance( "ECOM-" + i ).component( warComponent );
				InstanceHelpers.insertChild( tomcatInstance, war2 );
			}

		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
}
