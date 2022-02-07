package diozz.echo.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import diozz.echo.R
import diozz.echo.activities.MainActivity
import diozz.echo.fragments.AboutUsFragment
import diozz.echo.fragments.FavouritesFragment
import diozz.echo.fragments.MainScreenFragment
import diozz.echo.fragments.SettingsFragment

class NavigationDrawerAdapter(_contentList: ArrayList<String>, _getImages: IntArray, _context: Context)
    : RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>()
{
    var contentList: ArrayList<String>? = null
    var getImages: IntArray? = null
    var context: Context? = null

    init
    {
        this.contentList = _contentList
        this.getImages = _getImages
        this.context = _context
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NavViewHolder
    {
        return NavViewHolder(LayoutInflater.from(p0?.context).inflate(R.layout.row_custom_navdrawer, p0, false))
    }

    override fun getItemCount(): Int
    {
        return (contentList as ArrayList).size
    }

    override fun onBindViewHolder(p0: NavViewHolder, p1: Int)
    {
        p0?.iconGet?.setBackgroundResource(getImages?.get(p1) as Int)
        p0?.textGet?.text = contentList?.get(p1)
        p0?.contentHolder?.setOnClickListener {
            when (p1)
            {
                0 -> (context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.details_fragment, MainScreenFragment()).commit()
                1 -> (context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.details_fragment, FavouritesFragment()).commit()
                2 -> (context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.details_fragment, SettingsFragment()).commit()
                3 -> (context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.details_fragment, AboutUsFragment()).commit()
            }

            MainActivity.Statify.drawerLayout?.closeDrawers()
        }
    }

    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var iconGet: ImageView? = null
        var textGet: TextView? = null
        var contentHolder: RelativeLayout? = null

        init
        {
            iconGet = itemView?.findViewById(R.id.icon_navdrawer)
            textGet = itemView?.findViewById(R.id.text_navdrawer)
            contentHolder = itemView?.findViewById(R.id.nav_item_holder)
        }
    }
}