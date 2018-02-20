package fr.coppernic.sample.pcsc.reader;

import android.content.Context;
import android.os.SystemClock;

import java.util.ArrayList;

import fr.coppernic.sdk.pcsc.ApduResponse;
import fr.coppernic.sdk.pcsc.ProtocolControlInformation;
import fr.coppernic.sdk.pcsc.Scard;
import fr.coppernic.sdk.utils.core.CpcBytes;
import fr.coppernic.sdk.utils.core.CpcResult;
import timber.log.Timber;

/**
 * Created by michael on 16/02/18.
 */

public class PcscReader {
    // PCSC
    private Scard sCard = null;
    private boolean isConnected = false;

    private Context context;

    public PcscReader(Context context) {
        this.context = context;
        sCard = new Scard();
    }

    /**
     * Get list of available reader
     *
     * @return List of available reader
     */
    public ArrayList<String> listReaders() {
        ArrayList<String> deviceList = new ArrayList<>();
        CpcResult.RESULT result = sCard.establishContext(context);
        if (result == CpcResult.RESULT.OK) {
            result = sCard.listReaders(deviceList);
            if (result != CpcResult.RESULT.OK) {
                Timber.d("Error list card : %s", result.toString());
            }
        } else {
            Timber.d("Error establish context : %s", result.toString());
            return null;
        }

        return deviceList;
    }

    /**
     * Connect to a card with the reader
     *
     * @param readerName name of the reader
     * @return {@link CpcResult.RESULT}
     */
    public CpcResult.RESULT connect(String readerName) {
        CpcResult.RESULT result = sCard.connect(readerName, 0, 0);
        if (result == CpcResult.RESULT.OK) {
            isConnected = true;
        }
        return result;
    }

    /**
     * Disconnect reader
     *
     * @return {@link CpcResult.RESULT}
     */
    public CpcResult.RESULT disconnect() {
        isConnected = false;
        return sCard.disconnect();
    }

    /**
     * Get ATR from card
     *
     * @return ATR value
     */
    public String getAtr() {
        return CpcBytes.byteArrayToString(sCard.getAtr(), sCard.getAtr().length);
    }

    /**
     * Sends APDU to a card
     *
     * @param apduCommand APDU command
     * @return {@link ApduResponse}
     * @throws CpcResult.ResultException
     */
    public ApduResponse sendApdu(String apduCommand) throws CpcResult.ResultException {
        //remove space if any
        apduCommand = apduCommand.replaceAll(" ", "");

        byte[] apdu = CpcBytes.parseHexStringToArray(apduCommand);
        ApduResponse apduResponse = new ApduResponse();

        Long initTime = SystemClock.elapsedRealtime();
        CpcResult.RESULT res = sCard.transmit(new ProtocolControlInformation(ProtocolControlInformation.Protocol.T0), apdu, new ProtocolControlInformation(ProtocolControlInformation.Protocol.T0), apduResponse);
        Timber.d("Transmit Time = %d", (SystemClock.elapsedRealtime() - initTime));

        if (res != CpcResult.RESULT.OK) {
            Timber.d("Transmit : %s", res.toString());
            throw new CpcResult.ResultException(res);
        }

        Timber.d("Transmit : OK");
        return apduResponse;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
