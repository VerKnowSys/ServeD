package com.verknowsys.served.utils


import org.specs._
import org.specs.specification.PendingUntilFixed
import java.io._

class PropertiesTest extends Specification with PendingUntilFixed {
    final val configFilename = "/tmp/config.properties"
    
    "Properties" should {
        doBefore { setupConfigFile }

        "read from file" in {
            val props = new Properties(configFilename)
            val a1 = props.get("app.existing.config.one") or ""
            val a2 = props.get("app.existing.other.config.two") or ""
            val a3 = props.get("app.not.existing.something") or "my default"
            a1 must_== "foo bar baz"
            a2 must_== "foo bar baz bar blah"
            a3 must_== "my default"
        }
        
        "convert value to correct type" in {
            val props = new Properties(configFilename)
            
            val i1 = props.get("some.nice.integer") or 0
            val i2 = props.get("some.bad.integer") or 42
            val d1 = props.get("some.nice.double") or 0.0
            val d2 = props.get("some.bad.double") or 0.123
            val b1 = props.get("some.nice.true") or false
            val b2 = props.get("some.nice.false") or true
            val b3 = props.get("some.bad.boolean") or false
            val b4 = props.get("some.bad.boolean") or true
            
            i1 must_== 259
            i2 must_== 42
            d1 must_== 34.56
            d2 must_== 0.123
            b1 must_== true
            b2 must_== false
            b3 must_== false
            pendingUntilFixed { b4 must_== true }
        }
        
        "update properties file" in {
            val props = new Properties(configFilename)
            props("app.new.property.foo") = "very new"
            val s = props.get("app.new.property.foo") or ""
            s must_== "very new"

            val new_props = new Properties(configFilename)
            val s1 = new_props.get("app.existing.config.one") or "nothing!"
            val s2 = new_props.get("app.existing.other.config.two") or "nothing!"
            val s3 = new_props.get("app.not.existing.something") or "nothing!"
            val s4 = new_props.get("app.new.property.foo") or "nothing!"
            
            s1 must_== "foo bar baz"
            s2 must_== "foo bar baz bar blah"
            s3 must_== "nothing!"
            s4 must_== "very new"
        }
        
        "convert back to string" in {
            val props = new Properties(configFilename)
            props("some.bad.integer") = 4001
            props("some.bad.double") = 99.999
            props("some.bad.boolean") = true

            val new_props = new Properties(configFilename)
            val i1 = new_props.get("some.bad.integer") or 0
            val d1 = new_props.get("some.bad.double") or 0.0
            val b1 = new_props.get("some.bad.boolean") or false
            
            i1 must_== 4001
            d1 must_== 99.999
            b1 must_== true
        }
        
        
    }
    
    "PropertyConverter" should {
        "StringPropertyConverter" in {
            Property.StringPropertyConverter("foo") must_== Some("foo")
            Property.StringPropertyConverter("bar") must_== Some("bar")
            Property.StringPropertyConverter("") must_== Some("")
            
            Property.StringPropertyConverter.toString("foo") must_== "foo"
            Property.StringPropertyConverter.toString("bar") must_== "bar"
            Property.StringPropertyConverter.toString("") must_== ""
        }
        
        "IntPropertyConverter" in {
            Property.IntPropertyConverter("1") must_== Some(1)
            Property.IntPropertyConverter("123") must_== Some(123)
            Property.IntPropertyConverter("-68") must_== Some(-68)
            Property.IntPropertyConverter("42.634") must_== None
            Property.IntPropertyConverter("") must_== None
            Property.IntPropertyConverter("foo") must_== None
            
            Property.IntPropertyConverter.toString(1) must_== "1"
            Property.IntPropertyConverter.toString(-6) must_== "-6"
        }
        
        "DoublePropertyConverter" in {
            Property.DoublePropertyConverter("1.0") must_== Some(1.0)
            Property.DoublePropertyConverter("123.456") must_== Some(123.456)
            Property.DoublePropertyConverter("-68") must_== Some(-68.0)
            Property.DoublePropertyConverter("") must_== None
            Property.DoublePropertyConverter("foo") must_== None
            
            Property.DoublePropertyConverter.toString(1.0) must_== "1.0"
            Property.DoublePropertyConverter.toString(-6.5) must_== "-6.5"
        }
        
        "DoublePropertyConverter" in {
            Property.BooleanPropertyConverter("true") must_== Some(true)
            Property.BooleanPropertyConverter("false") must_== Some(false)
            Property.BooleanPropertyConverter("-68") must_== None
            Property.BooleanPropertyConverter("") must_== None
            Property.BooleanPropertyConverter("foo") must_== None
            
            Property.BooleanPropertyConverter.toString(true) must_== "true"
            Property.BooleanPropertyConverter.toString(false) must_== "false"
        }
    }

    // "Properties object" should {
    //     doBefore {setupConfigFile}
    //     "read from file" in {
    //         val props = new Properties(configFilename)
    //         props("app.existing.config.one") must_== Some("foo bar baz")
    //         props("app.existing.other.config.two") must_== Some("foo bar baz bar blah")
    //         props("app.not.existing.something") must_== None
    //     }
    // 
    //     "convert value to correct type" in {
    //         val props = new Properties(configFilename)
    //         props.int("some.nice.integer") must_== Some(259)
    //         props.int("some.bad.integer") must_== None
    //         props.double("some.nice.double") must_== Some(34.56)
    //         props.double("some.bad.double") must_== None
    //         props.bool("some.nice.true") must_== Some(true)
    //         props.bool("some.nice.false") must_== Some(false)
    //         props.bool("some.bad.boolean") must_== None
    //     }
    // 
    //     "update properties file" in {
    //         val props = new Properties(configFilename)
    //         props("app.new.property.foo") = "very new"
    //         props("app.new.property.foo") must_== Some("very new")
    // 
    //         val new_props = new Properties(configFilename)
    //         new_props("app.existing.config.one") must_== Some("foo bar baz")
    //         new_props("app.existing.other.config.two") must_== Some("foo bar baz bar blah")
    //         new_props("app.not.existing.something") must_== None
    //         new_props("app.new.property.foo") must_== Some("very new")
    //     }
    // 
    //     "convert back to string" in {
    //         val props = new Properties(configFilename)
    //         props("some.bad.integer") = 4001
    //         props("some.bad.double") = 99.999
    //         props("some.bad.boolean") = true
    // 
    //         val new_props = new Properties(configFilename)
    //         new_props.int("some.bad.integer") must_== Some(4001)
    //         new_props.double("some.bad.double") must_== Some(99.999)
    //         new_props.bool("some.bad.boolean") must_== Some(true)
    //     }
    // }

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
