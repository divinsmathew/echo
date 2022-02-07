package diozz.echo.activities

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import diozz.echo.R
import diozz.echo.adapters.NavigationDrawerAdapter
import diozz.echo.fragments.MainScreenFragment
import diozz.echo.fragments.PlayerFragment

class MainActivity : AppCompatActivity()
{
    var navDrawerIcons: ArrayList<String> = arrayListOf()
    var navDrawerImages = intArrayOf(R.drawable.navigation_allsongs, R.drawable.navigation_favorites, R.drawable.navigation_settings, R.drawable.navigation_aboutus)

    object Statify
    {
        var drawerLayout: DrawerLayout? = null
        var notifManager: NotificationManager? = null
        var notifBuilder: Notification? = null
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navDrawerIcons.add("All Songs")
        navDrawerIcons.add("Favourites")
        navDrawerIcons.add("Settings")
        navDrawerIcons.add("About Us")

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        MainActivity.Statify.drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(this@MainActivity, MainActivity.Statify.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        MainActivity.Statify.drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()

        this.supportFragmentManager.beginTransaction().add(R.id.details_fragment, MainScreenFragment(), "MainScreenFragment").commit()

        var navigationAdapter = NavigationDrawerAdapter(navDrawerIcons, navDrawerImages, this)
        navigationAdapter.notifyDataSetChanged()

        var NavigationRecyclerView = findViewById<RecyclerView>(R.id.navigation_rv)
        NavigationRecyclerView.layoutManager = LinearLayoutManager(this)
        NavigationRecyclerView.itemAnimator = DefaultItemAnimator()
        NavigationRecyclerView.adapter = navigationAdapter
        NavigationRecyclerView.setHasFixedSize(true)

        val intent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, 0)

        Statify.notifBuilder = Notification.Builder(this).setContentTitle(PlayerFragment.Statics.currentSongHelper?.songTitle).setContentText(PlayerFragment.Statics.currentSongHelper?.songArtist).setSmallIcon(R.drawable.echo_icon).setContentIntent(pIntent).setOngoing(true).setAutoCancel(true).build()
        Statify.notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    override fun onStop()
    {
        super.onStop()

        try
        {
            if (PlayerFragment.Statics.mediaPlayer?.isPlaying as Boolean)
            {
                Statify.notifBuilder = Notification.Builder(this)
                        .setContentTitle(PlayerFragment.Statics.currentSongHelper?.songTitle)
                        .setContentText(PlayerFragment.Statics.currentSongHelper?.songArtist)
                        .setSmallIcon(R.drawable.echo_icon)
                        .setContentIntent(PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), Intent(this, MainActivity::class.java), 0))
                        .setOngoing(true)
                        .setAutoCancel(true)
                        .build()

                Statify.notifManager?.notify(51, Statify.notifBuilder)

                PlayerFragment.Statics.notifShowing = true
            }
        }
        catch (e: Exception)
        {

        }
    }

    override fun onStart()
    {
        super.onStart()

        try
        {
            Statify.notifManager?.cancel(51)
            PlayerFragment.Statics.notifShowing = false
        }
        catch (e: Exception)
        {

        }
    }

    override fun onResume()
    {
        super.onResume()

        try
        {
            Statify.notifManager?.cancel(51)
            PlayerFragment.Statics.notifShowing = false
        }
        catch (e: Exception)
        {

        }
    }
}
