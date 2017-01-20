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

import net.roboconf.agent.internal.Agent;

/**
 * @author Vincent Zurczak - Linagora
 */
public class AgentLauncher {

	private static final String APP_NAME = "mongo-replicaset";
	private static final String ROOT_INSTANCE_NAME = "Mongo primary";

	private Agent agent;


	/**
	 * Launches an agent in memory and wait for 's' to be typed in into the console.
	 * @param args
	 */
	public static void main( String[] args ) {

		AgentLauncher launcher = new AgentLauncher();
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
	 * Launches the DM.
	 * @throws IOException
	 */
	private void initialize() throws IOException {

		DebugUtils.updateLoggingConfiguration( Level.FINER );

		this.agent = new Agent();
		this.agent.setMessageServerIp( "localhost" );
		this.agent.setMessageServerUsername( "guest" );
		this.agent.setMessageServerPassword( "guest" );
		this.agent.setIpAddress( "127.0.0.1" );
		this.agent.setApplicationName( APP_NAME );
		this.agent.setScopedInstancePath( "/" + ROOT_INSTANCE_NAME );
		this.agent.setTargetId( "no user data" );
		this.agent.setSimulatePlugins( false );

		this.agent.start();
	}


	/**
	 * Clean all the resources.
	 */
	private void clean() {

		if( this.agent != null )
			this.agent.stop();
	}
}
