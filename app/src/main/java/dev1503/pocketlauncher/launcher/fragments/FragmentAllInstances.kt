package dev1503.pocketlauncher.launcher.fragments

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.KVConfig
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import dev1503.pocketlauncher.launcher.widgets.InstanceListView
import dev1503.pocketlauncher.launcher.widgets.ModListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentAllInstances (self: AppCompatActivity) : Fragment(self, ColumnLayout(self), "FragmentAllInstances") {
    private val col = layout as ColumnLayout
    val checkableItems = ArrayList<ColumnLayout.ColumnLayoutItem>()

    val TAG = fragmentName

    @Override
    override fun init(): Boolean {
        val superResult = super.init()
        col.removeContentLayout()

        if (superResult) {
            val itemInternal = col.addItem(self.getString(R.string.internal_storage), R.drawable.folder_24px, true, "/data").apply {
                setIcon(R.drawable.folder_24px)
                checked = true
            }
            checkableItems.add(itemInternal)
        }
        self.lifecycleScope.launch(Dispatchers.IO) {
            val contentLayout = getContentLayout()
            uiRun {
                col.setContentLayout(contentLayout)
            }
        }

        return superResult
    }

    fun getContentLayout(): ScrollView {
        val l = LinearLayout.inflate(self, R.layout.layout_launcher_all_instances, null) as ScrollView

        val instanceListCard = l.findViewWithTag<MaterialCardView>("card_instances")
        val instanceListView = l.findViewWithTag<InstanceListView>("instance_list")
        val infoCard = l.findViewWithTag<MaterialCardView>("card_information")

        val instances = Utils.getAllInstances(self)

        if (instances.isEmpty()) {
            infoCard.isVisible = true
            instanceListCard.isVisible = false
            return l
        }
        infoCard.isVisible = false
        instanceListCard.isVisible = true

        instanceListView.instanceList = instances

        return l
    }

    fun getCheckableItemOnClick(): View.OnClickListener {
        return View.OnClickListener {
            checkableItems.forEach { item ->
                if (item != it) item.checked = false
            }
        }
    }
}