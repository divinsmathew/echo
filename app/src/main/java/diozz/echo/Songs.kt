package diozz.echo

import android.os.Parcel
import android.os.Parcelable

class Songs(var songId: Long, var songTitle: String, var songArtist: String, var songData: String, var dateAdded: Long) : Parcelable
{
    init
    {
        if (songTitle?.equals("<unknown>", true))
            this.songTitle = songData?.substringAfterLast("/") //file name

        if (songArtist?.equals("<unknown>", true))
            this.songArtist = "Unknown Artist"
    }

    object Statics
    {
        var nameComparator: Comparator<Songs> = Comparator<Songs> { song1, song2 ->

            val songOne = song1.songTitle.toUpperCase()
            val songTwo = song2.songTitle.toUpperCase()

            songOne.compareTo(songTwo)
        }

        var dateComparator: Comparator<Songs> = Comparator<Songs> { song1, song2 ->

            val songOne = song1.dateAdded.toDouble()
            val songTwo = song2.dateAdded.toDouble()

            songTwo.compareTo(songOne)
        }
    }

    constructor(parcel: Parcel) : this(parcel.readLong(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readLong())
    {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int)
    {
    }

    override fun describeContents(): Int
    {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Songs>
    {
        override fun createFromParcel(parcel: Parcel): Songs
        {
            return Songs(parcel)
        }

        override fun newArray(size: Int): Array<Songs?>
        {
            return arrayOfNulls(size)
        }
    }
}