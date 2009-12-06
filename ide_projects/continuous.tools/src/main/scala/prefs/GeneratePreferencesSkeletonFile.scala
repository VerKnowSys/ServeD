package prefs

/**
 * User: dmilith
 * Date: Jun 28, 2009
 * Time: 10:23:04 PM
 */

// TODO: to be refactored - Preferences should be parametrized
object GeneratePreferencesSkeletonFile extends Application {

	val prefs = new Preferences
	prefs.savePreferences

}