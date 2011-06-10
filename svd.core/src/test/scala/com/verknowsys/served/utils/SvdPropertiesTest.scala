package com.verknowsys.served.utils


import com.verknowsys.served.SvdSpecHelpers._
import com.verknowsys.served.utils._
import com.verknowsys.served._

import org.specs._


class SvdPropertiesTest extends Specification {
    final val configFilename = SvdConfig.systemTmpDir / "config.properties"
    
    "SvdProperties" should {
        doBefore { setupSvdConfigFile }

        "read from file" in {
            val props = new SvdProperties(configFilename)
            val a1 = props("app.existing.config.one") or ""
            val a2 = props("app.existing.other.config.two") or ""
            val a3 = props("app.not.existing.something") or "my default"
            a1 must_== "foo bar baz"
            a2 must_== "foo bar baz bar blah"
            a3 must_== "my default"
        }
        
        "convert value to correct type" in {
            val props = new SvdProperties(configFilename)
            
            val i1 = props("some.nice.integer") or 0
            val i2 = props("some.bad.integer") or 42
            val d1 = props("some.nice.double") or 0.0
            val d2 = props("some.bad.double") or 0.123
            val b1 = props("some.nice.true") or false
            val b2 = props("some.nice.false") or true
            val b3 = props("some.bad.boolean") or false
            val b4 = props("some.bad.boolean2") or true
                        
            i1 must_== 259
            i2 must_== 42
            d1 must_== 34.56
            d2 must_== 0.123
            b1 must_== true
            b2 must_== false
            b3 must_== false
            b4 must_== true
        }
        
        "update properties file" in {
            val props = new SvdProperties(configFilename)
            props("app.new.property.foo") = "very new"
            val s = props("app.new.property.foo") or ""
            s must_== "very new"

            val new_props = new SvdProperties(configFilename)
            val s1 = new_props("app.existing.config.one") or "nothing!"
            val s2 = new_props("app.existing.other.config.two") or "nothing!"
            val s3 = new_props("app.not.existing.something") or "nothing!"
            val s4 = new_props("app.new.property.foo") or "nothing!"
            
            s1 must_== "foo bar baz"
            s2 must_== "foo bar baz bar blah"
            s3 must_== "nothing!"
            s4 must_== "very new"
        }
        
        "convert back to string" in {
            val props = new SvdProperties(configFilename)
            props("some.bad.integer") = 4001
            props("some.bad.double") = 99.999
            props("some.bad.boolean") = true

            val new_props = new SvdProperties(configFilename)
            val i1 = new_props("some.bad.integer") or 0
            val d1 = new_props("some.bad.double") or 0.0
            val b1 = new_props("some.bad.boolean") or false
            
            i1 must_== 4001
            d1 must_== 99.999
            b1 must_== true
        }
        
        
    }
    
    "PropertyConverter" should {
        "StringPropertyConverter" in {
            SvdProperty.StringPropertyConverter("foo") must_== Some("foo")
            SvdProperty.StringPropertyConverter("bar") must_== Some("bar")
            SvdProperty.StringPropertyConverter("") must_== Some("")
            
            SvdProperty.StringPropertyConverter.toString("foo") must_== "foo"
            SvdProperty.StringPropertyConverter.toString("bar") must_== "bar"
            SvdProperty.StringPropertyConverter.toString("") must_== ""
        }
        
        "IntPropertyConverter" in {
            SvdProperty.IntPropertyConverter("1") must_== Some(1)
            SvdProperty.IntPropertyConverter("123") must_== Some(123)
            SvdProperty.IntPropertyConverter("-68") must_== Some(-68)
            SvdProperty.IntPropertyConverter("42.634") must_== None
            SvdProperty.IntPropertyConverter("") must_== None
            SvdProperty.IntPropertyConverter("foo") must_== None
            
            SvdProperty.IntPropertyConverter.toString(1) must_== "1"
            SvdProperty.IntPropertyConverter.toString(-6) must_== "-6"
        }
        
        "DoublePropertyConverter" in {
            SvdProperty.DoublePropertyConverter("1.0") must_== Some(1.0)
            SvdProperty.DoublePropertyConverter("123.456") must_== Some(123.456)
            SvdProperty.DoublePropertyConverter("-68") must_== Some(-68.0)
            SvdProperty.DoublePropertyConverter("") must_== None
            SvdProperty.DoublePropertyConverter("foo") must_== None
            
            SvdProperty.DoublePropertyConverter.toString(1.0) must_== "1.0"
            SvdProperty.DoublePropertyConverter.toString(-6.5) must_== "-6.5"
        }
        
        "DoublePropertyConverter" in {
            SvdProperty.BooleanPropertyConverter("true") must_== Some(true)
            SvdProperty.BooleanPropertyConverter("false") must_== Some(false)
            SvdProperty.BooleanPropertyConverter("-68") must_== None
            SvdProperty.BooleanPropertyConverter("") must_== None
            SvdProperty.BooleanPropertyConverter("foo") must_== None
            
            SvdProperty.BooleanPropertyConverter.toString(true) must_== "true"
            SvdProperty.BooleanPropertyConverter.toString(false) must_== "false"
        }
    }


    private def setupSvdConfigFile {
        val content = "app.existing.config.one = foo bar baz" ::
                "app.existing.other.config.two = foo bar baz bar blah" ::
                "some.nice.integer = 259" ::
                "some.bad.integer = soifgj" ::
                "some.nice.double = 34.56" ::
                "some.bad.double = kokpo" ::
                "some.nice.true = true" ::
                "some.nice.false = false" ::
                "some.bad.boolean = ifj4rf" ::
                "some.bad.boolean2 = ifj4rf" ::
                Nil mkString "\n"
                
        writeFile(configFilename, content)
    }
}
