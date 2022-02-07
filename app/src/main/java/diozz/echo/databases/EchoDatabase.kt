package diozz.echo.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import diozz.echo.Songs

class EchoDatabase : SQLiteOpenHelper
{

    object Statics
    {
        val DB_VERSION = 1
        val DB_NAME = "FavDb"
        val TABLE_NAME = "FavTable"
        val COL_ID = "SongID"
        val COL_SONG_TITLE = "SongTitle"
        val COL_SONG_ARTIST = "SongArtist"
        val COL_SONG_PATH = "SongPath"
    }


    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)
    constructor(context: Context?) : super(context, Statics.DB_NAME, null, Statics.DB_VERSION)

    override fun onCreate(db: SQLiteDatabase?)
    {
        val q = "CREATE TABLE " + Statics.TABLE_NAME + "( " + Statics.COL_ID + " INTEGER, " + Statics.COL_SONG_ARTIST + " STRING, " + Statics.COL_SONG_TITLE + " STRING, " + Statics.COL_SONG_PATH + " STRING);"

        db?.execSQL(q)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {

    }

    fun storeAsFavorite(id: Int, artist: String?, songTitle: String?, path: String?)
    {
        var contentValues = ContentValues()
        contentValues.put(Statics.COL_ID, id)
        contentValues.put(Statics.COL_SONG_ARTIST, artist)
        contentValues.put(Statics.COL_SONG_TITLE, songTitle)
        contentValues.put(Statics.COL_SONG_PATH, path)

        writableDatabase.insert(Statics.TABLE_NAME, null, contentValues)
        writableDatabase.close()
    }

    fun queryDbList(c: Context): ArrayList<Songs>?
    {
        var _songList = ArrayList<Songs>()

        try
        {
            var cur = readableDatabase.rawQuery("SELECT * FROM " + Statics.TABLE_NAME, null)

            if (cur.moveToFirst())
            {
                do
                {
                    var _id = cur.getInt(cur.getColumnIndexOrThrow(Statics.COL_ID))
                    var _artist = cur.getString(cur.getColumnIndexOrThrow(Statics.COL_SONG_ARTIST))
                    var _title = cur.getString(cur.getColumnIndexOrThrow(Statics.COL_SONG_TITLE))
                    var _data = cur.getString(cur.getColumnIndexOrThrow(Statics.COL_SONG_PATH))

                    _songList.add(Songs(_id.toLong(), _title, _artist, _data, 0))


                }
                while (cur.moveToNext())
            }
        }
        catch (e: Exception)
        {
            Toast.makeText(c, e.message, Toast.LENGTH_SHORT).show()
        }

        return _songList
    }

    fun deLeteFavorite(_id: Int)
    {
        writableDatabase.delete(Statics.TABLE_NAME, Statics.COL_ID + "=" + _id, null)
        writableDatabase.close()
    }

    fun checkIfIdExists(_id: Int): Boolean
    {
        //return readableDatabase.rawQuery("SELECT * FROM " + Statics.TABLE_NAME + " WHERE SongId = '$_id'",  null).columnCount > 0
        var storeId = -25814
        var cur = readableDatabase.rawQuery("SELECT * FROM " + Statics.TABLE_NAME + " WHERE SongId = '$_id'", null)

        if (cur.moveToFirst())
        {
            do
            {
                storeId = cur.getInt(cur.getColumnIndexOrThrow(Statics.COL_ID))

            }
            while (cur.moveToNext())
        }
        else
            return false

        return storeId != -25814
    }

    fun checkSize(): Int
    {
        var counter = 0
        var cur = readableDatabase.rawQuery("SELECT * FROM " + Statics.TABLE_NAME, null)
        if (cur.moveToFirst())
        {
            do
            {
                counter++

            }
            while (cur.moveToNext())
        }
        else
            return 0

        return counter
    }
}