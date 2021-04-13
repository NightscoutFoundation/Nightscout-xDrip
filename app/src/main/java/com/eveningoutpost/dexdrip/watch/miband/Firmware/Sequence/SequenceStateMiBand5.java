package com.eveningoutpost.dexdrip.watch.miband.Firmware.Sequence;

import com.eveningoutpost.dexdrip.watch.miband.Firmware.FirmwareOperations;

import java.util.ArrayList;
import java.util.List;

public class SequenceStateMiBand5 extends SequenceState {
    public static final String UNKNOWN_REQUEST = "UNKNOWN_REQUEST";
    public static final String WAITING_UNKNOWN_REQUEST_RESPONCE = "WAITING_UNKNOWN_REQUEST_RESPONCE";
    public static final String UNKNOWN_INIT_COMMAND = "UNKNOWN_INIT_COMMAND";

    {
        sequence.clear();
        sequence.add(INIT);
        sequence.add(UNKNOWN_INIT_COMMAND);
        sequence.add(NOTIFICATION_ENABLE);
        sequence.add(SET_NIGHTMODE);
        sequence.add(PREPARE_UPLOAD);
        sequence.add(WAITING_PREPARE_UPLOAD_RESPONSE);
        sequence.add(UNKNOWN_REQUEST);
        sequence.add(WAITING_UNKNOWN_REQUEST_RESPONCE);
        sequence.add(TRANSFER_SEND_WF_INFO);
        sequence.add(WAITING_TRANSFER_SEND_WF_INFO_RESPONSE);
        sequence.add(TRANSFER_FW_START);
        sequence.add(TRANSFER_FW_DATA);
        sequence.add(SEND_CHECKSUM);
        sequence.add(WAITING_SEND_CHECKSUM_RESPONSE);
        sequence.add(CHECKSUM_VERIFIED);
    }
}