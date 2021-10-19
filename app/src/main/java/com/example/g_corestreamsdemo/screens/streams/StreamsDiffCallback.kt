package com.example.g_corestreamsdemo.screens.streams

import androidx.recyclerview.widget.DiffUtil
import com.example.g_corestreamsdemo.model.StreamItemModel

class StreamsDiffCallback(
    private val oldList: List<StreamItemModel>,
    private val newList: List<StreamItemModel>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldStreamItem = oldList[oldItemPosition]
        val newStreamItem = newList[newItemPosition]

        return oldStreamItem.streamId == newStreamItem.streamId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldStreamItem = oldList[oldItemPosition]
        val newStreamItem = newList[newItemPosition]

        return oldStreamItem == newStreamItem
    }
}