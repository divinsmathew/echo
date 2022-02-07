package diozz.echo.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import diozz.echo.R
import diozz.echo.Songs
import diozz.echo.activities.MainActivity
import diozz.echo.adapters.MainScreenAdapter
import java.util.*
import kotlin.collections.ArrayList


class MainScreenFragment : Fragment()
{
    var songsList: ArrayList<Songs>? = null
    var nowPlayingButtomBar: RelativeLayout? = null
    var noResult: RelativeLayout? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var recyclerView: RecyclerView? = null
    var myActivity: Activity? = null
    var mainScreenAdapter: MainScreenAdapter? = null
    var trackPosition: Int = 0
    var sortMenu: MenuItem? = null
    var searchMenu: MenuItem? = null
    var songListCopy: java.util.ArrayList<Songs>?= null


    var t: Boolean? = true

    object StaticBar
    {
        var songTitle: TextView? = null
        var songArtist: TextView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        (myActivity as MainActivity)?.supportActionBar?.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater!!.inflate(R.layout.fragment_main_screen, container, false)
        noSongs = view.findViewById<RelativeLayout>(R.id.noSongs)
        nowPlayingButtomBar = view.findViewById<RelativeLayout>(R.id.hiddenBarMainScreen)
        StaticBar.songTitle = view.findViewById<TextView>(R.id.nowPlaying)
        StaticBar.songArtist = view.findViewById<TextView>(R.id.nowPlayingArtist)
        playPauseButton = view.findViewById<ImageButton>(R.id.playPauseButton)
        recyclerView = view.findViewById<RecyclerView>(R.id.contentMain)
        noResult = view.findViewById<RelativeLayout>(R.id.noResult)

        setHasOptionsMenu(true)
        activity?.title = "All Songs"

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        songsList = getSongsInPhone()

        val prefs = activity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val asc = prefs?.getBoolean("action_sort_ascending", true) as Boolean


        songListCopy = ArrayList()
        songListCopy?.addAll(songsList.orEmpty())


        if (asc)
        {
            Collections.sort(songsList, Songs.Statics.nameComparator)
            Collections.sort(songListCopy, Songs.Statics.nameComparator)
        }
        else
        {
            Collections.sort(songsList, Songs.Statics.dateComparator)
            Collections.sort(songListCopy, Songs.Statics.dateComparator)
        }

        if (songsList == null)
        {
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        }
        else
        {
            mainScreenAdapter = MainScreenAdapter(songsList as ArrayList<Songs>, myActivity as Context)
            var layoutManager = LinearLayoutManager(myActivity)
            recyclerView?.layoutManager = layoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = mainScreenAdapter

            recyclerView?.adapter?.notifyDataSetChanged()
        }

        bottomBarSetter()

        try
        {
            if (PlayerFragment.Statics.mediaPlayer == null)
                nowPlayingButtomBar?.visibility = View.GONE
            else if (PlayerFragment.Statics.mediaPlayer?.isPlaying == false)
                nowPlayingButtomBar?.visibility = View.GONE
            else
                nowPlayingButtomBar?.visibility = View.VISIBLE
        }
        catch (e: Exception)
        {
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?)
    {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)

        val prefs = activity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = prefs?.getBoolean("action_sort_ascending", true) as Boolean

        sortMenu = menu?.getItem(1)
        searchMenu = menu?.getItem(0)

        sortMenu?.subMenu?.getItem(0)?.isChecked = action_sort_ascending
        sortMenu?.subMenu?.getItem(1)?.isChecked = !action_sort_ascending

        val myActionMenuItem = menu?.findItem(R.id.action_search)
        val searchView = myActionMenuItem?.actionView as SearchView

            searchView?.queryHint = "Search for Title or Artist.."

        searchMenu?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener
        {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean
            {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean
            {
                val asc = context?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
                        ?.getBoolean("action_sort_ascending", false)

                if (asc as Boolean)
                {
                    Collections.sort(songsList, Songs.Statics.nameComparator)
                    Collections.sort(songListCopy, Songs.Statics.nameComparator)
                }
                else
                {
                    Collections.sort(songsList, Songs.Statics.dateComparator)
                    Collections.sort(songListCopy, Songs.Statics.dateComparator)
                }

                mainScreenAdapter?.notifyDataSetChanged()

                sortMenu?.isVisible = true
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
        {
            override fun onQueryTextSubmit(query: String): Boolean
            {
                //mainScreenAdapter?.filter(query)
                myActionMenuItem?.collapseActionView()

                return false
            }

            override fun onQueryTextChange(text: String): Boolean
            {
                mainScreenAdapter?.filter(text)

                if(songsList?.count() == 0)
                {
                    recyclerView?.visibility = View.GONE
                    noResult?.visibility = View.VISIBLE
                }
                else
                {
                    recyclerView?.visibility = View.VISIBLE
                    noResult?.visibility = View.GONE
                }

                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {
        if (item?.itemId == R.id.action_sort_ascending)
        {
            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("action_sort_ascending", true)
            //editor?.putBoolean("action_sort_recent", false) //not required because its binary!
            editor?.apply()
            if (songsList != null)
            {
                Collections.sort(songsList, Songs.Statics.nameComparator)
                Collections.sort(songListCopy, Songs.Statics.nameComparator)
            }
            mainScreenAdapter?.notifyDataSetChanged()
            sortMenu?.subMenu?.getItem(0)?.isChecked = true
            sortMenu?.subMenu?.getItem(1)?.isChecked = false

            return false
        }
        else if (item?.itemId == R.id.action_sort_by_recent)
        {
            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("action_sort_ascending", false)
            //editor?.putBoolean("action_sort_recent", true)
            editor?.apply()
            if (songsList != null)
            {
                Collections.sort(songsList, Songs.Statics.dateComparator)
                Collections.sort(songListCopy, Songs.Statics.dateComparator)
            }
            mainScreenAdapter?.notifyDataSetChanged()
            sortMenu?.subMenu?.getItem(0)?.isChecked = false
            sortMenu?.subMenu?.getItem(1)?.isChecked = true
            return false
        }
        else if (item?.itemId == R.id.action_search)
        {
            sortMenu?.isVisible = false
        }

        return super.onOptionsItemSelected(item)
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

    fun getSongsInPhone(): ArrayList<Songs>
    {
        var arrayList = ArrayList<Songs>()

        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)

        if (songCursor != null && songCursor.moveToFirst())
        {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val title = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val data = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateAdded = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val size = songCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

            while (songCursor.moveToNext())
            {
                //skip music files less than 100 bytes, which are probably invalid or corrupted
                if(songCursor.getLong(size) < 100)
                    continue

                var curentId = songCursor.getLong(songId)
                var curentTitle = songCursor.getString(title) ?: ""
                var curentArtist = songCursor.getString(artist) ?: ""
                var curentData = songCursor.getString(data)
                var curentDate = songCursor.getLong(dateAdded)

                arrayList.add(Songs(curentId, curentTitle, curentArtist, curentData, curentDate))
            }
        }

        return arrayList
    }

    fun bottomBarSetter()
    {
        try
        {
            bottomBarClickHandler()

            StaticBar.songTitle?.text = PlayerFragment.Statics.currentSongHelper?.songTitle
            StaticBar.songArtist?.text = PlayerFragment.Statics.currentSongHelper?.songArtist
            PlayerFragment.Statics.mediaPlayer?.setOnCompletionListener {
                StaticBar.songTitle?.text = PlayerFragment.Statics.currentSongHelper?.songTitle
                StaticBar.songArtist?.text = PlayerFragment.Statics.currentSongHelper?.songArtist
                PlayerFragment.Statics.onSongComplete()
            }

            if (PlayerFragment.Statics.mediaPlayer?.isPlaying as Boolean)
                nowPlayingButtomBar?.visibility = View.VISIBLE
            else
                nowPlayingButtomBar?.visibility = View.GONE
        }
        catch (e: Exception)
        {
        }
    }

    fun bottomBarClickHandler()
    {
        nowPlayingButtomBar?.setOnClickListener {
            FavouritesFragment.Statified.mediaPlayer = PlayerFragment.Statics.mediaPlayer

            val playerFragment = PlayerFragment()
            var args = Bundle()
            args.putString("songArtist", PlayerFragment.Statics.currentSongHelper?.songArtist)
            args.putString("path", PlayerFragment.Statics.currentSongHelper?.songPath)
            args.putString("songTitle", PlayerFragment.Statics.currentSongHelper?.songTitle)
            args.putInt("songId", PlayerFragment.Statics.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", PlayerFragment.Statics.currentSongHelper?.currentPosition as Int)
            args.putParcelableArrayList("songData", PlayerFragment.Statics.fetchSongs)
            args.putString("FavBottomBar", "Y")
            playerFragment.arguments = args

            fragmentManager?.beginTransaction()?.replace(R.id.details_fragment, playerFragment)?.addToBackStack("SongPlayingFragment")?.commit()
        }

        playPauseButton?.setOnClickListener {
            if (PlayerFragment.Statics.mediaPlayer?.isPlaying as Boolean)
            {
                PlayerFragment.Statics.mediaPlayer?.pause()
                trackPosition = PlayerFragment.Statics.mediaPlayer?.currentPosition as Int
                playPauseButton?.setImageResource(R.drawable.play_icon)
            }
            else
            {
                PlayerFragment.Statics.mediaPlayer?.seekTo(trackPosition)
                PlayerFragment.Statics.mediaPlayer?.start()
                playPauseButton?.setImageResource(R.drawable.pause_icon)
            }
        }
    }
}
