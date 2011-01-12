package com.verknowsys.served.utils


import org.specs._
import java.io._

class PropertiesTest extends Specification {
    final val configFilename = "/tmp/config.properties"
    
    "Properties" should {
        doBefore { setupConfigFile }
        
        "correct value to correct type" in {
            val props = new Properties(configFilename)
            
            val a1 = props.get("some.nice.integer") or 0
            a1 must_== 259
            
            
            
            // val a2 = 
        }
    }

    "Properties object" should {
        doBefore {setupConfigFile}
        "read from file" in {
            val props = new Properties(configFilename)
            props("app.existing.config.one") must_== Some("foo bar baz")
            props("app.existing.other.config.two") must_== Some("foo bar baz bar blah")
            props("app.not.existing.something") must_== None
        }

        "convert value to correct type" in {
            val props = new Properties(configFilename)
            props.int("some.nice.integer") must_== Some(259)
            props.int("some.bad.integer") must_== None
            props.double("some.nice.double") must_== Some(34.56)
            props.double("some.bad.double") must_== None
            props.bool("some.nice.true") must_== Some(true)
            props.bool("some.nice.false") must_== Some(false)
            props.bool("some.bad.boolean") must_== None
        }

        "update properties file" in {
            val props = new Properties(configFilename)
            props("app.new.property.foo") = "very new"
            props("app.new.property.foo") must_== Some("very new")

            val new_props = new Properties(configFilename)
            new_props("app.existing.config.one") must_== Some("foo bar baz")
            new_props("app.existing.other.config.two") must_== Some("foo bar baz bar blah")
            new_props("app.not.existing.something") must_== None
            new_props("app.new.property.foo") must_== Some("very new")
        }

        "convert back to string" in {
            val props = new Properties(configFilename)
            props("some.bad.integer") = 4001
            props("some.bad.double") = 99.999
            props("some.bad.boolean") = true

            val new_props = new Properties(configFilename)
            new_props.int("some.bad.integer") must_== Some(4001)
            new_props.double("some.bad.double") must_== Some(99.999)
            new_props.bool("some.bad.boolean") must_== Some(true)
        }
    }

    private def setupConfigFile {
        val content = "app.existing.config.one = foo bar baz" ::
                "app.existing.other.config.two = foo bar baz bar blah" ::
                "some.nice.integer = 259" ::
                "some.bad.integer = soifgj" ::
                "some.nice.double = 34.56" ::
                "some.bad.double = kokpo" ::
                "some.nice.true = true" ::
                "some.nice.false = false" ::
                "some.bad.boolean = ifj4rf" ::
                Nil mkString "\n"

        val file = new File(configFilename)
        val fw = new FileWriter(file)
        try {fw.write(content)}
        finally {fw.close}
    }
}
