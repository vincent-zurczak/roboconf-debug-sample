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
import java.io.IOException;

import net.roboconf.dm.management.Manager;
import net.roboconf.dm.management.exceptions.AlreadyExistingException;
import net.roboconf.dm.management.exceptions.InvalidApplicationException;
import net.roboconf.dm.rest.client.test.RestTestUtils;

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

		}
	}

	public void loadApplication() {

		// Initialize the DM
		Manager.INSTANCE.tryToChangeMessageServerIp( "localhost" );

		// Change the directory location for your own project
		try {
			Manager.INSTANCE.loadNewApplication( new File( "/home/vincent/git/roboconf-deployment/linagora-rse" ));

		} catch( AlreadyExistingException e ) {
			e.printStackTrace();

		} catch( InvalidApplicationException e ) {
			e.printStackTrace();

		} catch( IOException e ) {
			e.printStackTrace();
		}
	}
}
