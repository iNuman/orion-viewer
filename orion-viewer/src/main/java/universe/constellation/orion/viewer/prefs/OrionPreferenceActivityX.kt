package universe.constellation.orion.viewer.prefs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import universe.constellation.orion.viewer.OrionBaseActivity
import universe.constellation.orion.viewer.R

class OrionPreferenceActivityX : OrionBaseActivity() {
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        onOrionCreate(savedInstanceState, R.layout.activity_with_fragment, true, true)
        if (savedInstanceState == null) {
//            supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.settings_container, OrionPreferenceFragmentX())
//                .commit()
        }
    }
}

class BehaviourPreferenceFragment : SwitchHeaderPreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.user_pref_controls, rootKey)
    }
}

class AppearancePreferenceFragment : SwitchHeaderPreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
         setPreferencesFromResource(R.xml.user_pref_appearance, rootKey)
    }
}

abstract class SwitchHeaderPreferenceFragment : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = preferenceScreen.title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
        val summary = preferenceScreen.summary
        if (title != summary) {
            (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = summary
        }
    }
}