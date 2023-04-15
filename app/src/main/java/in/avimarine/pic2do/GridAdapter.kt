package `in`.avimarine.pic2do

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

internal class GridAdapter(
    private val todoList: List<GridImage>,
    private val context: Context
) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private lateinit var gridImage: ImageView

    // below method is use to return the count of course list
    override fun getCount(): Int {
        return todoList.size
    }

    // below function is use to return the item of grid view.
    override fun getItem(position: Int): Any? {
        return null
    }

    // below function is use to return item id of grid view.
    override fun getItemId(position: Int): Long {
        return 0
    }

    // in below function we are getting individual item of grid view.
    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var newView = view
        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (newView == null) {
            newView = layoutInflater!!.inflate(R.layout.image_grid_item, null)
        }
        gridImage = newView!!.findViewById(R.id.gridImage)

        val storageRef = Firebase.storage.reference.child(todoList[position].imageUrl)

        Glide.with(context).load(storageRef).into(gridImage)
        return newView
    }
}