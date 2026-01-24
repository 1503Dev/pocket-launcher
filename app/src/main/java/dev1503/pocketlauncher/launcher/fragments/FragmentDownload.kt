package dev1503.pocketlauncher.launcher.fragments

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout

class FragmentDownload (self: AppCompatActivity) : Fragment(self, ColumnLayout(self), "FragmentDownload") {
    val columnLayout: ColumnLayout = layout as ColumnLayout

    @Override
    override fun init(): Boolean {
        if (!super.init()) return false

        columnLayout.addDivider(self.getString(R.string.new_game))
        columnLayout.addItem(self.getString(R.string.games), R.drawable.stadia_controller_24px)

        return true
    }
}