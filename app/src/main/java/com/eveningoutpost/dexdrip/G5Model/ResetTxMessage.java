package com.eveningoutpost.dexdrip.G5Model;


import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.Models.usererror.UserErrorLog;

// jamorham

class ResetTxMessage extends BaseMessage {
    static final byte opcode = 0x42;

    ResetTxMessage() {
        init(opcode, 3);
        UserErrorLog.d(TAG, "ResetTx dbg: " + JoH.bytesToHex(byteSequence));
    }
}
