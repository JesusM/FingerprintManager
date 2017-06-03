package com.jesusm.kfingerprintmanager.encryption.presenter

import com.jesusm.kfingerprintmanager.base.BasePresenterTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FingerprintEncryptionDialogPresenterTest : BasePresenterTest(){
    @Test
    fun shouldCloseIfFingerprintNotAvailable() {
        val presenter = createPresenter(mockView)
        presenter.onViewShown()

        verify(mockView).onFingerprintDisplayed()
    }
}