package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ItemMusicBinding

/**
 * 歌曲列表适配器，当前正在播放的那一首高亮显示
 */
class MusicAdapter(
    private val data: List<MusicItem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    private var playingIndex = -1

    class ViewHolder(val binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root)

    fun setPlayingIndex(index: Int) {
        val old = playingIndex
        playingIndex = index
        if (old >= 0) notifyItemChanged(old)
        if (index >= 0) notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.tvItemTitle.text = item.title
        holder.binding.tvItemArtist.text = item.artist
        holder.binding.root.setBackgroundColor(
            if (position == playingIndex) {
                holder.itemView.context.getColor(R.color.music_item_selected)
            } else {
                0
            }
        )
        holder.binding.root.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount(): Int = data.size
}
