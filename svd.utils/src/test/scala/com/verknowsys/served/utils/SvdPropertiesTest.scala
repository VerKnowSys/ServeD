package com.verknowsys.served.utils


import com.verknowsys.served._
import com.verknowsys.served.testing._
import com.verknowsys.served.utils._



class SvdPropertiesTest extends DefaultTest with Logging with SvdUtils {
    final val configFilename = randomPath / "config.properties"

    override def beforeAll {
        setupSvdConfigFile
    }


    it should "SvdProperties can read from file" in {
        val props = new SvdProperties(configFilename)
        val a1 = props("app.existing.config.one") or ""
        val a2 = props("app.existing.other.config.two") or ""
        val a3 = props("app.not.existing.something") or "my default"
        a1 must be("foo bar baz")
        a2 must be("foo bar baz bar blah")
        a3 must be("my default")
    }


    it should "convert value to correct type" in {
        val props = new SvdProperties(configFilename)

        val i1 = props("some.nice.integer") or 0
        val i2 = props("some.bad.integer") or 42
        val d1 = props("some.nice.double") or 0.0
        val d2 = props("some.bad.double") or 0.123
        val b1 = props("some.nice.true") or false
        val b2 = props("some.nice.false") or true
        val b3 = props("some.bad.boolean") or false
        val b4 = props("some.bad.boolean2") or true

        i1 must be(259)
        i2 must be(42)
        d1 must be(34.56)
        d2 must be(0.123)
        b1 must be(true)
        b2 must be(false)
        b3 must be(false)
        b4 must be(true)
    }


    it should "update properties file" in {
        val props = new SvdProperties(configFilename)
        props("app.new.property.foo") = "very new"
        val s = props("app.new.property.foo") or ""
        s must be("very new")

        val new_props = new SvdProperties(configFilename)
        val s1 = new_props("app.existing.config.one") or "nothing!"
        val s2 = new_props("app.existing.other.config.two") or "nothing!"
        val s3 = new_props("app.not.existing.something") or "nothing!"
        val s4 = new_props("app.new.property.foo") or "nothing!"

        s1 must be("foo bar baz")
        s2 must be("foo bar baz bar blah")
        s3 must be("nothing!")
        s4 must be("very new")
    }


    it should "convert back to string" in {
        val props = new SvdProperties(configFilename)
        props("some.bad.integer") = 4001
        props("some.bad.double") = 99.999
        props("some.bad.boolean") = true

        val new_props = new SvdProperties(configFilename)
        val i1 = new_props("some.bad.integer") or 0
        val d1 = new_props("some.bad.double") or 0.0
        val b1 = new_props("some.bad.boolean") or false

        i1 must be(4001)
        d1 must be(99.999)
        b1 must be(true)
    }


    it should "StringPropertyConverter" in {
        SvdProperty.StringPropertyConverter("foo") must be(Some("foo"))
        SvdProperty.StringPropertyConverter("bar") must be(Some("bar"))
        SvdProperty.StringPropertyConverter("") must be(Some(""))

        SvdProperty.StringPropertyConverter.toString("foo") must be("foo")
        SvdProperty.StringPropertyConverter.toString("bar") must be("bar")
        SvdProperty.StringPropertyConverter.toString("") must be("")
    }


    it should "IntPropertyConverter" in {
        SvdProperty.IntPropertyConverter("1") must be(Some(1))
        SvdProperty.IntPropertyConverter("123") must be(Some(123))
        SvdProperty.IntPropertyConverter("-68") must be(Some(-68))
        SvdProperty.IntPropertyConverter("42.634") must be(None)
        SvdProperty.IntPropertyConverter("") must be(None)
        SvdProperty.IntPropertyConverter("foo") must be(None)
        SvdProperty.IntPropertyConverter.toString(1) must be("1")
        SvdProperty.IntPropertyConverter.toString(-6) must be("-6")
    }


    it should "DoublePropertyConverter2" in {
        SvdProperty.DoublePropertyConverter("1.0") must be(Some(1.0))
        SvdProperty.DoublePropertyConverter("123.456") must be(Some(123.456))
        SvdProperty.DoublePropertyConverter("-68") must be(Some(-68.0))
        SvdProperty.DoublePropertyConverter("") must be(None)
        SvdProperty.DoublePropertyConverter("foo") must be(None)
        SvdProperty.DoublePropertyConverter.toString(1.0) must be("1.0")
        SvdProperty.DoublePropertyConverter.toString(-6.5) must be("-6.5")
    }


    it should "DoublePropertyConverter3" in {
        SvdProperty.BooleanPropertyConverter("true") must be(Some(true))
        SvdProperty.BooleanPropertyConverter("false") must be(Some(false))
        SvdProperty.BooleanPropertyConverter("-68") must be(None)
        SvdProperty.BooleanPropertyConverter("") must be(None)
        SvdProperty.BooleanPropertyConverter("foo") must be(None)
        SvdProperty.BooleanPropertyConverter.toString(true) must be("true")
        SvdProperty.BooleanPropertyConverter.toString(false) must be("false")
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
