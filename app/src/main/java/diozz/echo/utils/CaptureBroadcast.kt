package diozz.echo.utils

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import diozz.echo.R
import diozz.echo.activities.MainActivity
import diozz.echo.fragments.PlayerFragment

class CaptureBroadcast : BroadcastReceiver()
{
    override fun onReceive(context: Context?, intent: Intent?)
    {
        //Toast.makeText(context,intent?.action, Toast.LENGTH_SHORT).show()
        try
        {
            if (intent?.action == Intent.ACTION_NEW_OUTGOING_CALL)
            {
                if (PlayerFragment.Statics.mediaPlayer?.isPlaying as Boolean)
                {
                    PlayerFragment.Statics.mediaPlayer?.pause()
                    PlayerFragment.Statics.currentSongHelper?.isPlaying = false
                    PlayerFragment.Statics.playPauseImageButton?.setImageResource(R.drawable.play_icon)

                    MainActivity.Statify.notifManager?.cancel(51)
                }
            }
            else when ((context?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager).callState)
            {
                TelephonyManager.CALL_STATE_RINGING ->
                {
                    MainActivity.Statify.notifManager?.cancel(51)
                    PlayerFragment.Statics.mediaPlayer?.pause()
                    PlayerFragment.Statics.currentSongHelper?.isPlaying = false
                    PlayerFragment.Statics.playPauseImageButton?.setImageResource(R.drawable.play_icon)
                }
            }
        }
        catch (e: Exception)
        {
            //Toast.makeText(context,e.message,Toast.LENGTH_SHORT).show()
        }
    }
}