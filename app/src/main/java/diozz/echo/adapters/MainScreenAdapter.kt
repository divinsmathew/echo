package diozz.echo.adapters

import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import diozz.echo.R
import diozz.echo.Songs
import diozz.echo.databases.EchoDatabase
import diozz.echo.fragments.PlayerFragment
import java.io.File

class MainScreenAdapter(songDetails: ArrayList<Songs>, context: Context) : RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>()
{
    var songDetails: ArrayList<Songs>? = null
    var songDetailsCopy: ArrayList<Songs>? = null
    var context: Context? = null

    init
    {
        this.songDetails = songDetails
        this.songDetailsCopy = ArrayList()
        this.songDetailsCopy?.addAll(songDetails)

        this.context = context
    }

    fun filter(text: String)
    {
        var text = text

        songDetails?.clear()
        //something's wrong here.
        //after clearing, we end up with a non scrollable, non responsive(to touch) list of old items in RecyclerView.
        //looks like the RecyclerView was updated with the cleared list, but the "dead" items are still showing up! :(

        if (text.isEmpty())
            songDetails?.addAll(songDetailsCopy.orEmpty())
        else
        {
            text = text.toLowerCase()
            for (item in songDetailsCopy.orEmpty())
                if (item?.songTitle.toLowerCase().contains(text) || item?.songArtist.toLowerCase().contains(text))
                    songDetails?.add(item)
        }

        notifyDataSetChanged()
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int)
    {
        val songObject = songDetails?.get(p1)
        p0.trackTitle?.text = songObject?.songTitle
        p0.trackArtist?.text = songObject?.songArtist

        p0.contentHolder?.setOnClickListener {
            val playerFragment = PlayerFragment()
            var args = Bundle()
            args.putString("songArtist", songObject?.songArtist)
            args.putString("path", songObject?.songData)
            args.putString("songTitle", songObject?.songTitle)
            args.putInt("songId", songObject?.songId?.toInt() as Int)
            args.putInt("songPosition", p1)
            args.putParcelableArrayList("songData", songDetails)
            playerFragment.arguments = args

            try
            {
                if (PlayerFragment.Statics.mediaPlayer != null)
                    if (PlayerFragment.Statics.mediaPlayer?.isPlaying as Boolean)
                        PlayerFragment.Statics.mediaPlayer?.stop()
            }
            catch (e: Exception)
            {
            }

            (context as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, playerFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        }

        p0.contentHolder?.setOnLongClickListener {

            val builder = AlertDialog.Builder(context as Context)
            builder.setTitle(songObject?.songTitle)

            val deletionBuilder = AlertDialog.Builder(context as Context)
            deletionBuilder.setTitle("Sure?")
            deletionBuilder.setMessage("This will remove \"" + songObject?.songTitle + "\" from your device. This can't be undone.")
            deletionBuilder.setNegativeButton("Cancel", null)
            deletionBuilder.setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                try
                {
                    //if file to be deleted is playing now, change the song
                    try
                    {
                        if (PlayerFragment.Statics.mediaPlayer != null)
                            if (PlayerFragment.Statics.mediaPlayer?.isPlaying as Boolean && PlayerFragment.Statics.currentSongHelper?.songId == songObject?.songId)
                            {
                                if (PlayerFragment.Statics.currentSongHelper?.isShuffling as Boolean)
                                    PlayerFragment.Statics.playNext("PlayNextNormalShuffle")
                                else
                                    PlayerFragment.Statics.playNext("PlayNextNormal")
                            }
                    }
                    catch (e: Exception)
                    {
                    }

                    //deletes file. but this is leaving a blank file of size 0 bytes.
                    val fDelete = File(songObject?.songData)
                    if (fDelete.delete())
                    {
                        //remove from favorites first
                        var favouriteContent = EchoDatabase(context)
                        if (favouriteContent?.checkIfIdExists(songObject?.songId?.toInt() as Int))
                            favouriteContent?.deLeteFavorite(songObject?.songId?.toInt() as Int)

                        removeFromListUsingId(songObject?.songId)

                        //this will completely remove the (blank)file from disk
                        var uri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, songObject?.songId as Long)
                        context?.contentResolver?.delete(uri, null, null)

                        Toast.makeText(context, "Music was deleted", Toast.LENGTH_SHORT).show()
                    }
                    else
                        Toast.makeText(context, "Deletion Failed", Toast.LENGTH_SHORT).show()
                }
                catch (e: Exception)
                {
                    Toast.makeText(context, "Deletion Failed", Toast.LENGTH_SHORT).show()
                }
            }
            var deletionDialog = deletionBuilder.create()


            /*  val renameBuilder = AlertDialog.Builder(context as Context)
              renameBuilder.setTitle("Rename")
              renameBuilder.setNegativeButton("Cancel",null)
              renameBuilder.setPositiveButton("Rename") { dialogInterface: DialogInterface, i: Int ->
                  try
                  {
                      val fdelete = File(songObject?.songData)
                      if(!fdelete.renameTo(File(songObject?.songData?.substringBeforeLast("/") + "wuhhoo")))
                          Toast.makeText(context,"Deletion Failed!", Toast.LENGTH_SHORT).show()
                  }
                  catch (e:Exception)
                  {
                      Toast.makeText(context,"Deletion Failed:\n" + e.message, Toast.LENGTH_SHORT).show()
                  }
              }
              var renameDialog = renameBuilder.create()*/


            var favouriteContent = EchoDatabase(context)
            var favString: String? = "Add To Favorites"
            if (favouriteContent?.checkIfIdExists(songObject?.songId?.toInt() as Int))
                favString = "Remove From Favorites"


            val animals = arrayOf("Delete Song", favString)
            builder.setItems(animals) { dialog, which ->
                when (which)
                {
                    //0 -> renameDialog.show()
                    0 -> deletionDialog.show()
                    1 ->
                        if (favString == "Add To Favorites")
                        {
                            favouriteContent?.storeAsFavorite(songObject?.songId?.toInt() as Int, songObject?.songArtist, songObject?.songTitle, songObject?.songData)
                            Toast.makeText(context, "\"" + songObject?.songTitle + "\" Was Added To Favourites", Toast.LENGTH_SHORT).show()
                        }
                        else
                        {
                            favouriteContent?.deLeteFavorite(songObject?.songId?.toInt() as Int)
                            Toast.makeText(context, "\"" + songObject?.songTitle + "\" Was Removed From Favourites", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            val dialog = builder.create()
            dialog.show()

            true
        }
    }


    private fun removeFromListUsingId(songId: Long?)
    {
        var c = 0
        for (item in songDetails.orEmpty())
        {
            if (item?.songId == songId)
            {
                songDetails?.remove(item)
                notifyItemRemoved(c)
                break
            }
            c++
        }

        for (item in songDetailsCopy.orEmpty())
            if (item?.songId == songId)
            {
                songDetailsCopy?.remove(item)
                break
            }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder
    {
        return MyViewHolder(LayoutInflater.from(p0?.context).inflate(R.layout.song_item, p0, false))
    }

    override fun getItemCount(): Int
    {
        return if (songDetails == null) 0 else (songDetails as ArrayList<Songs>).size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: LinearLayout? = null

        init
        {
            trackTitle = itemView?.findViewById(R.id.trackTitle)
            trackArtist = itemView?.findViewById(R.id.trackArtist)
            contentHolder = itemView?.findViewById(R.id.contentRow)
        }
    }
}
