package prefs

/**
 * User: dmilith
 * Date: Jun 28, 2009
 * Time: 10:23:04 PM
 */

object GeneratePreferencesFile extends Application {

	val prefs = (new Preferences).loadPreferences
	prefs.savePreferences

}