package com.example.g_corestreamsdemo.screens.streams

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.g_corestreamsdemo.R
import com.example.g_corestreamsdemo.databinding.LiStreamBinding
import com.example.g_corestreamsdemo.model.StreamItemModel

class StreamItemsAdapter : RecyclerView.Adapter<StreamItemsAdapter.StreamItemViewHolder>() {

    private val streamItems: MutableList<StreamItemModel> = ArrayList()
    private lateinit var itemsAdapterListener: StreamItemsAdapterListener

    fun setData(newStreams: List<StreamItemModel>) {
        val diffCallback = StreamsDiffCallback(streamItems, newStreams)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        streamItems.clear()
        streamItems.addAll(newStreams)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setListener(listener: StreamItemsAdapterListener) {
        itemsAdapterListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StreamItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LiStreamBinding.inflate(inflater, parent, false)

        return StreamItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StreamItemViewHolder, position: Int) {
        holder.bind(streamItems[position])
        holder.setListener(itemsAdapterListener, position)
    }

    override fun onViewRecycled(holder: StreamItemViewHolder) {
        super.onViewRecycled(holder)
        holder.clearStreamPreview()
    }


    override fun getItemCount(): Int {
        return streamItems.size
    }

    class StreamItemViewHolder(
        private val binding: LiStreamBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(streamItem: StreamItemModel) {
            binding.streamIdTextView.text = "id: ${streamItem.streamId}"
            binding.streamName.text = streamItem.streamName

            if (streamItem.streamActive) {
                when {
                    streamItem.streamLive -> {
                        binding.streamStatusTV.text =
                            binding.root.context.getString(R.string.is_live)
                        binding.streamStatusTV.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_live_24,
                            0,
                            0,
                            0
                        )
                    }
                    streamItem.streamBackupLive -> {
                        binding.streamStatusTV.text =
                            binding.root.context.getString(R.string.is_backup_live)
                        binding.streamStatusTV.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_backup_live_24,
                            0,
                            0,
                            0
                        )
                    }
                    else -> {
                        binding.streamStatusTV.text =
                            binding.root.context.getString(R.string.is_offline)
                        binding.streamStatusTV.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_offline_24,
                            0,
                            0,
                            0
                        )
                    }
                }
            } else {
                binding.streamStatusTV.text = binding.root.context.getString(R.string.is_disabled)
                binding.streamStatusTV.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_disabled_24,
                    0,
                    0,
                    0
                )
            }

            if (streamItem.streamPreviewUri != null) {
                Glide.with(binding.root)
                    .load(streamItem.streamPreviewUri)
                    .into(binding.streamPreview)
            }
        }

        fun clearStreamPreview() {
            binding.streamPreview.setImageResource(R.color.black)
        }

        fun setListener(listener: StreamItemsAdapterListener, position: Int) {
            binding.root.setOnClickListener {
                listener.onItemClick(position)
            }
        }
    }
}