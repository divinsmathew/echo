package diozz.echo.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.Switch
import diozz.echo.R
import diozz.echo.activities.MainActivity

class SettingsFragment : Fragment()
{
    var myActivity: Activity? = null
    var shakeSwitch: Switch? = null
    var checkboxHolder: RelativeLayout? = null

    object Statified
    {
        var MY_PREFS_NAME = "ShakeFeature"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater!!.inflate(R.layout.fragment_settings, container, false)
        shakeSwitch = view?.findViewById(R.id.shakeswitch)
        checkboxHolder = view?.findViewById(R.id.checkboxHolder)

        setHasOptionsMenu(true)
        activity?.title = "Settings"

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        (myActivity as MainActivity)?.supportActionBar?.show()
    }

    override fun onAttach(context: Context?)
    {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?)
    {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        val prefs = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
        val isAllowed = prefs?.getBoolean("feature", false)

        shakeSwitch?.isChecked = isAllowed as Boolean
        shakeSwitch?.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->

            val editor = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("feature", isChecked)
            editor?.apply()
        }

        checkboxHolder?.setOnClickListener {
            shakeSwitch?.isChecked = !(shakeSwitch?.isChecked as Boolean)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?)
    {
        super.onPrepareOptionsMenu(menu)

        menu?.findItem(R.id.action_sort)?.isVisible = false
        menu?.findItem(R.id.action_search)?.isVisible = false
    }
}
