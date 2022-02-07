package diozz.echo.fragments

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import diozz.echo.CurrentSongHelper
import diozz.echo.R
import diozz.echo.Songs
import diozz.echo.activities.MainActivity
import diozz.echo.databases.EchoDatabase
import java.util.*
import java.util.concurrent.TimeUnit


class PlayerFragment : Fragment()
{
    object Statics
    {
        var MY_SHUFFLE_PREF = "Shuffle Feature"
        var MY_LOOP_PREF = "Loop Feature"

        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var audioVisualisation: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null

        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null

        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playPauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null
        var seekBar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var fab: ImageButton? = null
        var favouriteContent: EchoDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSensorListner: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"

        var safeShake = true
        var notifShowing = false

        var updateSongTime = object : Runnable
        {
            override fun run()
            {
                val getCurrent = mediaPlayer?.currentPosition

                startTimeText?.text = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long), TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong() as Long)))

                seekBar?.progress = mediaPlayer!!.currentPosition
                Handler().postDelayed(this, 100)
            }
        }

        fun onSongComplete()
        {
            if (currentSongHelper?.isShuffling as Boolean)
                playNext("PlayNextNormalShuffle")
            else if (currentSongHelper?.isLooping as Boolean)
            {
                var nextSong = fetchSongs?.get(currentPosition)
                currentSongHelper?.songPath = nextSong?.songData
                currentSongHelper?.songTitle = nextSong?.songTitle
                currentSongHelper?.songArtist = nextSong?.songArtist
                currentSongHelper?.currentPosition = currentPosition
                currentSongHelper?.songId = nextSong?.songId
                Statics.mediaPlayer?.reset()

                try
                {
                    Statics.mediaPlayer?.setDataSource(Statics.myActivity, Uri.parse(currentSongHelper?.songPath))
                    Statics.mediaPlayer?.prepare()
                    Statics.mediaPlayer?.start()
                    processInfo(Statics.mediaPlayer as MediaPlayer)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }

                // mediaPlayer?.seekTo(0)
                //Statics.mediaPlayer?.start()
            }
            else
                playNext("PlayNextNormal")

            Statics.currentSongHelper?.isPlaying = true

            if (Statics.favouriteContent?.checkIfIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean)
                Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_on))
            else
                Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_off))
        }

        fun updateTexts(songTitle: String, songArtist: String)
        {
            var updatedTitle = songTitle
            var updatedArtist = songArtist

            // if (songTitle?.equals("<unknown>", true)) updatedTitle = (currentSongHelper?.songPath as String)?.substringAfterLast("/")
            // if (songArtist?.equals("<unknown>", true)) updatedArtist = "Unknown Artist"

            Statics.songTitleView?.text = updatedTitle
            Statics.songArtistView?.text = updatedArtist
        }

        fun processInfo(mp: MediaPlayer)
        {
            Statics.startTimeText?.text = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(mp.currentPosition?.toLong()), TimeUnit.MILLISECONDS.toSeconds(mp.currentPosition?.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mp.currentPosition.toLong())))
            Statics.endTimeText?.text = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(mp.duration?.toLong()), TimeUnit.MILLISECONDS.toSeconds(mp.duration?.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mp.duration.toLong())))

            Statics.seekBar?.max = mp.duration
            Statics.seekBar?.progress = mp.currentPosition


            if (PlayerFragment.Statics.notifShowing)
            {
                MainActivity.Statify.notifManager?.cancel(51)

                MainActivity.Statify.notifBuilder = Notification.Builder(myActivity).setContentTitle(PlayerFragment.Statics.currentSongHelper?.songTitle).setContentText(PlayerFragment.Statics.currentSongHelper?.songArtist).setSmallIcon(R.drawable.echo_icon).setContentIntent(PendingIntent.getActivity(myActivity, System.currentTimeMillis().toInt(), Intent(myActivity, MainActivity::class.java), 0)).setOngoing(true).setAutoCancel(true).build()

                MainActivity.Statify.notifManager?.notify(51, MainActivity.Statify.notifBuilder)
            }

            MainScreenFragment.StaticBar.songTitle?.text = PlayerFragment.Statics.currentSongHelper?.songTitle
            MainScreenFragment.StaticBar.songArtist?.text = PlayerFragment.Statics.currentSongHelper?.songArtist

            FavouritesFragment.StaticBar.songTitle?.text = PlayerFragment.Statics.currentSongHelper?.songTitle
            FavouritesFragment.StaticBar.songArtist?.text = PlayerFragment.Statics.currentSongHelper?.songArtist

            Handler().postDelayed(Statics.updateSongTime, 1000)
        }

        fun playNext(check: String)
        {
            if (check.equals("PlayNextNormal", true))
                Statics.currentPosition++
            else if (check.equals("PlayNextNormalShuffle", true))
                Statics.currentPosition = Random().nextInt(Statics.fetchSongs?.size?.plus(1) as Int)

            if (Statics.currentPosition == Statics.fetchSongs?.size)
                Statics.currentPosition = 0

            //  if (Statics.currentSongHelper?.isPlaying as Boolean)
            //      Statics.playPauseImageButton?.setImageResource(R.drawable.pause_icon)
            //  else
            Statics.playPauseImageButton?.setImageResource(R.drawable.pause_icon)

            //Statics.currentSongHelper?.isLooping = false
            var nextSong = Statics.fetchSongs?.get(Statics.currentPosition)
            Statics.currentSongHelper?.songPath = nextSong?.songData
            Statics.currentSongHelper?.songTitle = nextSong?.songTitle
            Statics.currentSongHelper?.songArtist = nextSong?.songArtist
            Statics.currentSongHelper?.songId = nextSong?.songId

            Statics.updateTexts(Statics.currentSongHelper?.songTitle as String, Statics.currentSongHelper?.songArtist as String)
            Statics.mediaPlayer?.reset()

            try
            {
                Statics.mediaPlayer?.setDataSource(Statics.myActivity, Uri.parse(Statics.currentSongHelper?.songPath))
                Statics.mediaPlayer?.prepare()
                Statics.mediaPlayer?.start()
                Statics.processInfo(Statics.mediaPlayer as MediaPlayer)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            if (Statics.favouriteContent?.checkIfIdExists(Statics.currentSongHelper?.songId?.toInt() as Int) as Boolean)
                Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_on))
            else
                Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_off))
        }

        fun playPrevious()
        {
            if (Statics.currentPosition > 0)
                Statics.currentPosition--

            if (Statics.currentSongHelper?.isPlaying as Boolean)
                Statics.playPauseImageButton?.setImageResource(R.drawable.pause_icon)
            else
                Statics.playPauseImageButton?.setImageResource(R.drawable.play_icon)

            //Statics.currentSongHelper?.isLooping = false
            var nextSong = fetchSongs?.get(Statics.currentPosition)
            Statics.currentSongHelper?.songPath = nextSong?.songData
            Statics.currentSongHelper?.songTitle = nextSong?.songTitle
            Statics.currentSongHelper?.songArtist = nextSong?.songArtist
            Statics.currentSongHelper?.songId = nextSong?.songId

            Statics.updateTexts(Statics.currentSongHelper?.songTitle as String, Statics.currentSongHelper?.songArtist as String)

            Statics.mediaPlayer?.reset()

            try
            {
                Statics.mediaPlayer?.setDataSource(Statics.myActivity, Uri.parse(Statics.currentSongHelper?.songPath))
                Statics.mediaPlayer?.prepare()
                Statics.mediaPlayer?.start()
                Statics.processInfo(Statics.mediaPlayer as MediaPlayer)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            if (Statics.favouriteContent?.checkIfIdExists(Statics.currentSongHelper?.songId?.toInt() as Int) as Boolean)
                Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_on))
            else
                Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_off))
        }
    }

    var mAccelaration: Float = 0f
    var mAccelarationCurrent: Float = 0f
    var mAccelarationLast: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var view = inflater!!.inflate(R.layout.fragment_player, container, false)

        Statics.seekBar = view?.findViewById(R.id.seekBar)
        Statics.startTimeText = view?.findViewById(R.id.startTime)
        Statics.endTimeText = view?.findViewById(R.id.endTime)
        Statics.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statics.nextImageButton = view?.findViewById(R.id.nextButton)
        Statics.previousImageButton = view?.findViewById(R.id.previousButton)
        Statics.loopImageButton = view?.findViewById(R.id.loopButton)
        Statics.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statics.songArtistView = view?.findViewById(R.id.songArtist)
        Statics.songTitleView = view?.findViewById(R.id.songTitle)
        Statics.glView = view?.findViewById(R.id.visualizer_view)
        Statics.fab = view?.findViewById(R.id.favIcon)

        setHasOptionsMenu(true)
        activity?.title = "Now Playing"

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        Statics.mSensorManager = Statics.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mAccelaration = 0.0f
        mAccelarationCurrent = SensorManager.GRAVITY_EARTH
        mAccelarationLast = SensorManager.GRAVITY_EARTH

        (activity as MainActivity)?.supportActionBar?.show()

        bindShakeListner()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?)
    {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?)
    {
        super.onPrepareOptionsMenu(menu)

        menu?.findItem(R.id.action_redirect)?.isVisible = true
        menu?.findItem(R.id.action_sort)?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {
        when (item?.itemId)
        {
            R.id.action_redirect ->
            {
                Statics.myActivity?.onBackPressed()
                return false
            }
        }

        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Statics.audioVisualisation = Statics.glView as AudioVisualization
    }

    override fun onAttach(context: Context?)
    {
        super.onAttach(context)
        Statics.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?)
    {
        super.onAttach(activity)
        Statics.myActivity = activity
    }

    override fun onResume()
    {
        super.onResume()
        Statics.audioVisualisation?.onResume()
        Statics.mSensorManager?.registerListener(Statics.mSensorListner, Statics.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause()
    {
        super.onPause()
        Statics.audioVisualisation?.onPause()
        Statics.mSensorManager?.unregisterListener(Statics.mSensorListner)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        Statics.audioVisualisation?.release()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        Statics.favouriteContent = EchoDatabase(Statics.myActivity)
        Statics.currentSongHelper = CurrentSongHelper()
        Statics.currentSongHelper?.isPlaying = true
        Statics.currentSongHelper?.isShuffling = false
        Statics.currentSongHelper?.isLooping = false

        var songPath: String? = null
        var songTitle: String? = null
        var songArtist: String? = null
        var songId: Long? = 0

        try
        {
            songPath = arguments?.getString("path")
            songTitle = arguments?.getString("songTitle")
            songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")?.toLong()

            Statics.currentPosition = arguments!!.getInt("songPosition")
            Statics.fetchSongs = arguments?.getParcelableArrayList("songData")

            Statics.currentSongHelper?.songPath = songPath
            Statics.currentSongHelper?.songTitle = songTitle
            Statics.currentSongHelper?.songArtist = songArtist
            Statics.currentSongHelper?.songId = songId
            Statics.currentSongHelper?.currentPosition = Statics.currentPosition

            Statics.updateTexts(Statics.currentSongHelper?.songTitle as String, Statics.currentSongHelper?.songArtist as String)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        var fromFav = arguments?.get("FavBottomBar") as String?

        if (fromFav != null) Statics.mediaPlayer = FavouritesFragment.Statified.mediaPlayer
        else
        {
            Statics.mediaPlayer = MediaPlayer()
            Statics.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            try
            {
                Statics.mediaPlayer?.setDataSource(Statics.myActivity, Uri.parse(songPath))
                Statics.mediaPlayer?.prepare()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            Statics.mediaPlayer?.start()
        }

        Statics.processInfo(Statics.mediaPlayer as MediaPlayer)

        if (Statics.mediaPlayer?.isPlaying as Boolean)
            Statics.playPauseImageButton?.setImageResource(R.drawable.pause_icon)
        else
            Statics.playPauseImageButton?.setImageResource(R.drawable.play_icon)

        Statics.mediaPlayer?.setOnCompletionListener {
            Statics.onSongComplete()
        }

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statics.myActivity as Context, 0)
        Statics.audioVisualisation?.linkTo(visualizationHandler)

        var prefsForShuffle = Statics.myActivity?.getSharedPreferences(Statics.MY_SHUFFLE_PREF, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean)
        {
            Statics.currentSongHelper?.isShuffling = true
            Statics.currentSongHelper?.isLooping = false
            Statics.shuffleImageButton?.setImageResource(R.drawable.shuffle_icon)
            Statics.loopImageButton?.setImageResource(R.drawable.loop_white_icon)
        }
        else
        {
            Statics.currentSongHelper?.isShuffling = false
            Statics.shuffleImageButton?.setImageResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = Statics.myActivity?.getSharedPreferences(Statics.MY_LOOP_PREF, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean)
        {
            Statics.currentSongHelper?.isShuffling = false
            Statics.currentSongHelper?.isLooping = true
            Statics.shuffleImageButton?.setImageResource(R.drawable.shuffle_white_icon)
            Statics.loopImageButton?.setImageResource(R.drawable.loop_icon)
        }
        else
        {
            Statics.currentSongHelper?.isLooping = false
            Statics.loopImageButton?.setImageResource(R.drawable.loop_white_icon)
        }

        if (Statics.favouriteContent?.checkIfIdExists(Statics.currentSongHelper?.songId?.toInt() as Int) as Boolean)
            Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_on))
        else
            Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_off))

        setUpClickHandlers()
    }

    fun setUpClickHandlers()
    {
        Statics.fab?.setOnClickListener {
            if (Statics.favouriteContent?.checkIfIdExists(Statics.currentSongHelper?.songId?.toInt() as Int) as Boolean)
            {
                Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_off))
                Statics.favouriteContent?.deLeteFavorite(Statics.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statics.myActivity, "\"" + Statics.currentSongHelper?.songTitle + "\" Was Removed From Favourites", Toast.LENGTH_SHORT).show()
            }
            else
            {
                Statics.fab?.setImageDrawable(ContextCompat.getDrawable(Statics.myActivity as Context, R.drawable.favorite_on))
                Statics.favouriteContent?.storeAsFavorite(Statics.currentSongHelper?.songId?.toInt() as Int, Statics.currentSongHelper?.songArtist, Statics.currentSongHelper?.songTitle, Statics.currentSongHelper?.songPath)
                Toast.makeText(Statics.myActivity, "\"" + Statics.currentSongHelper?.songTitle + "\" Was Added To Favourites", Toast.LENGTH_SHORT).show()
            }
        }

        Statics.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
            {
                if (fromUser)
                    Statics.mediaPlayer?.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar)
            {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar)
            {
            }
        })

        Statics.shuffleImageButton?.setOnClickListener {
            var editShuffle = Statics.myActivity?.getSharedPreferences(Statics.MY_SHUFFLE_PREF, Context.MODE_PRIVATE)?.edit()
            var editLoop = Statics.myActivity?.getSharedPreferences(Statics.MY_LOOP_PREF, Context.MODE_PRIVATE)?.edit()

            if (Statics.currentSongHelper?.isShuffling as Boolean)
            {
                Statics.shuffleImageButton?.setImageResource(R.drawable.shuffle_white_icon)
                Statics.currentSongHelper?.isShuffling = false
            }
            else
            {
                Statics.currentSongHelper?.isShuffling = true
                Statics.currentSongHelper?.isLooping = false
                Statics.shuffleImageButton?.setImageResource(R.drawable.shuffle_icon)
                Statics.loopImageButton?.setImageResource(R.drawable.loop_white_icon)
                editLoop?.putBoolean("feature", false)
                editLoop?.apply()
            }

            editShuffle?.putBoolean("feature", Statics.currentSongHelper?.isShuffling as Boolean)
            editShuffle?.apply()
        }
        Statics.nextImageButton?.setOnClickListener {
            Statics.currentSongHelper?.isPlaying = true
            if (Statics.currentSongHelper?.isShuffling as Boolean)
                Statics.playNext("PlayNextNormalShuffle")
            else
                Statics.playNext("PlayNextNormal")
        }
        Statics.previousImageButton?.setOnClickListener {
            Statics.currentSongHelper?.isPlaying = true
            //if (Statics.currentSongHelper?.isLooping as Boolean)
            // Statics.loopImageButton?.setImageResource(R.drawable.loop_white_icon)

            Statics.playPrevious()
        }
        Statics.loopImageButton?.setOnClickListener {
            var editShuffle = Statics.myActivity?.getSharedPreferences(Statics.MY_SHUFFLE_PREF, Context.MODE_PRIVATE)?.edit()
            var editLoop = Statics.myActivity?.getSharedPreferences(Statics.MY_LOOP_PREF, Context.MODE_PRIVATE)?.edit()

            if (Statics.currentSongHelper?.isLooping as Boolean)
            {
                Statics.currentSongHelper?.isLooping = false
                Statics.loopImageButton?.setImageResource(R.drawable.loop_white_icon)
            }
            else
            {
                Statics.currentSongHelper?.isLooping = true
                Statics.currentSongHelper?.isShuffling = false
                Statics.loopImageButton?.setImageResource(R.drawable.loop_icon)
                Statics.shuffleImageButton?.setImageResource(R.drawable.shuffle_white_icon)

                editShuffle?.putBoolean("feature", false)
                editShuffle?.apply()
            }

            editLoop?.putBoolean("feature", Statics.currentSongHelper?.isLooping as Boolean)
            editLoop?.apply()
        }

        Statics.playPauseImageButton?.setOnClickListener {

            if (Statics.mediaPlayer?.isPlaying as Boolean)
            {
                Statics.mediaPlayer?.pause()
                Statics.currentSongHelper?.isPlaying = false
                Statics.playPauseImageButton?.setImageResource(R.drawable.play_icon)
            }
            else
            {
                Statics.mediaPlayer?.start()
                Statics.currentSongHelper?.isPlaying = true
                Statics.playPauseImageButton?.setImageResource(R.drawable.pause_icon)
            }
        }
    }

    fun bindShakeListner()
    {
        Statics.safeShake = true

        Statics.mSensorListner = object : SensorEventListener
        {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int)
            {
            }

            override fun onSensorChanged(event: SensorEvent)
            {
                if (!Statics.safeShake) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                mAccelarationLast = mAccelarationCurrent
                mAccelarationCurrent = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = mAccelarationCurrent - mAccelarationLast
                mAccelaration = mAccelaration * 0.9f + delta

                if (mAccelaration > 12)
                {
                    val prefs = Statics.myActivity?.getSharedPreferences(Statics.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)

                    if (isAllowed as Boolean)
                    {
                        Statics.safeShake = false //sets safeShakeFeature to false for 1s so that multiple playNext() calls won't happen at once
                        Statics.playNext(if (Statics.currentSongHelper?.isShuffling as Boolean) "PlayNextNormalShuffle" else "PlayNextNormal")

                        Toast.makeText(Statics.myActivity, "Playing Next Song..", Toast.LENGTH_SHORT).show()

                        var setSafeShakeToTrue = object : Runnable
                        {
                            override fun run()
                            {
                                Statics.safeShake = true
                            }
                        }

                        Handler().postDelayed(setSafeShakeToTrue, 1000) //  sets safeShakeFeature to true after 1 second
                    }
                }
            }
        }
    }
}
