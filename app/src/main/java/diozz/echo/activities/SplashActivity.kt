package diozz.echo.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import diozz.echo.R

class SplashActivity : AppCompatActivity()
{
    var PermissionsString = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                                    Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (!HasPermissions(this@SplashActivity, *PermissionsString))
        {
            ActivityCompat.requestPermissions(this@SplashActivity, PermissionsString, 21)
        }
        else
            Handler().postDelayed({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }, 1000)
    }

    fun HasPermissions(context: Context, vararg perms: String): Boolean
    {
        for (perm in perms)
            if (context.checkCallingOrSelfPermission(perm) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != 21)
            return

        if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
                && grantResults[3] == PackageManager.PERMISSION_GRANTED
                && grantResults[4] == PackageManager.PERMISSION_GRANTED)
            Handler().postDelayed({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }, 1000)
        else
        {
            //Toast.makeText(this@SplashActivity,"Permission Denied!", Toast.LENGTH_SHORT).show();
            var al = AlertDialog.Builder(this@SplashActivity)
            al.setTitle("Permissions Required")
            al.setMessage("Echo requires the asked permissions for working flawlessly. Please grant them to continue.")
            al.setIcon(R.drawable.error)
            al.setPositiveButton("GRANT")
            {
                dialog, which ->
                ActivityCompat.requestPermissions(this@SplashActivity, PermissionsString, 21)
            }
            al.setNegativeButton("Close")
            {
                dialog, which ->
                finish();
            }
            al.setCancelable(false)

            al.show()
        }
    }
}