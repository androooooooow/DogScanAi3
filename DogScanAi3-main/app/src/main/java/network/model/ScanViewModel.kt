package network.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {

    private val scanRepository = ScanRepository()

    private val _scanCountResult = MutableLiveData<Result<ScanCountResponse>>()
    val scanCountResult: LiveData<Result<ScanCountResponse>> = _scanCountResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getScanCount(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = scanRepository.getScanCount(email)
            _scanCountResult.value = result
            _isLoading.value = false
        }
    }
}