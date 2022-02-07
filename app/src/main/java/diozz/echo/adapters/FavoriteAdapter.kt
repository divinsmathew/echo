package diozz.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import diozz.echo.R
import diozz.echo.Songs
import diozz.echo.fragments.PlayerFragment

class FavoriteAdapter(songDetails: ArrayList<Songs>, context: Context) : RecyclerView.Adapter<FavoriteAdapter.MyViewHolder>()
{
    var songDetails: ArrayList<Songs>? = null
    var context: Context? = null

    init
    {
        this.songDetails = songDetails
        this.context = context
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
                    .addToBackStack("SongPlayingFragmentFav")
                    .commit()
        }
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
            trackTitle = itemView?.findViewById<TextView>(R.id.trackTitle)
            trackArtist = itemView?.findViewById<TextView>(R.id.trackArtist)
            contentHolder = itemView?.findViewById<LinearLayout>(R.id.contentRow)
        }
    }
}
