package diozz.echo.fragments

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import diozz.echo.R
import diozz.echo.Songs
import diozz.echo.activities.MainActivity
import diozz.echo.adapters.FavoriteAdapter
import diozz.echo.databases.EchoDatabase
import kotlinx.android.synthetic.main.fragment_favourites.*

class FavouritesFragment : Fragment()
{
    var that: Activity? = null

    var noFavorites: TextView? = null
    var bottomBar: RelativeLayout? = null
    var pausePlayButton: ImageButton? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int? = null
    var favContent: EchoDatabase? = null

    var refreshList: ArrayList<Songs>? = null
    var listFromDatabase: ArrayList<Songs>? = null

    object Statified
    {
        var mediaPlayer: MediaPlayer? = null
    }

    object StaticBar
    {
        var songTitle: TextView? = null
        var songArtist: TextView? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var view = inflater!!.inflate(R.layout.fragment_favourites, container, false)

        noFavorites = view?.findViewById(R.id.noFavs)
        bottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        pausePlayButton = view?.findViewById(R.id.playPauseButton)
        StaticBar.songTitle = view?.findViewById(R.id.nowFavPlaying)
        StaticBar.songArtist = view?.findViewById(R.id.nowFavPlayingArtist)
        recyclerView = view?.findViewById(R.id.favRecycler)

        setHasOptionsMenu(true)
        activity?.title = "Favorites"

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?)
    {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?)
    {
        menu?.findItem(R.id.action_sort)?.isVisible = false
        menu?.findItem(R.id.action_search)?.isVisible = false

        super.onPrepareOptionsMenu(menu)
    }

    override fun onAttach(context: Context?)
    {
        super.onAttach(context)
        that = context as Activity
    }

    override fun onAttach(activity: Activity?)
    {
        super.onAttach(activity)
        that = activity
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        (that as MainActivity)?.supportActionBar?.show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        favContent = EchoDatabase(that)

        displayFavsBySearching()
        bottomBarSetter()
    }

    fun getSongsInPhone() : ArrayList<Songs>
    {
        var arrayList = ArrayList<Songs>()

        var contentResolver = that?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)

        if (songCursor != null && songCursor.moveToFirst())
        {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val title = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val data = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateAdded = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while (songCursor.moveToNext())
            {
                var curentId = songCursor.getLong(songId)
                var curentTitle = songCursor.getString(title)
                var curentArtist = songCursor.getString(artist)
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
                bottomBar?.visibility = View.VISIBLE
            else
                bottomBar?.visibility = View.GONE
        }
        catch (e: Exception){}
    }

    fun bottomBarClickHandler()
    {
        bottomBar?.setOnClickListener {
            Statified.mediaPlayer = PlayerFragment.Statics.mediaPlayer

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

        playPauseButton.setOnClickListener {
            if (PlayerFragment.Statics.mediaPlayer?.isPlaying as Boolean)
            {
                PlayerFragment.Statics.mediaPlayer?.pause()
                trackPosition = PlayerFragment.Statics.mediaPlayer?.currentPosition as Int
                playPauseButton?.setImageResource(R.drawable.play_icon)
            }
            else
            {
                PlayerFragment.Statics.mediaPlayer?.seekTo(trackPosition as Int)
                PlayerFragment.Statics.mediaPlayer?.start()
                playPauseButton?.setImageResource(R.drawable.pause_icon)
            }
        }
    }

    fun displayFavsBySearching()
    {
        if (favContent?.checkSize() as Int > 0)
        {
            refreshList = ArrayList<Songs>()
            listFromDatabase = favContent?.queryDbList(that as Context)
            var listFromDevice = getSongsInPhone()

            if (listFromDevice != null)
                for (i in 0..listFromDevice.size - 1)
                    for (j in 0..listFromDatabase?.size as Int - 1)
                        if (listFromDatabase?.get(j)?.songId === listFromDevice?.get(i).songId)
                            refreshList?.add((listFromDatabase as ArrayList<Songs>)[j])

            if (refreshList == null)
            {
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            }
            else
            {
                var favAdapter = FavoriteAdapter(refreshList as ArrayList<Songs>, that as Context)
                recyclerView?.layoutManager = LinearLayoutManager(that)
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favAdapter
                recyclerView?.setHasFixedSize(true)

                recyclerView?.visibility = View.VISIBLE
                noFavorites?.visibility = View.GONE
            }
        }
        else
        {
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }

        try
        {
            if (PlayerFragment.Statics.mediaPlayer == null)
                bottomBar?.visibility = View.GONE
            else if (PlayerFragment.Statics.mediaPlayer?.isPlaying == false)
                bottomBar?.visibility = View.GONE
            else
                bottomBar?.visibility = View.VISIBLE
        }
        catch (e: Exception){}
    }
}
