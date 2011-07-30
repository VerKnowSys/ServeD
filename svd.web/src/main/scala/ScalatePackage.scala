import org.fusesource.scalate.Binding
import org.fusesource.scalate.TemplateSource
import org.fusesource.scalate.support.TemplatePackage


/**
 * Defines some common imports, attributes and methods across templates in all packages
 */
class ScalatePackage extends TemplatePackage {

  /** Returns the Scala code to add to the top of the generated template method */
   def header(source: TemplateSource, bindings: List[Binding]) = """
import com.verknowsys.served.api._
import com.verknowsys.forms.BaseForm
import com.verknowsys.served.web.lib.Helpers._
  """
}