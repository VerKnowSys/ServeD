package ssh.tools


import actors.Actor
import com.sshtools.j2ssh.authentication.{PasswordAuthenticationClient, AuthenticationProtocolState}
import com.sshtools.j2ssh.SshClient
import deployer.Deployer
import java.io.File
import java.util.ArrayList
import org.apache.log4j.Logger
import prefs.{Preferences, PreferencesActor}
/**
 * User: dmilith
 * Date: Jun 27, 2009
 * Time: 3:15:04 PM
 */

object SSHActor extends Actor {

	private val logger = Logger.getLogger(SSHActor.getClass)
	private var prefs: Preferences = null
	private var debug: Boolean = false
	private var host = ""
	private var port = 22
	private var userName = ""
	private var password = ""
	private var uuid = ""
	val ssh = new SshClient

	override
	def act = {
		Actor.loop {
			react {
				case s: Preferences => {
					prefs = s
					debug = prefs.getb("debug")
					host = prefs.get("sshHost")
					port = prefs.geti("sshPort")
					userName = prefs.get("sshUserName")
					password = prefs.get("sshPassword")
					connect
					auth
					act
				}
				case 'Quit => {
					PreferencesActor ! 'Quit
					disconnect
					Deployer ! 'Quit
					exit
				}
				case ('PerformTasks, x: ArrayList[File], deployUuid: String) => {
					logger.info("Performing tasks with given list of files: " + x.toArray.map{ a => a })
					uuid = deployUuid
					performTasksAndQuit
					act
				}
			}
		}
	}

	def performTasksAndQuit = {
		//Open the SFTP channel
		val client = ssh.openSftpClient
		val client2 = ssh.openSessionChannel
		println("XXXX" + client2.executeCommand("mkdir /tmp/DUPA"))
		client2.close
		//Send the file
		client.put("/tmp/FileCache/http:__www_google_pl_images_nav_logo4_png","/tmp/dupa" + uuid + ".png")
		//disconnect
		client.quit
		this ! 'Quit
	}

	def auth = {
		val passwordAuthenticationClient = new PasswordAuthenticationClient
		passwordAuthenticationClient.setUsername(userName)
		passwordAuthenticationClient.setPassword(password)
		val result = ssh.authenticate(passwordAuthenticationClient)
		if (result != AuthenticationProtocolState.COMPLETE) {
			 logger.error("Login to " + host + ":" + port + " " + userName + "/" + password + " failed");
		}
	}

	def connect = ssh.connect(host, port)

	def disconnect = ssh.disconnect
	
}