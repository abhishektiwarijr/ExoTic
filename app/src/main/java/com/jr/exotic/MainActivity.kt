package com.jr.exotic

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
    }

    private var downloadDirectory: File? = null
    private var downloadCache: Cache? = null
    private var databaseProvider: DatabaseProvider? = null


    private val url = "http://aniin.com/videos/oct08v1.mp4"
    private val userAgent by lazy {
        Util.getUserAgent(this, getString(R.string.app_name))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRotate.setOnClickListener {
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                btnRotate.text = getString(R.string.portrait)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                btnRotate.text = getString(R.string.landscape)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }

        val dataSourceFactory = buildDataSourceFactory()
        //Instantiate the player.
        val player = ExoPlayerFactory.newSimpleInstance(
            this, DefaultRenderersFactory(this)
            , DefaultTrackSelector(), DefaultLoadControl()
        )
        //Attach player to the view.
        exoPlayerView.player = player
        //Create media source
        val mediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
        //Prepare the video player with media source.
        player.playWhenReady = true
        player.prepare(mediaSource)

    }

    private fun buildDataSourceFactory(): DataSource.Factory {
        val upstreamFactory = DefaultDataSourceFactory(this, buildHttpDataSourceFactory())
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache())
    }

    private fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(userAgent)
    }

    private fun buildReadOnlyCacheDataSource(
        upstreamFactory: DataSource.Factory, cache: Cache
    ): CacheDataSourceFactory {
        return CacheDataSourceFactory(
            cache,
            upstreamFactory,
            FileDataSourceFactory(),
            /* eventListener= */ null,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
        )/* cacheWriteDataSinkFactory= */
    }

    @Synchronized
    private fun getDownloadCache(): Cache {
        if (downloadCache == null) {
            val downloadContentDirectory = File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache =
                SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), getDatabaseProvider())
        }
        return downloadCache as Cache
    }

    private fun getDownloadDirectory(): File? {
        if (downloadDirectory == null) {
            downloadDirectory = getExternalFilesDir(null)
            if (downloadDirectory == null) {
                downloadDirectory = filesDir
            }
        }
        return downloadDirectory
    }

    private fun getDatabaseProvider(): DatabaseProvider {
        if (databaseProvider == null) {
            databaseProvider = ExoDatabaseProvider(this)
        }
        return databaseProvider as DatabaseProvider
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            btnRotate.text = getString(R.string.portrait)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            btnRotate.text = getString(R.string.landscape)
        }
    }
}