package com.rdapps.gamepad.command.handler.subcommand;

import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;

import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

/**
 * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md#subcommand-0x30-set-player-lights
 */
class SetPlayerLightsHandler implements SubCommandHandler {
    private final static String TAG = SetPlayerLightsHandler.class.getName();
    private final static byte ACK = (byte) 0x80;

    @Override
    public InputReport handleRumbleAndSubCommand(JoyController joyController, OutputReport outputReport) {
        InputReport subCommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subCommandReply.fillAckByte(ACK);
        subCommandReply.fillSubCommand(outputReport.getSubCommandId());
        byte lightsByte = outputReport.getData()[10];
        joyController.setPlayerLights(lightsByte);
        return subCommandReply;
    }
}
