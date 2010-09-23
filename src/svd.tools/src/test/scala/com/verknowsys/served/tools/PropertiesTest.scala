package com.verknowsys.served.tools

import org.specs._
import java.io._

class PropertiesTest extends SpecificationWithJUnit {
	final val configFilename = "/tmp/config.properties"

	"Properties object" should {
		doBefore { setupConfigFile }
		"read from file" in {
			val props = new Properties(configFilename)
			props("app.existing.config.one") must_== Some("foo bar baz")
			props("app.existing.other.config.two") must_== Some("foo bar baz bar blah")
			props("app.not.existing.something") must_== None
		}

		"update properties file" in {
			val props = new Properties(configFilename)
			props("app.new.property.foo") = "very new"

			val new_props = new Properties(configFilename)
			new_props("app.existing.config.one") must_== Some("foo bar baz")
			new_props("app.existing.other.config.two") must_== Some("foo bar baz bar blah")
			new_props("app.not.existing.something") must_== None
			new_props("app.new.property.foo") must_== Some("very new")
		}
	}

	private def setupConfigFile {
		val content = "app.existing.config.one = foo bar baz\napp.existing.other.config.two = foo bar baz bar blah"

		val file = new File(configFilename)
		val fw = new FileWriter(file)
		try { fw.write(content) }
		finally { fw.close }
	}
}
