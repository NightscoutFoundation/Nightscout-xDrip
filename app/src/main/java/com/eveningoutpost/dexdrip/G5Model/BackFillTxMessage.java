package com.eveningoutpost.dexdrip.G5Model;


import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.Models.usererror.UserErrorLog;

import static com.eveningoutpost.dexdrip.G5Model.DexTimeKeeper.getDexTime;

// created by jamorham

public class BackFillTxMessage extends BaseMessage {

    final byte opcode = 0x50;
    final int length = 20;

    public BackFillTxMessage(int startDexTime, int endDexTime) {
        init(opcode, length);
        data.put((byte) 0x5);
        data.put((byte) 0x2);
        data.put((byte) 0x0);
        data.putInt(startDexTime);
        data.putInt(endDexTime);
        data.put(new byte[6]);
        appendCRC();
        UserErrorLog.d(TAG, "BackfillTxMessage dbg: " + JoH.bytesToHex(byteSequence));
    }

    public static BackFillTxMessage get(String id, long startTime, long endTime) {

        final int dexStart = getDexTime(id, startTime);
        final int dexEnd = getDexTime(id, endTime);
        if (dexStart < 1 || dexEnd < 1) {
            UserErrorLog.e(TAG, "Unable to calculate start or end time for BackFillTxMessage");
            return null;
        }
        return new BackFillTxMessage(dexStart, dexEnd);
    }

}
