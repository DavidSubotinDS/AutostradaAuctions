package com.example.autostradaauctions.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.autostradaauctions.data.repository.AuctionRepository
import com.example.autostradaauctions.data.repository.BiddingRepository

class EnhancedAuctionDetailViewModelFactory(
    private val auctionRepository: AuctionRepository,
    private val biddingRepository: BiddingRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancedAuctionDetailViewModel::class.java)) {
            return EnhancedAuctionDetailViewModel(auctionRepository, biddingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
