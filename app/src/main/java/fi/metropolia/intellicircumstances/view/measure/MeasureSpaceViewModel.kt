package fi.metropolia.intellicircumstances.view.measure

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fi.metropolia.intellicircumstances.bluetooth.*
import fi.metropolia.intellicircumstances.bluetooth.decode.RuuviTagSensorData
import fi.metropolia.intellicircumstances.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MeasureSpaceViewModel(application: Application) : AndroidViewModel(application) {
    val tagDevices = MutableLiveData<List<RuuviTagDevice>?>(null)
   // val ruuviTagDevices: LiveData<List<RuuviTagDevice>?>
       // get() = _ruuviTagDevices
    val sensorData = MutableLiveData<RuuviTagSensorData?>(null)

    private val _ruuviConnectionState = MutableLiveData(ConnectionState.DISCONNECTED)
    val ruuviConnectionState: LiveData<ConnectionState>
        get() = _ruuviConnectionState

    private val scannerCallback = object : RuuviTagScannerCallback {
        override fun onScanComplete(ruuviTagDevices: List<RuuviTagDevice>) {
            tagDevices.postValue(ruuviTagDevices)
        }
    }

    private val connectionCallback = object : RuuviTagConnectionCallback {
        override fun onConnectionStateChange(connectionState: ConnectionState) {
            _ruuviConnectionState.postValue(connectionState)
        }

        override fun onReceiveSensorData(ruuviTagSensorData: RuuviTagSensorData) {
            sensorData.postValue(ruuviTagSensorData)
        }
    }

    private val ruuviTagScanner = RuuviTagScanner(application.applicationContext, scannerCallback)
    private val ruuviTagConnector = RuuviTagConnector(application.applicationContext, connectionCallback)

    private val deviceRepository = DeviceRepository(application.applicationContext)

    fun scanDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            if (ruuviTagScanner.startScan()) {
                delay(SCAN_TIMEOUT)
                ruuviTagScanner.stopScan()
            }
        }
    }

    fun addDeviceAndConnect(spaceId: Long, ruuviTagDevice: RuuviTagDevice) {
        viewModelScope.launch {
            deviceRepository.addDeviceToSpace(spaceId, ruuviTagDevice)
            ruuviTagConnector.connectDevice(ruuviTagDevice.macAddress)
        }
    }

    fun connectDevice(spaceId: Long) {
        viewModelScope.launch {
            val device = deviceRepository.getRuuviTagDeviceBySpaceId(spaceId)
            device?.macAddress?.let { ruuviTagConnector.connectDevice(it) }
        }
    }

    fun isBluetoothEnabled(): Flow<Boolean> = flow {
        while (true) {
            emit(ruuviTagConnector.isBluetoothEnabled())
            delay(CHECK_BLUETOOTH)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ruuviTagConnector.disconnectDevice()
    }

    companion object {
        private const val CHECK_BLUETOOTH = 1000L
        private const val SCAN_TIMEOUT = 30000L
    }
}